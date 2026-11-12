package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Preselects a hotbar mace while falling toward an entity under the crosshair.
 * Restores the original hotbar slot when the target or fall is lost.
 * No forced attack, spoofed inventory, or reach modification.
 */
public final class MaceSwap {
    private static int previousSlot = -1;
    private static int maceSlot = -1;
    private static Object previousLevel;

    private MaceSwap() {}

    public static void reset(Minecraft client) {
        if (client.player != null && previousSlot >= 0 && maceSlot >= 0) {
            var inventory = client.player.getInventory();
            if (inventory.getSelectedSlot() == maceSlot) inventory.setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
        maceSlot = -1;
    }

    public static void tick(Minecraft client) {
        if (client.level != previousLevel) {
            reset(client);
            previousLevel = client.level;
        }
        if (client.player == null || client.gameMode == null || client.level == null ||
                !ToolkitConfig.INSTANCE.maceSwap || client.gui.screen() != null ||
                client.player.isSpectator() || client.player.isUsingItem() ||
                client.player.onGround() || client.player.fallDistance < 1.5 ||
                !(client.hitResult instanceof EntityHitResult hit) ||
                hit.getEntity() == client.player ||
                hit.getEntity().distanceToSqr(client.player) > 36.0) {
            reset(client);
            return;
        }

        var inventory = client.player.getInventory();
        if (previousSlot >= 0) {
            if (inventory.getSelectedSlot() != maceSlot) reset(client); // User changed slot manually.
            return;
        }
        if (inventory.getSelectedItem().is(Items.MACE)) return;
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.getItem(slot).is(Items.MACE)) {
                previousSlot = inventory.getSelectedSlot();
                maceSlot = slot;
                inventory.setSelectedSlot(slot);
                return;
            }
        }
    }
}
