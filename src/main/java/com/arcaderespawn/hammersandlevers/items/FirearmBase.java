package com.arcaderespawn.hammersandlevers.items;

import com.arcaderespawn.hammersandlevers.HammersAndLevers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.InfinityEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

public class FirearmBase extends Item {
    public static final ITag.INamedTag<Item> rifleCartridge = ItemTags.createOptional(new ResourceLocation(HammersAndLevers.MODID, "rifle_cartridge"));
    public static final ITag.INamedTag<Item> pistolCartridge = ItemTags.createOptional(new ResourceLocation(HammersAndLevers.MODID,"pistol_cartridge"));
    public static final ITag.INamedTag<Item> shotgunCartridge = ItemTags.createOptional(new ResourceLocation(HammersAndLevers.MODID,"shotgun_cartridge"));

    protected Integer internalMagazineSize;

    private static final float FIREARM_MAX_RANGE = 2048.0f;
    protected static final Logger LOGGER = LogManager.getLogger();

    protected DamageSource damageSource = DamageSource.MAGIC;
    protected float damage = 5.0f;

    public FirearmBase(Properties properties) {
        super(properties);
    }

    private static void setAmmoCount(ItemStack stack, Byte itemCount) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        compoundnbt.putByte("AmmoCount", itemCount);
    }

    public static byte getAmmoCount(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        return compoundnbt.getByte("AmmoCount");
    }

    private static byte getMaxAmmoCount() {
        return 6;
    }

    protected void doShoot(World worldIn, PlayerEntity playerIn, ItemStack stackIn) {
        doShoot(worldIn, playerIn, stackIn, 0.0f, 0.0f);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        ItemStack ammo = this.performInventoryAmmoSearch(playerIn);

        boolean doNotConsumeAmmoFlag =
                (playerIn.isCreative() || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0)
                        && !ammo.isEmpty();

        if(playerIn.isSneaking() && !ammo.isEmpty() && getAmmoCount(stack) < getMaxAmmoCount())
        {
            if(!doNotConsumeAmmoFlag)
            {
                ammo.shrink(1);

                if(ammo.isEmpty()) playerIn.inventory.deleteStack(ammo);
            }

            setAmmoCount(stack, (byte)(getAmmoCount(stack) + 1));
            return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
        }

        if(getAmmoCount(stack) > 0)
        {
            doShoot(worldIn, playerIn, stack);
            setAmmoCount(stack, (byte)(getAmmoCount(stack) - 1));
            return new ActionResult<ItemStack>(ActionResultType.CONSUME, stack);
        }

        return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 128;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    protected void doShoot(World worldIn, PlayerEntity playerIn, ItemStack stackIn, float yawSkew, float pitchSkew) {
        if(getAmmoCount(stackIn) > 0) {
            setAmmoCount(stackIn,(byte)(getAmmoCount(stackIn) - 1));

            RayTraceResult result = fireShootRay(worldIn, playerIn, stackIn, yawSkew, pitchSkew);

            if(result instanceof EntityRayTraceResult)
            {
                if(((EntityRayTraceResult) result).getEntity() instanceof LivingEntity)
                {
                    ((LivingEntity)((EntityRayTraceResult) result).getEntity()).attackEntityFrom(damageSource, damage);
                }
            }

            worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, result.getHitVec().x, result.getHitVec().y, result.getHitVec().z, 0.2, 0.2, 0.2);
        }
    }

    protected RayTraceResult fireShootRay(World worldIn, PlayerEntity playerIn, ItemStack stackIn, float yawSkew, float pitchSkew)
    {
        Vector3d startVec = playerIn.getEyePosition(0.0f);
        Vector3d stopVec = startVec.add(playerIn.getLookVec().add(Vector3d.fromPitchYaw(pitchSkew, yawSkew)).scale(FIREARM_MAX_RANGE));

        EntityRayTraceResult result = ProjectileHelper.rayTraceEntities(worldIn, playerIn,
                startVec,
                stopVec,
                new AxisAlignedBB(startVec, stopVec), null);

        RayTraceResult worldResult = worldIn.rayTraceBlocks( new RayTraceContext(startVec, stopVec,
            RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, playerIn));

        if(result != null) {
            return result;
        } else {
            return worldResult;
        }
    }

    protected ItemStack performInventoryAmmoSearch(PlayerEntity playerIn)
    {
        ItemStack ammo = ItemStack.EMPTY;
        Predicate<ItemStack> ammoPredicate = getInventoryAmmoSearchPredicate();
        if(ammoPredicate.test(playerIn.getHeldItem(Hand.OFF_HAND))) {
            ammo = playerIn.getHeldItem(Hand.OFF_HAND);
        }
        else {
            for(int i = 0; i < playerIn.inventory.getSizeInventory(); i++) {
                ItemStack stackInSlot = playerIn.inventory.getStackInSlot(i);
                if (ammoPredicate.test(stackInSlot)) {
                    ammo = stackInSlot;
                    break;
                }
            }
        }

        return ammo;
    }

    protected Predicate<ItemStack> getInventoryAmmoSearchPredicate() {
        return RIFLE_CARTRIDGES.or(PISTOL_CARTRIDGES).or(SHOTGUN_CARTRIDGES);
    }

    protected Predicate<ItemStack> RIFLE_CARTRIDGES = (stack) -> {
        return stack.getItem().isIn(rifleCartridge);
    };

    protected Predicate<ItemStack> PAPER_RIFLE_CARTRIDGES = (stack) -> {
        return stack.getItem() == HammersAndLevers.riflePaperCartridgeItem;
    };

    protected Predicate<ItemStack> BRASS_RIFLE_CARTRIDGES = (stack) -> {
        return stack.getItem() == HammersAndLevers.rifleCartridgeItem;
    };

    protected Predicate<ItemStack> PISTOL_CARTRIDGES = (stack) -> {
        return stack.getItem().isIn(pistolCartridge);
    };

    protected Predicate<ItemStack> PAPER_PISTOL_CARTRIDGES = (stack) -> {
        return stack.getItem() == HammersAndLevers.pistolPaperCartridgeItem;
    };

    protected Predicate<ItemStack> BRASS_PISTOL_CARTRIDGES = (stack) -> {
        return stack.getItem() == HammersAndLevers.pistolCartridgeItem;
    };

    protected Predicate<ItemStack> SHOTGUN_CARTRIDGES = (stack) -> {
        return stack.getItem().isIn(shotgunCartridge);
    };
}
