package com.nanaios.refined_ammo_box;

import com.mojang.logging.LogUtils;
import com.nanaios.refined_ammo_box.config.RefinedAmmoBoxConfig;
import com.nanaios.refined_ammo_box.item.RefinedAmmoBoxEnergyStorage;
import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxCreativeTabs;
import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxDataComponents;
import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxItems;
import com.tacz.guns.GunMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(RefinedAmmoBox.MODID)
public class RefinedAmmoBox {
    public static final String MODID = "refined_ammo_box";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RefinedAmmoBox(IEventBus modEventBus, ModContainer modContainer) {
        // Data Componentsの登録
        LOGGER.info("Registering Refined Ammo Box data components...");
        RefinedAmmoBoxDataComponents.DATA_COMPONENTS.register(modEventBus);

        // アイテムの登録
        LOGGER.info("Registering Refined Ammo Box items...");
        RefinedAmmoBoxItems.WIRELESS_ITEMS.register(modEventBus);
        RefinedAmmoBoxItems.FAKE_ITEMS.register(modEventBus);

        // クリエイティブタブの登録
        LOGGER.info("Registering Refined Ammo Box creative tabs...");
        RefinedAmmoBoxCreativeTabs.TABS.register(modEventBus);

        // コンフィグの登録
        LOGGER.info("Registering Refined Ammo Box config...");
        modContainer.registerConfig(ModConfig.Type.COMMON, RefinedAmmoBoxConfig.SPEC);

        // イベントリスナーの登録
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::buildContents);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new RefinedAmmoBoxEnergyStorage(stack, RefinedAmmoBoxConfig.AMMO_BOX_ENERGY_CAPACITY.get()),
                RefinedAmmoBoxItems.AMMO_BOX.get()
        );
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> RefinedAmmoBoxEnergyStorage.Creative.INSTANCE,
                RefinedAmmoBoxItems.CREATIVE_AMMO_BOX.get()
        );
    }

    public void buildContents(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation targetTabLocation = ResourceLocation.fromNamespaceAndPath(GunMod.MOD_ID, "other");
        if (!event.getTabKey().location().equals(targetTabLocation)) return;

        LOGGER.info("Registering Refined Ammo Box items to other creative tab...");
        RefinedAmmoBoxItems.registerCreativeTab(event);
    }
}
