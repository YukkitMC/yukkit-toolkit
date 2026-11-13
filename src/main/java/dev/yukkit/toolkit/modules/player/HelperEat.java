package dev.yukkit.toolkit.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * Small state machine that selects a hotbar slot, holds the use key until the item has been
 * consumed, then puts the previous slot and key state back.
 */
final class HelperEat {
    private int prevSlot = -1;
    private int slot = -1;
    private int age;
    private boolean started;
    private boolean holding;

    boolean active() { return slot >= 0; }

    boolean start(int hotbarSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || active()) return false;
        prevSlot = mc.player.getInventory().getSelectedSlot();
        slot = hotbarSlot;
        age = 0;
        started = false;
        mc.player.getInventory().setSelectedSlot(hotbarSlot);
        return true;
    }

    void tick() {
        if (!active()) return;
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) { reset(false); return; }
        age++;
        if (p.getInventory().getSelectedSlot() != slot) { reset(false); return; }
        if (!started) {
            if (age < 2) return;
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            mc.options.keyUse.setDown(true);
            holding = true;
            started = true;
            age = 0;
            return;
        }
        if (p.isUsingItem()) {
            if (age > 100) reset(true);
            return;
        }
        if (age >= 2) reset(true);
    }

    void abort() { reset(true); }

    private void reset(boolean restoreSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (holding) mc.options.keyUse.setDown(false);
        if (restoreSlot && mc.player != null && prevSlot >= 0 && mc.player.getInventory().getSelectedSlot() == slot) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
        prevSlot = -1;
        slot = -1;
        age = 0;
        started = false;
        holding = false;
    }
}
