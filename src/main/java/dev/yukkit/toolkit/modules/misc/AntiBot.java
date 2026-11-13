package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Flags player entities that are probably not real players (server-side NPCs, hologram holders,
 * fake players used by anti-cheats). Other modules ask {@link #isBot(Entity)}.
 */
public final class AntiBot extends Module {
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static volatile Set<Integer> botIds = Set.of();

    private final Setting.Bool tabList = bool("Tab List", true);
    private final Setting.Bool invalidName = bool("Invalid Name", true);
    private final Setting.Bool duplicate = bool("Duplicate Name", true);
    private final Setting.Bool zeroPing = bool("Zero Ping", false);
    private final Setting.Num grace = num("Grace Ticks", 20, 0, 100, 5);

    public AntiBot() {
        super("AntiBot", "Detects fake players (NPCs) so other modules can ignore them.", Category.MISC);
    }

    /** True when the entity is a player that this module considers fake. False while the module is off. */
    public static boolean isBot(Entity entity) {
        return entity instanceof Player && botIds.contains(entity.getId());
    }

    @Override
    public void onDisable() { botIds = Set.of(); }

    @Override
    public void onWorldChange() { botIds = Set.of(); }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.getConnection() == null) {
            botIds = Set.of();
            return;
        }
        List<Player> players = new java.util.ArrayList<>();
        Map<String, List<Player>> byName = new HashMap<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof Player p && p != mc.player) {
                players.add(p);
                byName.computeIfAbsent(p.getName().getString(), k -> new java.util.ArrayList<>()).add(p);
            }
        }

        Set<Integer> found = new HashSet<>();
        int graceTicks = grace.asInt();
        for (Player p : players) {
            if (p.tickCount < graceTicks) continue;
            UUID id = p.getUUID();
            var info = mc.getConnection().getPlayerInfo(id);
            String name = p.getName().getString();

            if (tabList.get() && info == null) found.add(p.getId());
            else if (zeroPing.get() && info != null && info.getLatency() == 0 && p.tickCount > 100) found.add(p.getId());
            if (invalidName.get() && !VALID_NAME.matcher(name).matches()) found.add(p.getId());
        }
        if (duplicate.get()) {
            for (List<Player> group : byName.values()) {
                if (group.size() < 2) continue;
                // keep the entity that is backed by a tab list entry; flag the others
                boolean keptOne = false;
                for (Player p : group) {
                    boolean real = mc.getConnection().getPlayerInfo(p.getUUID()) != null;
                    if (real && !keptOne) { keptOne = true; continue; }
                    if (p.tickCount >= graceTicks) found.add(p.getId());
                }
            }
        }
        botIds = found;
    }

    @Override
    public String tag() { return botIds.isEmpty() ? "" : Integer.toString(botIds.size()); }
}
