package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class HighJump extends Module {
    private final Setting.Num height = num("Power", 0.8, 0.45, 3.0, 0.05);
    private boolean wasOnGround;

    public HighJump() { super("HighJump", "Jump much higher than normal.", Category.MOVEMENT); }

    @Override public void onEnable() { wasOnGround = false; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        boolean ground = p.onGround();
        Vec3 m = p.getDeltaMovement();
        if (wasOnGround && !ground && m.y > 0.1 && UtilMove.jumpKey() && !UtilMove.busy(p)
                && !p.isInWater() && !p.onClimbable()) {
            p.setDeltaMovement(m.x, height.get(), m.z);
        }
        wasOnGround = ground;
    }

    @Override public String tag() { return String.format(java.util.Locale.ROOT, "%.2f", height.get()); }
}
