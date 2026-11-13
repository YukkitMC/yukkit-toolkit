package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class LongJump extends Module {
    private final Setting.Num boost = num("Boost", 0.65, 0.2, 2.0, 0.05);
    private final Setting.Num lift = num("Extra lift", 0.05, 0.0, 0.6, 0.01);
    private final Setting.Num decay = num("Decay", 0.99, 0.9, 1.0, 0.005);
    private final Setting.Bool autoJump = bool("Auto jump", false);
    private boolean wasOnGround;
    private double airSpeed;
    private boolean jumping;

    public LongJump() { super("LongJump", "Launches you much further when you jump.", Category.MOVEMENT); }

    @Override public void onEnable() { wasOnGround = false; jumping = false; airSpeed = 0; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.isInWater() || p.onClimbable()) { jumping = false; return; }
        Vec3 m = p.getDeltaMovement();
        boolean ground = p.onGround();

        if (ground && autoJump.get() && UtilMove.moving()) p.jumpFromGround();

        if (wasOnGround && !ground && m.y > 0.1 && (UtilMove.jumpKey() || autoJump.get()) && UtilMove.moving()) {
            Vec3 dir = UtilMove.inputDirection(p);
            airSpeed = boost.get() * UtilMove.speedEffectScale(p);
            p.setDeltaMovement(dir.x * airSpeed, m.y + lift.get(), dir.z * airSpeed);
            jumping = true;
        } else if (jumping && !ground) {
            if (UtilMove.moving()) {
                airSpeed *= decay.get();
                UtilMove.setHorizontal(p, UtilMove.inputDirection(p), airSpeed);
            }
        } else if (ground) {
            jumping = false;
        }
        wasOnGround = ground;
    }
}
