package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Tracers extends Module {
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool hostile = bool("Hostile", false);
    private final Setting.Bool passive = bool("Passive", false);
    private final Setting.Choice target = choice("Target", "Feet", "Feet", "Center", "Eyes");
    private final Setting.Choice color = choice("Color", "Team", "Team", "Distance", "Health", "Fixed");
    private final Setting.Num hue = num("Hue", 180, 0, 360, 10);
    private final Setting.Num width = num("Width", 1.5, 0.5, 4.0, 0.5);
    private final Setting.Num range = num("Range", 96, 8, 200, 8);

    public Tracers() { super("Tracers", "Draws lines to nearby entities.", Category.RENDER); }

    @Override public void onWorld() {
        if (!hasWorld()) return;
        float pt = UtilDraw.partial();
        Vec3 origin = UtilDraw.lineOrigin();
        double max = range.get();
        int count = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            int k = UtilDraw.kind(e);
            if (k == UtilDraw.NONE) continue;
            if (k == UtilDraw.PLAYER && !players.get()) continue;
            if (k == UtilDraw.HOSTILE && !hostile.get()) continue;
            if (k == UtilDraw.PASSIVE && !passive.get()) continue;
            double dist = Math.sqrt(e.distanceToSqr(mc.player));
            if (dist > max) continue;
            Vec3 p = e.getPosition(pt);
            double h = e.getBbHeight();
            double y = switch (target.get()) {
                case "Center" -> p.y + h * 0.5;
                case "Eyes" -> p.y + e.getEyeHeight();
                default -> p.y;
            };
            int rgb = UtilDraw.entityColor(e, color.get(), dist, max, hue.asFloat());
            UtilDraw.line(origin, new Vec3(p.x, y, p.z), rgb, width.asFloat(), true);
            if (++count >= 150) break;
        }
    }
}
