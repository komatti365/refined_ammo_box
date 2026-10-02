package com.nanaios.refined_ammo_box.item;

import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxDataComponents;
import net.minecraft.world.item.ItemStack;

/// タイムスタンプを管理するためのインターフェース
public interface ITimeStamp {

    default long getTimeStamp(ItemStack stack) {
        return stack.getOrDefault(RefinedAmmoBoxDataComponents.AMMO_BOX_REFRESH, 0L);
    }

    default void setTimeStamp(ItemStack stack, long value) {
        stack.set(RefinedAmmoBoxDataComponents.AMMO_BOX_REFRESH, value);
    }
}