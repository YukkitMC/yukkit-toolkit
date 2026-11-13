package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Step extends Module {
    private final Setting.Num height = num("Height", 1.0, 0.7, 2.5, 0.1);
    private final Setting.Bool packets = bool("Jump packets", true);
    private int cooldown;

    public Step() { super("Step", "Walk up blocks taller than the vanilla step height.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (cooldown > 0) { cooldown--; return; }
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.isInWater() || p.onClimbable() || p.isShiftKeyDown()) return;
        if (!p.horizontalCollision || !p.onGround() || !UtilMove.moving()) return;

        Vec3 d = UtilMove.inputDirection(p);
        AABB box = p.getBoundingBox();
        double rise = -1.0;
        for (double r = 0.7; r <= height.get() + 1.0E-6; r += 0.1) {
            AABB up = box.move(0.0, r, 0.0);
            if (!mc.level.noCollision(p, up)) break; // ceiling in the way
            if (mc.level.noCollision(p, up.move(d.x * 0.3, 0.0, d.z * 0.3))) { rise = r; break; }
        }
        if (rise < 0.0) return;

        double x = p.getX(), y = p.getY(), z = p.getZ();
        if (packets.get()) {
            ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Pos(x, y + Math.min(0.42, rise), z, false, p.horizontalCollision));
            if (rise > 1.0) {
                ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Pos(x, y + 0.75, z, false, p.horizontalCollision));
            }
        }
        p.setPos(x, y + rise, z);
        cooldown = 2;
    }

    @Override public String tag() { return String.format(java.util.Locale.ROOT, "%.1f", height.get()); }
}
