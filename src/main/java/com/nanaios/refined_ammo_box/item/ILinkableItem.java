package com.nanaios.refined_ammo_box.item;

import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxDataComponents;
import net.minecraft.world.item.ItemStack;

public interface ILinkableItem {

    default void setLinked(ItemStack stack, boolean isLinked) {
        stack.set(RefinedAmmoBoxDataComponents.AMMO_BOX_LINKED, isLinked);
    }

    default boolean isLinked(ItemStack stack) {
        return stack.getOrDefault(RefinedAmmoBoxDataComponents.AMMO_BOX_LINKED, false);
    }
}