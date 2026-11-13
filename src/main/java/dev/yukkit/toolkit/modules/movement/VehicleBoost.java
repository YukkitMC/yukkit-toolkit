package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class VehicleBoost extends Module {
    private final Setting.Num factor = num("Boost", 1.08, 1.0, 1.5, 0.01);
    private final Setting.Num max = num("Max speed", 0.9, 0.2, 3.0, 0.05);

    public VehicleBoost() { super("VehicleBoost", "Speeds up boats, horses and other mounts you steer.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        Entity v = mc.player.getControlledVehicle();
        if (v == null || !UtilMove.moving()) return;
        Vec3 m = v.getDeltaMovement();
        double h = UtilMove.horizontalSpeed(m);
        if (h < 0.01) return;
        double target = Math.min(h * factor.get(), max.get());
        if (target <= h) return;
        double k = target / h;
        v.setDeltaMovement(m.x * k, m.y, m.z * k);
    }
}
