package com.arcaderespawn.hammersandlevers.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class LematPistol extends PistolBase
{


    public LematPistol(Properties properties)
    {
        super(properties);

        this.setRegistryName("lemat_pistol");
    }

    private static byte getMaxAmmoCount() {
        return 6;
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

    @Override
    protected Predicate<ItemStack> getInventoryAmmoSearchPredicate()
    {
        return PISTOL_CARTRIDGES;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {


        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    protected void doShoot(World worldIn, PlayerEntity playerIn, ItemStack stackIn) {
        super.doShoot(worldIn, playerIn, stackIn);
    }
}
