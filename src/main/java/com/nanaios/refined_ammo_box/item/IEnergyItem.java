package com.nanaios.refined_ammo_box.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public interface IEnergyItem {
    default int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null ? energy.receiveEnergy(maxReceive, simulate) : 0;
    }

    default int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null ? energy.extractEnergy(maxExtract, simulate) : 0;
    }

    default int getEnergyStored(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null ? energy.getEnergyStored() : 0;
    }

    default int getMaxEnergyStored(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null ? energy.getMaxEnergyStored() : 0;
    }

    default boolean canExtract(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null && energy.canExtract();
    }

    default boolean canReceive(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null && energy.canReceive();
    }
}
