package com.arcaderespawn.hammersandlevers.items;

import com.arcaderespawn.hammersandlevers.HammersAndLevers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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

    public static String getAmmoString(ItemStack stack) {
        return String.valueOf(getAmmoCount(stack));
    }

    public static void setCanFire(ItemStack stack, boolean canFire) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        compoundnbt.putBoolean("canFire", canFire);
    }

    public static boolean getCanFire(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        return compoundnbt.getBoolean("canFire");
    }

    public static boolean readyToFire(ItemStack stack) {
        return getCanFire(stack) && getAmmoCount(stack) > 0;
    }

    protected byte getMaxAmmoCount() {
        return 6;
    }

    protected ActionResult<ItemStack> doReload(PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag)
    {
        if(!doNotConsumeAmmoFlag)
        {
            ammo.shrink(1);

            if(ammo.isEmpty()) playerIn.inventory.deleteStack(ammo);
        }

        setAmmoCount(weapon, (byte)(getAmmoCount(weapon) + 1));
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, weapon);
    }

    protected ActionResult<ItemStack> fireWeaponSucceed(World worldIn, PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag)
    {
        doShoot(worldIn, playerIn, weapon);
        setAmmoCount(weapon,(byte)(getAmmoCount(weapon) - 1));
        return new ActionResult<ItemStack>(ActionResultType.CONSUME, weapon);
    }

    protected ActionResult<ItemStack> fireWeaponFailed(World worldIn, PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag)
    {
        return new ActionResult<ItemStack>(ActionResultType.PASS, weapon);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack weapon = playerIn.getHeldItem(handIn);
        ItemStack ammo = this.performInventoryAmmoSearch(playerIn);

        boolean doNotConsumeAmmoFlag =
                (playerIn.isCreative() || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, weapon) > 0)
                        && !ammo.isEmpty();

        if(playerIn.isSneaking() && !ammo.isEmpty() && getAmmoCount(weapon) < this.getMaxAmmoCount())
        {
            return doReload(playerIn, weapon, ammo, doNotConsumeAmmoFlag);
        }

        if(readyToFire(weapon))
        {
            return this.fireWeaponSucceed(worldIn, playerIn, weapon, ammo, doNotConsumeAmmoFlag);
        }
        else
        {
            return this.fireWeaponFailed(worldIn, playerIn, weapon, ammo, doNotConsumeAmmoFlag);
        }
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 128;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    protected void doShoot(World worldIn, PlayerEntity playerIn, ItemStack stackIn, float pitchSkew, float yawSkew) {
        RayTraceResult result = fireHitRay(worldIn, playerIn, stackIn, yawSkew, pitchSkew);

        LOGGER.debug(result);
        if(result instanceof EntityRayTraceResult)
        {
            if(((EntityRayTraceResult) result).getEntity() instanceof LivingEntity)
            {
                ((LivingEntity)((EntityRayTraceResult) result).getEntity()).attackEntityFrom(damageSource, damage);
            }
        }

        worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, result.getHitVec().x, result.getHitVec().y, result.getHitVec().z, 0.2, 0.2, 0.2);
    }

    protected void doShoot(World worldIn, PlayerEntity playerIn, ItemStack stackIn) {
        doShoot(worldIn, playerIn, stackIn, 0.0f, 0.0f);
    }

    protected RayTraceResult fireHitRay(World worldIn, PlayerEntity playerIn, ItemStack stackIn,
                                        float yawSkew, float pitchSkew)
    {
        Vector3d startVec = playerIn.getEyePosition(0.0f);
        Vector3d stopVec = playerIn.getEyePosition(0.0f)
                .add(getVectorForRotation(playerIn.rotationPitch + pitchSkew, playerIn.rotationYaw + yawSkew)
                .scale(FIREARM_MAX_RANGE));

        LOGGER.debug(startVec);
        LOGGER.debug(stopVec);

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

    protected final Vector3d getVectorForRotation(float pitch, float yaw) {
        float pitchRadians = pitch * ((float)Math.PI / 180F);
        float yawRadians = -yaw * ((float)Math.PI / 180F);
        float yawCos = MathHelper.cos(yawRadians);
        float yawSin = MathHelper.sin(yawRadians);
        float pitchCos = MathHelper.cos(pitchRadians);
        float pitchSin = MathHelper.sin(pitchRadians);
        return new Vector3d((double)(yawSin * pitchCos), (double)(-pitchSin), (double)(yawCos * pitchCos));
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
