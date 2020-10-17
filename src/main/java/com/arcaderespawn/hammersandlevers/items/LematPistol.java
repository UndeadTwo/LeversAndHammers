package com.arcaderespawn.hammersandlevers.items;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.swing.*;
import java.util.function.Predicate;

public class LematPistol extends FirearmBase
{
    private final int SHOTGUN_PELLETS = 13;

    public LematPistol(Properties properties)
    {
        super(properties);

        this.setRegistryName("lemat_pistol");
    }

    protected byte getMaxAmmoCount() {
        return 8;
    }

    private static boolean getShellLoaded(ItemStack stack)
    {
        CompoundNBT tag = stack.getOrCreateTag();
        return tag.getBoolean("shellLoaded");
    }

    private static void setShellLoaded(ItemStack stack, Boolean loaded)
    {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putBoolean("shellLoaded", loaded);
    }

    @Override
    public String getAmmoString(ItemStack stack)
    {
        if(getFireMode(stack) == FireMode.PISTOL)
        {
            return String.valueOf(getAmmoCount(stack));
        }
        else
        {
            return String.valueOf(getShellLoaded(stack) ? 1 : 0);
        }
    }

    public enum FireMode
    {
        SHOTGUN ((byte)1),
        PISTOL ((byte)0);

        private final byte numericValue;

        FireMode(byte numericValue)
        {
            this.numericValue = numericValue;
        }

        public byte getNumericValue() {
            return numericValue;
        }
    }

    private static FireMode getFireMode(ItemStack stack)
    {
        CompoundNBT tag = stack.getOrCreateTag();

        switch(tag.getByte("fireMode"))
        {
            case 1:
                return FireMode.SHOTGUN;
            default:
                return FireMode.PISTOL;
        }
    }

    private static void setFireMode(ItemStack stack, FireMode mode)
    {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putByte("fireMode", mode.getNumericValue());
    }

    //Hide the FirearmBase implementation to maintain api consistency.
    public static boolean readyToFire(ItemStack stack)
    {
        return getCanFire(stack) && getAmmoCount(stack) > 0 || getCanFire(stack) && getShellLoaded(stack);
    }

    protected ItemStack performInventoryShellSearch(PlayerEntity playerIn)
    {
        ItemStack ammo = ItemStack.EMPTY;
        Predicate<ItemStack> ammoPredicate = SHOTGUN_CARTRIDGES;
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

    protected ActionResult<ItemStack> doShellReload(PlayerEntity playerIn, ItemStack weapon, ItemStack shell, boolean doNotConsumeAmmoFlag)
    {
        if(!doNotConsumeAmmoFlag)
        {
            shell.shrink(1);

            if(shell.isEmpty()) playerIn.inventory.deleteStack(shell);
        }

        setShellLoaded(weapon, true);
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, weapon);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack weapon = playerIn.getHeldItem(handIn);
        ItemStack ammo = this.performInventoryAmmoSearch(playerIn);
        ItemStack shells = this.performInventoryShellSearch(playerIn);

        boolean doNotConsumeAmmoFlag =
                (playerIn.isCreative() || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, weapon) > 0)
                        && !ammo.isEmpty();

        if(playerIn.isSneaking() && getFireMode(weapon) == FireMode.PISTOL && !ammo.isEmpty() && getAmmoCount(weapon) < this.getMaxAmmoCount())
        {
            LOGGER.debug("Loaded round.");
            return doReload(playerIn, weapon, ammo, doNotConsumeAmmoFlag);
        }
        else if(playerIn.isSneaking() && getFireMode(weapon) == FireMode.SHOTGUN && !shells.isEmpty() && !getShellLoaded(weapon))
        {
            LOGGER.debug("Loaded shell.");
            return doShellReload(playerIn, weapon, shells, doNotConsumeAmmoFlag);
        }
        else if(playerIn.isSneaking())
        {
            LOGGER.debug("Switched mode.");
            if(getFireMode(weapon) == FireMode.PISTOL) setFireMode(weapon, FireMode.SHOTGUN);
            else if(getFireMode(weapon) == FireMode.SHOTGUN) setFireMode(weapon, FireMode.PISTOL);

            return new ActionResult<>(ActionResultType.SUCCESS, weapon);
        }

        if(readyToFire(weapon))
        {
            if(getFireMode(weapon) == FireMode.PISTOL && getAmmoCount(weapon) > 0)
            {
                return this.fireWeaponPistolSucceed(worldIn, playerIn, weapon, ammo, doNotConsumeAmmoFlag);
            }
            else if(getFireMode(weapon) == FireMode.SHOTGUN && getShellLoaded(weapon))
            {
                return this.fireWeaponShotgunSucceed(worldIn, playerIn, weapon, ammo, doNotConsumeAmmoFlag);
            }
        }
        else
        {
            return this.fireWeaponFailed(worldIn, playerIn, weapon, ammo, doNotConsumeAmmoFlag);
        }

        return new ActionResult<>(ActionResultType.PASS, weapon);
    }

    @Override
    protected Predicate<ItemStack> getInventoryAmmoSearchPredicate()
    {
        return PAPER_PISTOL_CARTRIDGES;
    }

    protected ActionResult<ItemStack> fireWeaponPistolSucceed(World worldIn, PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag)
    {
        setCanFire(weapon, false);
        return super.fireWeaponSucceed(worldIn, playerIn, weapon, ammo, doNotConsumeAmmoFlag);
    }

    protected ActionResult<ItemStack> fireWeaponShotgunSucceed(World worldIn, PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag)
    {
        setCanFire(weapon, false);
        setShellLoaded(weapon, false);

        for(int i = 0; i < SHOTGUN_PELLETS; i++)
        {
            float calcedYaw = 0;
            float calcedPitch = 0;
            float distanceMod = 1;
            float modI = 0.f;

            if(i < 8) {
                modI = (float)(i * 45);
                distanceMod = 5.0f;
            }
            if(i < 12) {
                modI = (float) (i - 8 * 90);
                distanceMod = 2.5f;
            }
            if(i != 12)
            {
                calcedPitch = (float)Math.sin(Math.toRadians(modI)) * distanceMod;
                calcedYaw = (float)Math.cos(Math.toRadians(modI)) * distanceMod;
            }

            LOGGER.debug(i);
            LOGGER.debug(calcedPitch);
            LOGGER.debug(calcedYaw);
            doShoot(worldIn, playerIn, weapon, calcedPitch, calcedYaw);
        }

        return new ActionResult<ItemStack>(ActionResultType.CONSUME, weapon);
    }

    public static int getAmmoColor(ItemStack stack)
    {
        if(getFireMode(stack) == FireMode.PISTOL)
            return 0xFF0000;
        else
            return 0x00FF00;
    }

    @Override
    protected ActionResult<ItemStack> fireWeaponFailed(World worldIn, PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag)
    {
        setCanFire(weapon, true);
        return new ActionResult<ItemStack>(ActionResultType.PASS, weapon);
    }
}
