package com.nanaios.refined_ammo_box.util;

import com.nanaios.refined_ammo_box.registries.RefinedAmmoBoxDataComponents;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemPlayerValidator;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class RSLinkHelper {

    @Nullable
    public static GlobalPos getLinkedPosition(ItemStack stack) {
        return stack.get(RefinedAmmoBoxDataComponents.RS_LINK_TARGET);
    }

    public static boolean isValid(ItemStack stack) {
        return getLinkedPosition(stack) != null;
    }

    public static void putNode(ItemStack stack, GlobalPos globalPos) {
        stack.set(RefinedAmmoBoxDataComponents.RS_LINK_TARGET, globalPos);
    }

    @Nullable
    public static Network getNetwork(ItemStack stack) {
        GlobalPos linkPos = getLinkedPosition(stack);
        if (linkPos == null) return null;

        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(linkPos.dimension());
        if (level == null) return null;

        if (!level.isLoaded(linkPos.pos())) return null;

        BlockEntity blockEntity = level.getBlockEntity(linkPos.pos());
        if (blockEntity instanceof NetworkNodeContainerProvider provider) {
            for (InWorldNetworkNodeContainer container : provider.getContainers()) {
                NetworkNode node = container.getNode();
                Network network = node.getNetwork();
                if (network != null) {
                    return network;
                }
            }
        }
        return null;
    }

    public static boolean isInnerRange(Network network, Level level, BlockPos pos) {
        NetworkItemPlayerValidator.PlayerCoordinates coordinates =
                new NetworkItemPlayerValidator.PlayerCoordinates(level.dimension(), Vec3.atCenterOf(pos));

        GraphNetworkComponent graph = network.getComponent(GraphNetworkComponent.class);
        if (graph == null) return false;

        for (NetworkNodeContainer container : graph.getContainers()) {
            if (container instanceof NetworkItemPlayerValidator validator) {
                if (validator.isValid(coordinates)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ActionResult extractionAmmo(Level level, BlockPos pos, ItemStack ammoBox, ItemStack ammo, int count, Action mode) {
        if (!isValid(ammoBox)) {
            return new ActionResult(ActionResult.Status.DEVICE_NOT_LINKED, 0);
        }

        Network network = getNetwork(ammoBox);
        if (network == null) {
            return new ActionResult(ActionResult.Status.LINKED_NETWORK_NOT_FOUND, 0);
        }

        if (!isInnerRange(network, level, pos)) {
            return new ActionResult(ActionResult.Status.LINKED_NETWORK_NOT_FOUND, 0);
        }

        StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        if (storage == null) {
            return new ActionResult(ActionResult.Status.LINKED_NETWORK_NOT_FOUND, 0);
        }

        ItemResource itemResource = ItemResource.ofItemStack(ammo);
        long extracted = storage.extract(itemResource, count, mode, Actor.EMPTY);
        return new ActionResult(ActionResult.Status.SUCCESS, (int) Math.min(Integer.MAX_VALUE, extracted));
    }

    public record ActionResult(Status status, int count) {
        public enum Status {
            SUCCESS,
            DEVICE_NOT_LINKED,
            LINKED_NETWORK_NOT_FOUND
        }
    }
}
