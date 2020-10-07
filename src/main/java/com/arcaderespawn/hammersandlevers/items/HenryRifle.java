package com.arcaderespawn.hammersandlevers.items;

import com.arcaderespawn.hammersandlevers.HammersAndLevers;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class HenryRifle extends RifleBase {
    public HenryRifle(Properties properties) {
        super(properties);

        this.setRegistryName("henry_rifle");
    }

    @Override
    protected Predicate<ItemStack> getInventoryAmmoSearchPredicate() {
        return BRASS_RIFLE_CARTRIDGES;
    }
}
