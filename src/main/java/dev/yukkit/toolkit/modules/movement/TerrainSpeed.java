package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class TerrainSpeed extends Module {
    private final Setting.Bool soulSand = bool("Soul sand", true);
    private final Setting.Bool honey = bool("Honey", true);
    private final Setting.Num multiplier = num("Multiplier", 2.5, 1.0, 3.0, 0.1);
    private final Setting.Num cap = num("Max speed", 0.3, 0.1, 1.0, 0.05);

    public TerrainSpeed() { super("TerrainSpeed", "Cancels the slowdown of soul sand and honey blocks.", Category.MOVEMENT); }

    private boolean slow(BlockState s) {
        return (soulSand.get() && s.is(Blocks.SOUL_SAND)) || (honey.get() && s.is(Blocks.HONEY_BLOCK));
    }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || !p.onGround() || !UtilMove.moving()) return;
        double x = p.getX(), y = p.getY(), z = p.getZ();
        if (!slow(UtilMove.state(UtilMove.pos(x, y, z))) && !slow(UtilMove.state(UtilMove.pos(x, y - 0.5, z)))) return;
        Vec3 m = p.getDeltaMovement();
        double h = UtilMove.horizontalSpeed(m);
        if (h < 1.0E-4) return;
        double target = Math.min(h * multiplier.get(), Math.max(cap.get() * UtilMove.speedEffectScale(p), h));
        double k = target / h;
        p.setDeltaMovement(m.x * k, m.y, m.z * k);
    }
}
