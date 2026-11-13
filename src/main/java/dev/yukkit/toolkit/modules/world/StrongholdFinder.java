package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Watches thrown eyes of ender, remembers the line each one flew along and triangulates
 * where those lines cross, which is where the stronghold should be.
 */
public final class StrongholdFinder extends Module {
    private static final int MAX_LINES = 8;
    private static final int SETTLE_TICKS = 8;

    private final Setting.Bool lines = bool("Draw lines", true);
    private final Setting.Bool chat = bool("Chat message", true);
    private final Setting.Bool hud = bool("HUD text", true);

    /** A thrown eye: where it appeared and, once it has flown a bit, its horizontal heading. */
    private static final class Throw {
        final Vec3 origin;
        int age;
        double dirX, dirZ;
        boolean ready;

        Throw(Vec3 origin) { this.origin = origin; }
    }

    private final Map<Integer, Throw> tracking = new HashMap<>();
    private final List<Throw> finished = new ArrayList<>();
    private Vec3 estimate;
    private long lastAnnounced = Long.MIN_VALUE;

    public StrongholdFinder() {
        super("StrongholdFinder", "Tracks Eye of Ender throws and predicts the stronghold position.", Category.WORLD);
    }

    @Override public void onEnable() { reset(); }

    @Override public void onDisable() { reset(); }

    @Override public void onWorldChange() { reset(); }

    private void reset() {
        tracking.clear();
        finished.clear();
        estimate = null;
        lastAnnounced = Long.MIN_VALUE;
    }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EyeOfEnder eye)) continue;
            Throw t = tracking.get(eye.getId());
            if (t == null) {
                tracking.put(eye.getId(), new Throw(eye.position()));
                continue;
            }
            if (t.ready) continue;
            t.age++;
            Vec3 now = eye.position();
            double dx = now.x - t.origin.x;
            double dz = now.z - t.origin.z;
            double len = Math.hypot(dx, dz);
            if (t.age >= SETTLE_TICKS && len > 1.0) {
                t.dirX = dx / len;
                t.dirZ = dz / len;
                t.ready = true;
                finished.add(t);
                if (finished.size() > MAX_LINES) finished.remove(0);
                recompute();
            }
        }
    }

    private void recompute() {
        double sumX = 0, sumZ = 0;
        int n = 0;
        for (int i = 0; i < finished.size(); i++) {
            for (int j = i + 1; j < finished.size(); j++) {
                Throw a = finished.get(i);
                Throw b = finished.get(j);
                double cross = a.dirX * b.dirZ - a.dirZ * b.dirX;
                if (Math.abs(cross) < 0.08) continue; // nearly parallel, too imprecise
                double ox = b.origin.x - a.origin.x;
                double oz = b.origin.z - a.origin.z;
                double ta = (ox * b.dirZ - oz * b.dirX) / cross;
                double tb = (ox * a.dirZ - oz * a.dirX) / cross;
                if (ta < 0 || tb < 0) continue; // crossing is behind one of the throws
                sumX += a.origin.x + a.dirX * ta;
                sumZ += a.origin.z + a.dirZ * ta;
                n++;
            }
        }
        if (n == 0) return;
        estimate = new Vec3(sumX / n, mc.player.getY(), sumZ / n);
        long chunk = ((long) ((int) Math.floor(estimate.x) >> 4) << 32) ^ (((int) Math.floor(estimate.z) >> 4) & 0xFFFFFFFFL);
        if (chat.get() && chunk != lastAnnounced) {
            lastAnnounced = chunk;
            mc.player.sendSystemMessage(Component.literal(String.format(java.util.Locale.ROOT,
                    "[StrongholdFinder] Stronghold near x=%d z=%d (chunk %d, %d)",
                    (int) Math.floor(estimate.x), (int) Math.floor(estimate.z),
                    (int) Math.floor(estimate.x) >> 4, (int) Math.floor(estimate.z) >> 4)));
        }
    }

    @Override
    public void onHud(GuiGraphicsExtractor g) {
        if (!hud.get() || !hasWorld()) return;
        int x = 6;
        int y = g.guiHeight() / 2 - 12;
        if (estimate != null) {
            double dist = Math.hypot(estimate.x - mc.player.getX(), estimate.z - mc.player.getZ());
            g.text(mc.font, String.format(java.util.Locale.ROOT, "Stronghold: x=%d z=%d (%d blocks)",
                    (int) Math.floor(estimate.x), (int) Math.floor(estimate.z), (int) dist), x, y, 0xFFB7F5A0, true);
            g.text(mc.font, "Chunk " + (((int) Math.floor(estimate.x)) >> 4) + ", " + (((int) Math.floor(estimate.z)) >> 4),
                    x, y + 11, 0xFFDDDDDD, true);
        } else if (finished.size() == 1) {
            Throw t = finished.get(0);
            double yaw = Math.toDegrees(Math.atan2(-t.dirX, t.dirZ));
            g.text(mc.font, String.format(java.util.Locale.ROOT, "Eye heading: yaw %.1f - throw again from another spot", yaw),
                    x, y, 0xFFF5E0A0, true);
        } else {
            g.text(mc.font, "Throw an Eye of Ender", x, y, 0xFFDDDDDD, true);
        }
    }

    @Override
    public void onWorld() {
        if (!lines.get() || !hasWorld()) return;
        for (Throw t : finished) {
            Vec3 end = new Vec3(t.origin.x + t.dirX * 400.0, t.origin.y, t.origin.z + t.dirZ * 400.0);
            Gizmos.line(t.origin, end, 0xFFFFD060, 2.0f).setAlwaysOnTop();
        }
        if (estimate != null) {
            Vec3 base = new Vec3(estimate.x, mc.player.getY() - 30.0, estimate.z);
            Vec3 top = new Vec3(estimate.x, mc.player.getY() + 80.0, estimate.z);
            Gizmos.line(base, top, 0xFF60FF90, 3.0f).setAlwaysOnTop();
        }
    }

    @Override
    public String tag() {
        return estimate != null ? (((int) Math.floor(estimate.x)) + ", " + ((int) Math.floor(estimate.z))) : "";
    }
}
