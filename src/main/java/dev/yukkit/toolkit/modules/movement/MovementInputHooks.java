package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

/** Entry point used by the keyboard-input mixin; chains the modules that edit the key state. */
public final class MovementInputHooks {
    private MovementInputHooks() {}

    public static Input apply(Input in) {
        if (in == null) return in;
        try {
            if (Minecraft.getInstance().player == null) return in;
            Input out = in;
            SnapTap snap = ModuleManager.get(SnapTap.class);
            if (snap != null && snap.isEnabled()) out = snap.filter(out);
            InventoryMove inv = ModuleManager.get(InventoryMove.class);
            if (inv != null && inv.isEnabled()) out = inv.filter(out);
            AvoidHazards hazards = ModuleManager.get(AvoidHazards.class);
            if (hazards != null && hazards.isEnabled()) out = hazards.filter(out);
            Sneak sneak = ModuleManager.get(Sneak.class);
            if (sneak != null && sneak.isEnabled()) out = sneak.filter(out);
            return out;
        } catch (Throwable t) {
            return in;
        }
    }

    /** Movement vector matching what the vanilla keyboard input would build from these keys. */
    public static Vec2 vectorOf(Input in) {
        float forward = (in.forward() ? 1f : 0f) - (in.backward() ? 1f : 0f);
        float strafe = (in.left() ? 1f : 0f) - (in.right() ? 1f : 0f);
        double len = Math.sqrt(forward * forward + strafe * strafe);
        if (len < 1.0E-5) return Vec2.ZERO;
        return new Vec2((float) (strafe / len), (float) (forward / len));
    }
}
