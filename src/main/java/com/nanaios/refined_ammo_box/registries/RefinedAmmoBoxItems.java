package com.nanaios.refined_ammo_box.registries;

import com.nanaios.refined_ammo_box.RefinedAmmoBox;
import com.nanaios.refined_ammo_box.item.CreativeWirelessAmmoBoxItem;
import com.nanaios.refined_ammo_box.item.IEnergyItem;
import com.nanaios.refined_ammo_box.item.TabIconItem;
import com.nanaios.refined_ammo_box.item.WirelessAmmoBoxItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RefinedAmmoBoxItems {
    public static final DeferredRegister.Items WIRELESS_ITEMS = DeferredRegister.createItems(RefinedAmmoBox.MODID);
    public static final DeferredRegister.Items FAKE_ITEMS = DeferredRegister.createItems(RefinedAmmoBox.MODID);

    /// タブのアイコンアイテム
    public static final DeferredItem<TabIconItem> ICON = FAKE_ITEMS.register("tab_icon", TabIconItem::new);

    /// アイテムの登録
    public static final DeferredItem<Item> AMMO_BOX = WIRELESS_ITEMS.register("ammo_box", WirelessAmmoBoxItem::new);
    public static final DeferredItem<Item> CREATIVE_AMMO_BOX = WIRELESS_ITEMS.register("creative_ammo_box", CreativeWirelessAmmoBoxItem::new);

    public static void registerCreativeTab(CreativeModeTab.Output output) {
        for (DeferredHolder<Item, ? extends Item> registry : WIRELESS_ITEMS.getEntries()) {
            Item item = registry.get();
            ItemStack baseStack = new ItemStack(item);
            output.accept(baseStack);

            // 満充電のアイテムも追加する
            if (item instanceof IEnergyItem energyItem && energyItem.canReceive(baseStack)) {
                ItemStack fullEnergyStack = baseStack.copy();
                int received = energyItem.receiveEnergy(fullEnergyStack, energyItem.getMaxEnergyStored(fullEnergyStack), false);
                if (received > 0 && !ItemStack.isSameItemSameComponents(baseStack, fullEnergyStack)) {
                    output.accept(fullEnergyStack);
                }
            }
        }
    }
}
