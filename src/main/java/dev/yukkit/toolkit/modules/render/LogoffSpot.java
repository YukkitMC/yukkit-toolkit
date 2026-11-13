package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Remembers where players were standing when they disconnected nearby. */
public final class LogoffSpot extends Module {
    private final Setting.Num seconds = num("Seconds", 60, 5, 600, 5);
    private final Setting.Num maxDist = num("MaxDistance", 64, 8, 128, 8);
    private final Setting.Bool chat = bool("Chat", true);

    private static final class Seen {
        String name; AABB box; double dist; int missing;
    }

    private static final class Spot {
        String name; AABB box; int ticks;
    }

    private final Map<UUID, Seen> seen = new HashMap<>();
    private final Map<UUID, Seen> pending = new HashMap<>();
    private final List<Spot> spots = new ArrayList<>();

    public LogoffSpot() { super("LogoffSpot", "Marks where players logged off.", Category.RENDER); }

    private void clear() { seen.clear(); pending.clear(); spots.clear(); }

    @Override public void onEnable() { clear(); }
    @Override public void onDisable() { clear(); }
    @Override public void onWorldChange() { clear(); }

    @Override public void onTick() {
        if (!hasWorld() || mc.getConnection() == null) return;
        java.util.Set<UUID> now = new java.util.HashSet<>();
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            UUID id = p.getUUID();
            now.add(id);
            pending.remove(id);
            Seen s = seen.computeIfAbsent(id, k -> new Seen());
            s.name = p.getName().getString();
            s.box = p.getBoundingBox();
            s.dist = Math.sqrt(p.distanceToSqr(mc.player));
        }
        // Players that vanished from the level: logoff if they also left the tab list.
        Iterator<Map.Entry<UUID, Seen>> it = seen.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Seen> e = it.next();
            if (!now.contains(e.getKey())) {
                pending.put(e.getKey(), e.getValue());
                it.remove();
            }
        }
        Iterator<Map.Entry<UUID, Seen>> pi = pending.entrySet().iterator();
        while (pi.hasNext()) {
            Map.Entry<UUID, Seen> e = pi.next();
            Seen s = e.getValue();
            boolean listed = mc.getConnection().getPlayerInfo(e.getKey()) != null;
            if (!listed) {
                if (s.dist <= maxDist.get()) {
                    Spot sp = new Spot();
                    sp.name = s.name;
                    sp.box = s.box;
                    spots.add(sp);
                    if (chat.get()) mc.player.sendSystemMessage(
                            Component.literal("[LogoffSpot] " + s.name + " logged off at "
                                    + (int) s.box.getCenter().x + ", " + (int) s.box.minY + ", " + (int) s.box.getCenter().z));
                }
                pi.remove();
            } else if (++s.missing > 40) {
                pi.remove(); // simply moved out of render distance
            }
        }
        int life = seconds.asInt() * 20;
        spots.removeIf(sp -> ++sp.ticks > life);
        if (spots.size() > 50) spots.remove(0);
    }

    @Override public void onWorld() {
        for (Spot sp : spots) UtilDraw.box(sp.box, 0xFFFFFF, "Both", true);
    }

    @Override public String tag() { return Integer.toString(spots.size()); }
}
