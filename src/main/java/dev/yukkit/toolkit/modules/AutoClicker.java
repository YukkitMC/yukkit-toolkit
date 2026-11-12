package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Hold left click while looking at an entity. No target acquisition,
 * aim assist, reach changes, or action when a GUI is open.
 */
public final class AutoClicker {
    private static int cooldown;

    private AutoClicker() {}

    public static void tick(Minecraft client) {
        if (cooldown > 0) cooldown--;
        if (!ToolkitConfig.INSTANCE.autoClicker || client.level == null || client.player == null
                || client.gameMode == null || client.gui.screen() != null
                || !client.options.keyAttack.isDown() || !(client.hitResult instanceof EntityHitResult hit)) return;
        if (client.player.isSpectator() || client.player.isUsingItem()) return;
        if (hit.getEntity() == client.player ||
                hit.getEntity().distanceToSqr(client.player) > Math.pow(client.player.entityInteractionRange(), 2)) return;
        if (cooldown > 0 || client.player.getAttackStrengthScale(0.0f) < 0.90f) return;

        client.gameMode.attack(client.player, hit.getEntity());
        client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        cooldown = Math.max(1, Math.round(20.0f / Math.max(2, ToolkitConfig.INSTANCE.clickCps)));
    }
}
