package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Parkour extends Module {
    private final Setting.Num drop = num("Min drop", 1.2, 0.6, 4.0, 0.2);
    private int cooldown;

    public Parkour() { super("Parkour", "Jumps automatically at the edge of a block.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (cooldown > 0) { cooldown--; return; }
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || !p.onGround() || p.isShiftKeyDown() || p.isInWater()) return;
        if (!UtilMove.moving()) return;
        Vec3 d = UtilMove.inputDirection(p);
        AABB ahead = p.getBoundingBox().move(d.x * 0.35, -drop.get(), d.z * 0.35);
        if (mc.level.noCollision(p, ahead)) {
            p.jumpFromGround();
            cooldown = 5;
        }
    }
}
