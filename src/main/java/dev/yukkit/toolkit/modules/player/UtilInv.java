package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Shared inventory helpers for the player modules. */
final class UtilInv {
    private UtilInv() {}

    static boolean ready() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.level != null && mc.gameMode != null;
    }

    /** Sends one container click through the normal client path. */
    static void click(AbstractContainerMenu menu, int slot, int button, ContainerInput type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.handleContainerInput(menu.containerId, slot, button, type, mc.player);
    }

    /** Menu slot index (in the player's own inventory menu) for a hotbar slot 0-8. */
    static int hotbarMenuSlot(int hotbarIndex) { return 36 + hotbarIndex; }

    /** First hotbar slot whose stack matches, or -1. */
    static int findHotbar(Player p, Predicate<ItemStack> test) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && test.test(s)) return i;
        }
        return -1;
    }

    /** True when the menu is a plain storage container (chest, barrel, shulker box, hopper, dispenser). */
    static boolean isStorageMenu(Player p, AbstractContainerMenu menu) {
        if (menu == null || menu == p.inventoryMenu) return false;
        try {
            MenuType<?> t = menu.getType();
            return t == MenuType.GENERIC_9x1 || t == MenuType.GENERIC_9x2 || t == MenuType.GENERIC_9x3
                    || t == MenuType.GENERIC_9x4 || t == MenuType.GENERIC_9x5 || t == MenuType.GENERIC_9x6
                    || t == MenuType.SHULKER_BOX || t == MenuType.HOPPER || t == MenuType.GENERIC_3x3;
        } catch (Throwable t) {
            return false;
        }
    }

    /** True when the player's main inventory or hotbar can accept (part of) this stack. */
    static boolean hasRoomFor(Player p, ItemStack stack) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.isEmpty()) return true;
            if (s.getCount() < s.getMaxStackSize() && ItemStack.isSameItemSameComponents(s, stack)) return true;
        }
        return false;
    }

    /**
     * Uses an item while looking straight down (or any pitch) and then restores both the client
     * rotation and the rotation the server believes in. {@code hotbarSlot < 0} means the offhand.
     */
    static void useAimed(Player p, int hotbarSlot, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return;
        var inv = p.getInventory();
        int prevSlot = inv.getSelectedSlot();
        float prevPitch = p.getXRot();
        InteractionHand hand = hotbarSlot < 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (hotbarSlot >= 0) inv.setSelectedSlot(hotbarSlot);
        p.setXRot(pitch);
        try {
            mc.gameMode.useItem(p, hand);
        } finally {
            p.setXRot(prevPitch);
            if (hotbarSlot >= 0) inv.setSelectedSlot(prevSlot);
        }
        ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Rot(p.getYRot(), prevPitch, p.onGround(), p.horizontalCollision));
    }
}
