package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class ESP extends Module {
    private final Setting_ unused = null;
    private final dev.yukkit.toolkit.core.Setting.Bool players = bool("Players", true);
    private final dev.yukkit.toolkit.core.Setting.Bool hostile = bool("Hostile", true);
    private final dev.yukkit.toolkit.core.Setting.Bool passive = bool("Passive", false);
    private final dev.yukkit.toolkit.core.Setting.Bool invisible = bool("Invisible", true);
    private final dev.yukkit.toolkit.core.Setting.Bool walls = bool("ThroughWalls", true);
    private final dev.yukkit.toolkit.core.Setting.Choice style = choice("Style", "Both", "Outline", "Filled", "Both");
    private final dev.yukkit.toolkit.core.Setting.Choice color = choice("Color", "Team", "Team", "Distance", "Health", "Fixed");
    private final dev.yukkit.toolkit.core.Setting.Num hue = num("Hue", 180, 0, 360, 10);
    private final dev.yukkit.toolkit.core.Setting.Num range = num("Range", 64, 8, 160, 8);

    private interface Setting_ {}

    public ESP() { super("ESP", "Draws boxes around players and mobs.", Category.RENDER); }

    @Override public void onWorld() {
        if (!hasWorld()) return;
        float pt = UtilDraw.partial();
        double max = range.get();
        int count = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            int k = UtilDraw.kind(e);
            if (k == UtilDraw.NONE) continue;
            if (k == UtilDraw.PLAYER && !players.get()) continue;
            if (k == UtilDraw.HOSTILE && !hostile.get()) continue;
            if (k == UtilDraw.PASSIVE && !passive.get()) continue;
            if (!invisible.get() && e.isInvisible()) continue;
            double dist = Math.sqrt(e.distanceToSqr(mc.player));
            if (dist > max) continue;
            AABB box = UtilDraw.smoothBox(e, pt);
            int rgb = UtilDraw.entityColor(e, color.get(), dist, max, hue.asFloat());
            UtilDraw.box(box, rgb, style.get(), walls.get());
            if (++count >= 200) break;
        }
    }

    @Override public String tag() { return style.get(); }
}
