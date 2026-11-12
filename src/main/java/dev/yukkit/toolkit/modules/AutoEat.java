package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

/**
 * Client-side golden carrot eating below a configurable hunger threshold.
 * Temporarily switches to a golden carrot in the hotbar and restores the
 * previous slot when the eating animation ends or is interrupted.
 */
public final class AutoEat {
    private static int previousSlot = -1;
    private static int foodSlot = -1;
    private static int cooldown;
    private static int activeTicks;
    private static Object previousLevel;

    private AutoEat() {}

    private static void restore(Minecraft client) {
        if (client.player != null && previousSlot >= 0 && foodSlot >= 0) {
            var inventory = client.player.getInventory();
            if (inventory.getSelectedSlot() == foodSlot) inventory.setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
        foodSlot = -1;
        activeTicks = 0;
    }

    public static void tick(Minecraft mc) {
        if (mc.level != previousLevel) {
            restore(mc);
            previousLevel = mc.level;
        }
        if (cooldown > 0) cooldown--;
        if (mc.player == null || mc.level == null || mc.gameMode == null ||
                !ToolkitConfig.INSTANCE.autoEat || mc.gui.screen() != null ||
                mc.player.isSpectator() || mc.options.keyUse.isDown()) {
            restore(mc);
            return;
        }

        var player = mc.player;
        var inv = player.getInventory();
        if (previousSlot >= 0) {
            activeTicks++;
            if (inv.getSelectedSlot() != foodSlot || !inv.getSelectedItem().is(Items.GOLDEN_CARROT)) {
                restore(mc);
                cooldown = 20;
                return;
            }
            if (activeTicks < 3) return; // Allow the selected slot to reach the server.
            if (player.isUsingItem()) return;

            // Start once. Wait for the normal eating animation to finish.
            if (activeTicks == 3) {
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                return;
            }
            restore(mc);
            cooldown = 25;
            return;
        }

        if (cooldown != 0 || player.isUsingItem() ||
                player.getFoodData().getFoodLevel() > ToolkitConfig.INSTANCE.eatThreshold) return;
        if (player.getFoodData().getFoodLevel() >= 20) return;

        for (int slot = 0; slot < 9; slot++) {
            if (!inv.getItem(slot).is(Items.GOLDEN_CARROT)) continue;
            previousSlot = inv.getSelectedSlot();
            foodSlot = slot;
            inv.setSelectedSlot(slot);
            activeTicks = 0;
            return;
        }
    }
}
