package com.nanaios.refined_ammo_box.item;

import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class RefinedAmmoBoxEnergyStorage implements IEnergyStorage {
    private final ItemStack stack;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;

    public RefinedAmmoBoxEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
        this.stack = stack;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    public RefinedAmmoBoxEnergyStorage(ItemStack stack, int capacity) {
        this(stack, capacity, capacity, capacity);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;
        int energyStored = getEnergyStored();
        int energyReceived = Math.min(capacity - energyStored, Math.min(this.maxReceive, maxReceive));
        if (!simulate && energyReceived > 0) {
            stack.set(RefinedAmmoBoxDataComponents.ENERGY, energyStored + energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;
        int energyStored = getEnergyStored();
        int energyExtracted = Math.min(energyStored, Math.min(this.maxExtract, maxExtract));
        if (!simulate && energyExtracted > 0) {
            stack.set(RefinedAmmoBoxDataComponents.ENERGY, energyStored - energyExtracted);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return stack.getOrDefault(RefinedAmmoBoxDataComponents.ENERGY, 0);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    public static class Creative implements IEnergyStorage {
        public static final Creative INSTANCE = new Creative();

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return maxExtract;
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
