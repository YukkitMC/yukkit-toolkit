package dev.yukkit.toolkit.hud;

import dev.yukkit.toolkit.ToolkitConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * One registered HUD, with entity data sampled only once per 5 game ticks.
 * Rendering never scans the world and does not allocate per-frame entity lists.
 */
public final class ToolkitHud {
    private static final int BLUE = 0xFF006EFE;
    private static final int BG = 0xA9101521;
    private static final int WHITE = 0xFFF5F6FA;
    private static int ticks;
    private static List<String> tnt = List.of();
    private static List<String> friends = List.of();

    private ToolkitHud() {}

    public static void register() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("yukkit_toolkit", "hud"),
                (graphics, delta) -> render(graphics));
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            tnt = List.of();
            friends = List.of();
            TeamGlow.clear();
            return;
        }
        if (++ticks % 5 != 0) return;
        ToolkitConfig config = ToolkitConfig.INSTANCE;
        List<DistanceRow> tnts = new ArrayList<>();
        List<DistanceRow> nearFriends = new ArrayList<>();
        Set<String> teamSet = new HashSet<>();
        if (config.teamMates != null) {
            for (String name : config.teamMates) teamSet.add(name.toLowerCase(Locale.ROOT));
        }

        // Iterate only loaded client entities. Never request chunks or player coordinates.
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            double distanceSquared = entity.distanceToSqr(minecraft.player);
            if (config.tntTimer && entity instanceof PrimedTnt primed && distanceSquared <= 64.0 * 64.0) {
                tnts.add(new DistanceRow(distanceSquared, "TNT  " +
                        (Math.round(primed.getFuse() / 2.0) / 10.0) + "s  " +
                        (int) Math.sqrt(distanceSquared) + "m"));
            }
            if (entity instanceof Player player && entity != minecraft.player &&
                    teamSet.contains(player.getName().getString().toLowerCase(Locale.ROOT)) &&
                    distanceSquared <= 64.0 * 64.0) {
                nearFriends.add(new DistanceRow(distanceSquared,
                        player.getName().getString() + "  " + (int) Math.sqrt(distanceSquared) + "m"));
            }
        }
        tnts.sort(Comparator.comparingDouble(DistanceRow::distance));
        nearFriends.sort(Comparator.comparingDouble(DistanceRow::distance));
        tnt = tnts.stream().limit(3).map(DistanceRow::text).toList();
        friends = nearFriends.stream().limit(5).map(DistanceRow::text).toList();
        TeamGlow.tick(minecraft, teamSet);
    }

    private record DistanceRow(double distance, String text) {}

    private static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui.hud.isHidden()) return;
        ToolkitConfig config = ToolkitConfig.INSTANCE;
        if (config.keystrokes) renderKeystrokes(minecraft, graphics);
        int y = 12;
        if (config.tntTimer && !tnt.isEmpty()) {
            y = renderRows(minecraft, graphics, "TNT TIMER", tnt, y, 0xFFFFC55A);
        }
        if (config.teamHud && !friends.isEmpty()) {
            renderRows(minecraft, graphics, "TEAM NEARBY", friends, y + 6, BLUE);
        }
    }

    private static int renderRows(Minecraft mc, GuiGraphicsExtractor g, String title, List<String> rows, int y, int accent) {
        int x = Math.max(4, g.guiWidth() - 148);
        int w = Math.min(144, g.guiWidth() - x - 4);
        int height = 22 + 14 * rows.size();
        g.fill(x, y, x + w, y + height, BG);
        g.fill(x, y, x + 3, y + height, accent);
        g.text(mc.font, title, x + 9, y + 7, accent, true);
        for (int i = 0; i < rows.size(); i++) {
            g.text(mc.font, rows.get(i), x + 9, y + 22 + i * 14, WHITE, true);
        }
        return y + height;
    }

    private static void renderKeystrokes(Minecraft mc, GuiGraphicsExtractor g) {
        int x = 12, y = Math.max(15, g.guiHeight() - 116);
        key(mc, g, x + 27, y, "W", mc.options.keyUp.isDown());
        key(mc, g, x, y + 28, "A", mc.options.keyLeft.isDown());
        key(mc, g, x + 27, y + 28, "S", mc.options.keyDown.isDown());
        key(mc, g, x + 54, y + 28, "D", mc.options.keyRight.isDown());
        key(mc, g, x, y + 56, "LMB", mc.options.keyAttack.isDown());
        key(mc, g, x + 42, y + 56, "RMB", mc.options.keyUse.isDown());
        g.fill(x, y + 84, x + 80, y + 95, mc.options.keyJump.isDown() ? BLUE : BG);
        g.text(mc.font, "SPACE", x + 20, y + 86, WHITE, true);
    }

    private static void key(Minecraft mc, GuiGraphicsExtractor g, int x, int y, String label, boolean pressed) {
        int width = label.length() == 3 ? 38 : 25;
        g.fill(x, y, x + width, y + 25, pressed ? BLUE : BG);
        g.text(mc.font, label, x + (width - mc.font.width(label)) / 2, y + 9, WHITE, true);
    }
}
