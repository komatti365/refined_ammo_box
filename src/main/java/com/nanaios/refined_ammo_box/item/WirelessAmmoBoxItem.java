package com.nanaios.refined_ammo_box.item;

import com.nanaios.refined_ammo_box.RefinedAmmoBoxLang;
import com.nanaios.refined_ammo_box.config.RefinedAmmoBoxConfig;
import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxDataComponents;
import com.nanaios.refined_ammo_box.util.RSLinkHelper;
import com.nanaios.refined_ammo_box.util.RSLinkHelper.ActionResult;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.item.AmmoBoxItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WirelessAmmoBoxItem extends AmmoBoxItem implements ILinkableItem, IEnergyItem, ITimeStamp {

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isCrouching()) {
            if (!level.isClientSide) {
                clearAmmoData(stack);
                player.displayClientMessage(RefinedAmmoBoxLang.CLEAR_AMMO_DATA.get(), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (this.getEnergyStored(stack) <= 0) {
            super.setAmmoCount(stack, 0);
            setAmmoId(stack, DefaultAssets.EMPTY_AMMO_ID);
            return;
        }

        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;

        ItemStack iGunStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean isUpdate = updateAmmoId(stack, iGunStack);

        if (isWantUpdate(stack) || isUpdate) {
            setPos(stack, player.blockPosition());
            setLevel(stack, level);
            setTimeStamp(stack, System.currentTimeMillis());

            ActionResult result = updateAmmoCount(stack);

            if (result.status() == ActionResult.Status.LINKED_NETWORK_NOT_FOUND) {
                player.displayClientMessage(RefinedAmmoBoxLang.NETWORK_NOT_FOUND.get(), true);
            }
        }
    }

    public void setLevel(ItemStack stack, Level level) {
        stack.set(RefinedAmmoBoxDataComponents.AMMO_BOX_LEVEL, level.dimension().location().toString());
    }

    @Nullable
    public Level getLevel(ItemStack stack) {
        String levelStr = stack.getOrDefault(RefinedAmmoBoxDataComponents.AMMO_BOX_LEVEL, "minecraft:overworld");
        return ServerLifecycleHooks.getCurrentServer().getLevel(
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(levelStr))
        );
    }

    public void setPos(ItemStack stack, BlockPos pos) {
        stack.set(RefinedAmmoBoxDataComponents.AMMO_BOX_BLOCK_POS, pos.asLong());
    }

    @Nullable
    public BlockPos getPos(ItemStack stack) {
        return BlockPos.of(stack.getOrDefault(RefinedAmmoBoxDataComponents.AMMO_BOX_BLOCK_POS, 0L));
    }

    public boolean isWantUpdate(ItemStack stack) {
        return (System.currentTimeMillis() - getTimeStamp(stack)) > 1000;
    }

    public boolean updateAmmoId(ItemStack ammoBox, ItemStack gunStack) {
        if (!(gunStack.getItem() instanceof IGun gun)) return false;

        ResourceLocation ammoId = TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack))
                .map(commonGunIndex -> commonGunIndex.getGunData().getAmmoId())
                .orElse(DefaultAssets.EMPTY_AMMO_ID);

        if (ammoId.equals(getAmmoId(ammoBox))) return false;

        setAmmoId(ammoBox, ammoId);
        return true;
    }

    public ActionResult updateAmmoCount(ItemStack stack) {
        ItemStack ammo = AmmoItemBuilder.create().setId(getAmmoId(stack)).setCount(1).build();

        BlockPos pos = getPos(stack);
        Level level = getLevel(stack);
        if (level == null || pos == null) return new ActionResult(ActionResult.Status.LINKED_NETWORK_NOT_FOUND, 0);

        ActionResult result = RSLinkHelper.extractionAmmo(level, pos, stack, ammo, Integer.MAX_VALUE, Action.SIMULATE);
        super.setAmmoCount(stack, result.count());
        this.setLinked(stack, result.status() == ActionResult.Status.SUCCESS);

        return result;
    }

    @Override
    public boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
        if (getAmmoCount(ammoBox) <= 0) return false;
        return super.isAmmoBoxOfGun(gun, ammoBox);
    }

    @Override
    public void setAmmoCount(ItemStack ammoBox, int count) {
        int oldCount = this.getAmmoCount(ammoBox);
        int diff = oldCount - count;
        if (diff <= 0) return;

        BlockPos pos = getPos(ammoBox);
        Level level = getLevel(ammoBox);
        if (level == null || pos == null) return;

        ItemStack ammo = AmmoItemBuilder.create().setId(getAmmoId(ammoBox)).setCount(1).build();
        RSLinkHelper.extractionAmmo(level, pos, ammoBox, ammo, diff, Action.EXECUTE);

        this.extractEnergy(ammoBox, RefinedAmmoBoxConfig.AMMO_BOX_USE_POWER_PER_AMMO.get() * diff, false);
        updateAmmoCount(ammoBox);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return RefinedAmmoBoxLang.AMMO_BOX_NAME.get();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack ammoBox, Slot slot, ClickAction action, Player player) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float max = getMaxEnergyStored(stack);
        if (max <= 0) return 0;
        float stored = (float) getEnergyStored(stack) / max;
        return Math.round(stored * 13F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float max = getMaxEnergyStored(stack);
        float stored = max > 0 ? (float) getEnergyStored(stack) / max : 0;
        return Mth.hsvToRgb(Math.max(0.0F, stored) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.PASS;

        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(ctx.getHand());

        BlockEntity blockEntity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (blockEntity instanceof NetworkNodeContainerProvider) {
            GlobalPos globalPos = GlobalPos.of(ctx.getLevel().dimension(), ctx.getClickedPos());
            RSLinkHelper.putNode(stack, globalPos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(RefinedAmmoBoxLang.ENERGY_STORED.get(getEnergyStored(stack), getMaxEnergyStored(stack)).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        GlobalPos linkedPos = RSLinkHelper.getLinkedPosition(stack);
        if (linkedPos != null) {
            tooltip.add(RefinedAmmoBoxLang.NETWORK_TOOL_TIP.get(
                    linkedPos.pos().getX(),
                    linkedPos.pos().getY(),
                    linkedPos.pos().getZ()
            ).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }
    }

    public void clearAmmoData(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }
}
