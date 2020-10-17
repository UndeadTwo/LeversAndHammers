package com.arcaderespawn.hammersandlevers.items;

import com.arcaderespawn.hammersandlevers.HammersAndLevers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class HenryRifle extends RifleBase {
    public HenryRifle(Properties properties) {
        super(properties);

        this.setRegistryName("henry_rifle");
    }

    private static byte getMaxAmmoCount() {
        return 15;
    }

    @Override
    protected Predicate<ItemStack> getInventoryAmmoSearchPredicate() {
        return BRASS_RIFLE_CARTRIDGES;
    }

    @Override
    protected ActionResult<ItemStack> fireWeaponFailed(World worldIn, PlayerEntity playerIn, ItemStack weapon, ItemStack ammo, boolean doNotConsumeAmmoFlag) {
        setCanFire(weapon, true);
        return new ActionResult<ItemStack>(ActionResultType.PASS, weapon);
    }
}
