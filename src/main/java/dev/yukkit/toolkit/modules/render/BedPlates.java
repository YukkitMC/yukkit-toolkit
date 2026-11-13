package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Draws a plate on nearby beds, coloured by how well they are covered (green open, red walled in). */
public final class BedPlates extends Module {
    private final Setting.Num radius = num("Radius", 24, 6, 48, 2);
    private final Setting.Bool tracers = bool("Tracers", false);

    private record Plate(BlockPos pos, float cover) {}

    private List<Plate> plates = new ArrayList<>();
    private int timer;

    public BedPlates() { super("BedPlates", "Highlights beds and how protected they are.", Category.RENDER); }

    @Override public void onEnable() { plates = new ArrayList<>(); timer = 0; }
    @Override public void onDisable() { plates = new ArrayList<>(); }
    @Override public void onWorldChange() { plates = new ArrayList<>(); timer = 0; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (timer-- > 0) return;
        timer = 20;
        int r = radius.asInt();
        List<BlockPos> beds = UtilDraw.scan(r, Math.min(r, 16), 64, (p, s) -> s.getBlock() instanceof BedBlock);
        List<Plate> out = new ArrayList<>();
        for (BlockPos p : beds) {
            int covered = 0;
            if (!mc.level.getBlockState(p.above()).isAir()) covered++;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (!mc.level.getBlockState(p.relative(d)).isAir()) covered++;
            }
            out.add(new Plate(p, covered / 5.0f));
        }
        plates = out;
    }

    @Override public void onWorld() {
        if (plates.isEmpty() || !hasWorld()) return;
        Vec3 origin = UtilDraw.lineOrigin();
        for (Plate pl : plates) {
            BlockPos p = pl.pos();
            int rgb = UtilDraw.hue((1.0f - pl.cover()) * 120f);
            AABB box = new AABB(p.getX(), p.getY() + 0.56, p.getZ(), p.getX() + 1, p.getY() + 0.7, p.getZ() + 1);
            UtilDraw.box(box, rgb, "Both", true);
            if (tracers.get()) UtilDraw.line(origin, Vec3.atCenterOf(p), rgb, 1.0f, true);
        }
    }

    @Override public String tag() { return Integer.toString(plates.size()); }
}
