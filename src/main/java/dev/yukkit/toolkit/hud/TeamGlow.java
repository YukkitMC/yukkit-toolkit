package dev.yukkit.toolkit.hud;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Local-only, opt-in highlighting for loaded nearby teammates. */
public final class TeamGlow {
    private static final Map<UUID, Player> applied = new HashMap<>();
    private static Object lastLevel;

    private TeamGlow() {}

    public static void clear() {
        for (Player player : applied.values()) {
            if (!player.isRemoved()) player.setGlowingTag(false);
        }
        applied.clear();
        lastLevel = null;
    }

    public static void tick(Minecraft client, Set<String> teamNames) {
        if (client.level != lastLevel) {
            clear();
            lastLevel = client.level;
        }
        if (!ToolkitConfig.INSTANCE.teamGlow || teamNames.isEmpty() || client.player == null) {
            clear();
            lastLevel = client.level;
            return;
        }
        Set<UUID> seen = new HashSet<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Player player) || player == client.player) continue;
            String name = player.getName().getString().toLowerCase(Locale.ROOT);
            if (!teamNames.contains(name) || player.distanceToSqr(client.player) > 32 * 32) continue;
            UUID uuid = player.getUUID();
            seen.add(uuid);
            // Do not overwrite server-side glowing already present on this player.
            if (!applied.containsKey(uuid) && !player.hasGlowingTag()) {
                player.setGlowingTag(true);
                applied.put(uuid, player);
            }
        }
        for (var iterator = applied.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            if (!seen.contains(entry.getKey()) || entry.getValue().isRemoved()) {
                if (!entry.getValue().isRemoved()) entry.getValue().setGlowingTag(false);
                iterator.remove();
            }
        }
    }
}
