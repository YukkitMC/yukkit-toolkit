package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class VehicleControl extends Module {
    private final Setting.Num speed = num("Speed", 0.6, 0.1, 3.0, 0.05);
    private final Setting.Num vertical = num("Vertical", 0.4, 0.05, 2.0, 0.05);
    private final Setting.Bool fly = bool("Fly", false);

    public VehicleControl() { super("VehicleControl", "Steer the vehicle you ride directly.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        Entity v = mc.player.getControlledVehicle();
        if (v == null) return;
        Vec3 m = v.getDeltaMovement();
        Vec3 dir = UtilMove.inputDirection(mc.player);
        double vy = m.y;
        if (fly.get()) vy = UtilMove.jumpKey() ? vertical.get() : UtilMove.sneakKey() ? -vertical.get() : 0.0;
        if (UtilMove.moving()) v.setDeltaMovement(dir.x * speed.get(), vy, dir.z * speed.get());
        else if (fly.get()) v.setDeltaMovement(m.x, vy, m.z);
        if (fly.get()) v.fallDistance = 0;
    }
}
