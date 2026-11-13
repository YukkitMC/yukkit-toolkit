package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Strafe extends Module {
    private final Setting.Num strength = num("Strength", 1.0, 0.1, 1.0, 0.05);
    private final Setting.Num minSpeed = num("Min speed", 0.12, 0.0, 0.5, 0.01);
    private final Setting.Bool onlyAir = bool("Only in air", true);

    public Strafe() { super("Strafe", "Redirect your momentum instantly while moving.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.isInWater() || p.onClimbable()) return;
        if (onlyAir.get() && p.onGround()) return;
        if (!UtilMove.moving()) return;
        Vec3 m = p.getDeltaMovement();
        double cur = Math.max(UtilMove.horizontalSpeed(m), minSpeed.get());
        Vec3 dir = UtilMove.inputDirection(p);
        double k = strength.get();
        p.setDeltaMovement(m.x * (1 - k) + dir.x * cur * k, m.y, m.z * (1 - k) + dir.z * cur * k);
    }
}
