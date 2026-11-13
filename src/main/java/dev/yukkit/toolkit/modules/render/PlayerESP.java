package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import dev.yukkit.toolkit.team.TeamEspColor;
import dev.yukkit.toolkit.team.TeamNames;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Boxes (and optional tracers) around other players. Teammates use the team colour from the Teams tab. */
public final class PlayerESP extends Module {
    private final Setting.Choice style = choice("Style", "Both", "Outline", "Filled", "Both");
    private final Setting.Choice enemyColor = choice("Enemy Color", "Distance", "Distance", "Health", "Red");
    private final Setting.Bool teammates = bool("Show Teammates", true);
    private final Setting.Bool invisible = bool("Invisible", true);
    private final Setting.Bool walls = bool("ThroughWalls", true);
    private final Setting.Bool tracers = bool("Tracers", false);
    private final Setting.Num range = num("Range", 128, 16, 256, 16);

    public PlayerESP() { super("PlayerESP", "Highlights other players; teammates use your team colour.", Category.RENDER); }

    @Override public void onWorld() {
        if (!hasWorld()) return;
        float pt = UtilDraw.partial();
        double max = range.get();
        Vec3 origin = tracers.get() ? UtilDraw.lineOrigin() : null;
        int count = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Player p) || p == mc.player || p.isSpectator()) continue;
            if (!invisible.get() && p.isInvisible()) continue;
            boolean mate = TeamNames.matches(p);
            if (mate && !teammates.get()) continue;
            double dist = Math.sqrt(p.distanceToSqr(mc.player));
            if (dist > max) continue;
            int rgb = mate ? TeamEspColor.rgb()
                    : switch (enemyColor.get()) {
                        case "Health" -> UtilDraw.entityColor(p, "Health", dist, max, 0f);
                        case "Red" -> 0xFF4848;
                        default -> UtilDraw.distanceColor(dist, max);
                    };
            AABB box = UtilDraw.smoothBox(p, pt);
            UtilDraw.box(box, rgb, style.get(), walls.get());
            if (origin != null) {
                Vec3 pos = p.getPosition(pt);
                UtilDraw.line(origin, new Vec3(pos.x, pos.y + p.getBbHeight() * 0.5, pos.z), rgb, 1.5f, walls.get());
            }
            if (++count >= 100) break;
        }
    }
}
