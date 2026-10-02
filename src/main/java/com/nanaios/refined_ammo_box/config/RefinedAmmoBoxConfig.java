package com.nanaios.refined_ammo_box.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RefinedAmmoBoxConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue AMMO_BOX_USE_POWER_PER_AMMO;
    public static final ModConfigSpec.IntValue AMMO_BOX_ENERGY_CAPACITY;

    static {
        BUILDER.push("power");
        AMMO_BOX_USE_POWER_PER_AMMO = BUILDER
                .comment("Energy consumed per round of ammo retrieved from an ammo box, default: 2")
                .defineInRange("AmmoBoxUsePowerPerAmmo", 2, 0, Integer.MAX_VALUE);
        AMMO_BOX_ENERGY_CAPACITY = BUILDER
                .comment("Max energy capacity of the wireless ammo box, default: 32000")
                .defineInRange("AmmoBoxEnergyCapacity", 32000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
