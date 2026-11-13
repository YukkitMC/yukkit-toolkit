package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class Breadcrumbs extends Module {
    private final Setting.Num seconds = num("Seconds", 10, 1, 60, 1);
    private final Setting.Choice color = choice("Color", "Rainbow", "Rainbow", "Solid");
    private final Setting.Num hue = num("Hue", 200, 0, 360, 10);
    private final Setting.Num width = num("Width", 2.0, 0.5, 5.0, 0.5);
    private final Setting.Bool walls = bool("ThroughWalls", true);

    private final Deque<Vec3> trail = new ArrayDeque<>();
    private Vec3 last;

    public Breadcrumbs() { super("Breadcrumbs", "Leaves a trail behind you.", Category.RENDER); }

    @Override public void onEnable() { trail.clear(); last = null; }
    @Override public void onDisable() { trail.clear(); last = null; }
    @Override public void onWorldChange() { trail.clear(); last = null; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        Vec3 now = mc.player.position().add(0, 0.05, 0);
        if (last == null || last.distanceToSqr(now) > 0.01) {
            trail.addLast(now);
            last = now;
        }
        int max = seconds.asInt() * 20;
        while (trail.size() > max) trail.pollFirst();
    }

    @Override public void onWorld() {
        if (trail.size() < 2) return;
        int n = trail.size();
        int i = 0;
        Vec3 prev = null;
        Iterator<Vec3> it = trail.iterator();
        while (it.hasNext()) {
            Vec3 p = it.next();
            if (prev != null && prev.distanceToSqr(p) < 100.0) {
                int rgb = color.is("Rainbow") ? UtilDraw.hue(i * 4f) : UtilDraw.hue(hue.asFloat());
                UtilDraw.line(prev, p, rgb, width.asFloat(), walls.get());
            }
            prev = p;
            i++;
        }
    }
}
