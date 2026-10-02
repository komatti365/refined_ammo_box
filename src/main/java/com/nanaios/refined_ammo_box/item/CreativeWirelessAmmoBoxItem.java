package com.nanaios.refined_ammo_box.item;

import com.nanaios.refined_ammo_box.RefinedAmmoBoxLang;
import com.nanaios.refined_ammo_box.util.RSLinkHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreativeWirelessAmmoBoxItem extends WirelessAmmoBoxItem {
    @Override
    public int getEnergyStored(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        GlobalPos linkedPos = RSLinkHelper.getLinkedPosition(stack);
        if (linkedPos != null) {
            tooltip.add(RefinedAmmoBoxLang.NETWORK_TOOL_TIP.get(
                    linkedPos.pos().getX(),
                    linkedPos.pos().getY(),
                    linkedPos.pos().getZ()
            ).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return RefinedAmmoBoxLang.CREATIVE_WIRELESS_AMMO_BOX_NAME.get().withStyle(ChatFormatting.DARK_PURPLE);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        return maxExtract;
    }

    @Override
    public int getMaxEnergyStored(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canExtract(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canReceive(ItemStack stack) {
        return false;
    }
}