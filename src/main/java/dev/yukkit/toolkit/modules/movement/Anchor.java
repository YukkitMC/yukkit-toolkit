package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Anchor extends Module {
    private final Setting.Num range = num("Range", 3, 1, 6, 1);
    private final Setting.Num pull = num("Pull", 0.4, 0.1, 1.0, 0.05);

    public Anchor() { super("Anchor", "Pulls you into single-block holes while falling.", Category.MOVEMENT); }

    private static boolean hole(BlockPos p) {
        return !UtilMove.solid(p) && !UtilMove.solid(p.above())
                && UtilMove.solid(p.north()) && UtilMove.solid(p.south())
                && UtilMove.solid(p.east()) && UtilMove.solid(p.west())
                && UtilMove.solid(p.below());
    }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.onGround() || p.isInWater() || p.isShiftKeyDown()) return;
        Vec3 m = p.getDeltaMovement();
        if (m.y > 0.0) return;
        BlockPos base = UtilMove.pos(p.getX(), p.getY(), p.getZ());
        for (int d = 0; d <= range.asInt(); d++) {
            BlockPos at = base.below(d);
            if (!hole(at)) continue;
            double cx = at.getX() + 0.5, cz = at.getZ() + 0.5;
            double dx = cx - p.getX(), dz = cz - p.getZ();
            if (Math.abs(dx) > 0.6 || Math.abs(dz) > 0.6) return;
            double k = pull.get();
            p.setDeltaMovement(Mth.clamp(dx * k, -0.2, 0.2), m.y, Mth.clamp(dz * k, -0.2, 0.2));
            return;
        }
    }
}
