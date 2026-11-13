package dev.yukkit.toolkit.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** Estimates per-tick entity velocities from position changes (remote entities report none). */
final class UtilMotion {
    private static final Minecraft mc = Minecraft.getInstance();
    private static Map<Integer, Vec3> positions = new HashMap<>();
    private static final Map<Integer, Vec3> velocities = new HashMap<>();
    private static Object lastLevel;
    private static long lastTick = Long.MIN_VALUE;

    private UtilMotion() {}

    /** Safe to call from several modules in the same tick; only the first call does work. */
    static void update() {
        if (mc.level == null) {
            positions.clear();
            velocities.clear();
            lastLevel = null;
            return;
        }
        if (mc.level != lastLevel) {
            positions.clear();
            velocities.clear();
            lastLevel = mc.level;
            lastTick = Long.MIN_VALUE;
        }
        long now = mc.level.getGameTime();
        if (now == lastTick) return;
        lastTick = now;
        Map<Integer, Vec3> next = new HashMap<>();
        velocities.clear();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity)) continue;
            Vec3 pos = e.position();
            next.put(e.getId(), pos);
            Vec3 old = positions.get(e.getId());
            if (old != null) velocities.put(e.getId(), pos.subtract(old));
        }
        positions = next;
    }

    static Vec3 velocity(Entity e) {
        Vec3 v = velocities.get(e.getId());
        return v == null ? Vec3.ZERO : v;
    }
}
