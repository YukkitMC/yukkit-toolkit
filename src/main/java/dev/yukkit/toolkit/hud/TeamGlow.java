package dev.yukkit.toolkit.hud;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.team.TeamNames;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Render-only teammate outline. No entity flags are changed, so updates
 * from the server cannot erase our local choice.
 */
public final class TeamGlow {
    private TeamGlow() {}

    public static void clear() {}

    public static boolean shouldGlow(Player player) {
        Minecraft mc = Minecraft.getInstance();
        return ToolkitConfig.INSTANCE.teamGlow &&
                mc != null && mc.player != null && mc.level != null &&
                player != mc.player && player.level() == mc.level &&
                player.distanceToSqr(mc.player) <= (double) ToolkitConfig.INSTANCE.teamGlowRange * ToolkitConfig.INSTANCE.teamGlowRange &&
                TeamNames.matches(player);
    }

    // Kept for older call sites; TeamNames matching is refreshed periodically.
    public static void tick(Minecraft mc, java.util.Set<String> ignored) {
        TeamNames.refresh();
    }
}
