package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

/**
 * Normal client-side inventory interaction.
 * Only fills an empty offhand and only when the standard inventory menu
 * is active. This avoids packet spam, displaced items, and container desync.
 */
public final class AutoTotem {
    private static int cooldown;

    private AutoTotem() {}

    public static void tick(Minecraft client) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (!ToolkitConfig.INSTANCE.autoTotem || client.player == null || client.gameMode == null
                || client.level == null || client.gui.screen() != null) return;
        var player = client.player;
        if (player.isSpectator() || !player.getOffhandItem().isEmpty()) return;
        if (player.containerMenu != player.inventoryMenu) return;
        var menu = player.inventoryMenu;
        if (!menu.getCarried().isEmpty()) return;

        for (Slot slot : menu.slots) {
            if (slot.container != player.getInventory()) continue;
            if (slot.index < 9 || slot.index > 44) continue;
            if (!slot.getItem().is(Items.TOTEM_OF_UNDYING)) continue;
            client.gameMode.handleContainerInput(menu.containerId, slot.index, 40, ContainerInput.SWAP, player);
            cooldown = 8; // Limit to one click while the server acknowledges the slot change.
            return;
        }
    }
}
