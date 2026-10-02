package com.nanaios.refined_ammo_box.client;

import com.nanaios.refined_ammo_box.RefinedAmmoBox;
import com.nanaios.refined_ammo_box.item.WirelessAmmoBoxItem;
import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@Mod(value = RefinedAmmoBox.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RefinedAmmoBox.MODID, value = Dist.CLIENT)
public class RefinedAmmoBoxClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (DeferredHolder<Item, ? extends Item> registry : RefinedAmmoBoxItems.WIRELESS_ITEMS.getEntries()) {
                ItemProperties.register(
                        registry.get(),
                        ResourceLocation.fromNamespaceAndPath(RefinedAmmoBox.MODID, "linked"),
                        RefinedAmmoBoxClient::isLighting
                );
            }
        });
    }

    public static float isLighting(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        if (stack.getItem() instanceof WirelessAmmoBoxItem item) {
            if (item.isLinked(stack) && item.getEnergyStored(stack) > 0) {
                return 1.0f;
            } else {
                return 0.0f;
            }
        }
        return 1.0f;
    }
}
