package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class NoClip extends Module {
    private final Setting.Num speed = num("Speed", 0.4, 0.05, 2.0, 0.05);
    private final Setting.Num vertical = num("Vertical", 0.4, 0.05, 2.0, 0.05);

    public NoClip() { super("NoClip", "Fly through blocks.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        p.noPhysics = true;
        p.fallDistance = 0;
        Vec3 dir = UtilMove.inputDirection(p);
        double vy = UtilMove.jumpKey() ? vertical.get() : UtilMove.sneakKey() ? -vertical.get() : 0.0;
        p.setDeltaMovement(dir.x * speed.get(), vy, dir.z * speed.get());
    }

    @Override public void onDisable() {
        if (mc.player != null) mc.player.noPhysics = false;
    }

    @Override public void onWorldChange() {
        if (mc.player != null) mc.player.noPhysics = false;
    }
}
