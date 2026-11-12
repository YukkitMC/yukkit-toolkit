package dev.yukkit.toolkit.team;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Handles premium and offline-mode player names without contacting Mojang. */
public final class TeamNames {
    private static Set<String> names = Set.of();

    private TeamNames() {}

    public static void refresh() {
        Set<String> next = new HashSet<>();
        if (ToolkitConfig.INSTANCE.teamMates != null) {
            for (String name : ToolkitConfig.INSTANCE.teamMates) {
                String normalized = normalize(name);
                if (!normalized.isBlank()) next.add(normalized);
            }
        }
        names = Set.copyOf(next);
    }

    public static String normalize(String value) {
        if (value == null) return "";
        // Strip Minecraft § color codes and compare case-insensitively.
        return value.replaceAll("(?i)§[0-9a-fk-or]", "").strip().toLowerCase(Locale.ROOT);
    }

    public static boolean matches(Player player) {
        if (player == null || names.isEmpty()) return false;
        String actual = normalize(player.getName().getString());
        return names.contains(actual) || names.contains(normalize(player.getScoreboardName()));
    }
}
