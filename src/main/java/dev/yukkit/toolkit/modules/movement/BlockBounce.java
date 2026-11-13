package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class BlockBounce extends Module {
    private final Setting.Num bounce = num("Bounciness", 0.7, 0.2, 1.0, 0.05);
    private final Setting.Num minFall = num("Min fall speed", 0.5, 0.2, 2.0, 0.05);
    private double lastY;
    private boolean wasOnGround = true;

    public BlockBounce() { super("BlockBounce", "Bounce back up when landing hard on any block.", Category.MOVEMENT); }

    @Override public void onEnable() { lastY = 0; wasOnGround = true; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        boolean ground = p.onGround();
        if (ground && !wasOnGround && lastY < -minFall.get() && !p.isShiftKeyDown() && !p.isInWater() && !UtilMove.busy(p)) {
            Vec3 m = p.getDeltaMovement();
            p.setDeltaMovement(m.x, -lastY * bounce.get(), m.z);
            p.fallDistance = 0;
        }
        lastY = p.getDeltaMovement().y;
        wasOnGround = ground;
    }
}
