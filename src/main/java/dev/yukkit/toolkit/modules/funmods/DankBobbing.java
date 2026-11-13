package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.util.Mth;

/** Exaggerated, rhythmic camera bobbing while you walk. The nod is added and removed again each tick, so aiming is unaffected over time. */
public final class DankBobbing extends Module {
    private final Setting.Num strength = num("Strength", 4.0, 0.5, 15.0, 0.5);
    private final Setting.Num rhythm = num("Rhythm", 1.0, 0.25, 3.0, 0.25);
    private final Setting.Bool sprintOnly = bool("Sprint Only", false);

    private float applied;
    private float phase;

    public DankBobbing() {
        super("DankBobbing", "Makes the camera bob dramatically while moving.", Category.FUN);
    }

    @Override
    public void onEnable() { applied = 0f; phase = 0f; }

    @Override
    public void onDisable() {
        if (mc.player != null && applied != 0f) mc.player.setXRot(Mth.clamp(mc.player.getXRot() - applied, -90f, 90f));
        applied = 0f;
    }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        var player = mc.player;
        double speed = player.getDeltaMovement().horizontalDistance();
        boolean moving = player.onGround() && speed > 0.05 && (!sprintOnly.get() || player.isSprinting());
        float target = 0f;
        if (moving) {
            phase += (float) (speed * 6.0 * rhythm.get());
            target = Mth.sin(phase) * strength.asFloat();
        }
        float smooth = applied + (target - applied) * 0.5f;
        float delta = smooth - applied;
        applied = smooth;
        if (delta != 0f) player.setXRot(Mth.clamp(player.getXRot() + delta, -90f, 90f));
    }
}
