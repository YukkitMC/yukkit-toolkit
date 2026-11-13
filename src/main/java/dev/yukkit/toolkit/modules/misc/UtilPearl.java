package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** Throwing and aiming ender pearls. Client thread only. */
final class UtilPearl {
    private static final double SPEED = 1.5;
    private static final double GRAVITY = 0.03;
    private static final double DRAG = 0.99;
    private static final int MAX_TICKS = 400;

    private UtilPearl() {}

    /** Hotbar index holding an ender pearl, or -1. */
    static int hotbarPearl() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(Items.ENDER_PEARL)) return i;
        }
        return -1;
    }

    static boolean hasPearl() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return hotbarPearl() >= 0 || mc.player.getOffhandItem().is(Items.ENDER_PEARL);
    }

    /** Throws a pearl with the given rotation, then restores the view and the selected slot. */
    static boolean throwPearl(float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gameMode == null || mc.level == null) return false;
        int slot = hotbarPearl();
        boolean offhand = slot < 0 && player.getOffhandItem().is(Items.ENDER_PEARL);
        if (slot < 0 && !offhand) return false;

        var inv = player.getInventory();
        int previous = inv.getSelectedSlot();
        float oldYaw = player.getYRot();
        float oldPitch = player.getXRot();
        InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        if (!offhand && slot != previous) inv.setSelectedSlot(slot);
        player.setYRot(yaw);
        player.setXRot(pitch);
        ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), player.horizontalCollision));
        try {
            mc.gameMode.useItem(player, hand);
        } finally {
            player.setYRot(oldYaw);
            player.setXRot(oldPitch);
            ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Rot(oldYaw, oldPitch, player.onGround(), player.horizontalCollision));
            if (!offhand && slot != previous) inv.setSelectedSlot(previous);
        }
        return true;
    }

    static float yawTo(Vec3 from, Vec3 to) {
        return (float) (Math.toDegrees(Math.atan2(to.z - from.z, to.x - from.x)) - 90.0);
    }

    private static Vec3 direction(float yaw, float pitch) {
        double yr = Math.toRadians(yaw);
        double pr = Math.toRadians(pitch);
        double cos = Math.cos(pr);
        return new Vec3(-Math.sin(yr) * cos, -Math.sin(pr), Math.cos(yr) * cos);
    }

    /**
     * Searches the launch pitch whose arc comes down closest to {@code target} (air drag and gravity only,
     * blocks are ignored).
     *
     * @return {yaw, pitch, horizontalError} or null when nothing reasonable exists
     */
    static float[] solve(Vec3 target) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return null;
        Vec3 start = new Vec3(player.getX(), player.getEyeY() - 0.1, player.getZ());
        float yaw = yawTo(start, target);
        Vec3 inherit = player.getDeltaMovement();
        double iy = player.onGround() ? 0.0 : inherit.y;

        double bestErr = Double.MAX_VALUE;
        float bestPitch = 0f;
        for (float pitch = -89f; pitch <= 89f; pitch += 0.25f) {
            Vec3 d = direction(yaw, pitch);
            double vx = d.x * SPEED + inherit.x;
            double vy = d.y * SPEED + iy;
            double vz = d.z * SPEED + inherit.z;
            double x = start.x, y = start.y, z = start.z;
            double err = Double.MAX_VALUE;
            for (int t = 0; t < MAX_TICKS; t++) {
                double nx = x + vx, ny = y + vy, nz = z + vz;
                if (vy < 0 && ny <= target.y) {
                    double f = (y - target.y) / Math.max(1.0E-6, y - ny);
                    double cx = x + (nx - x) * f;
                    double cz = z + (nz - z) * f;
                    err = Math.hypot(cx - target.x, cz - target.z);
                    break;
                }
                x = nx; y = ny; z = nz;
                vx *= DRAG; vy *= DRAG; vz *= DRAG;
                vy -= GRAVITY;
            }
            if (err < bestErr) {
                bestErr = err;
                bestPitch = pitch;
            }
        }
        if (bestErr > 3.0) return null;
        return new float[] {yaw, bestPitch, (float) bestErr};
    }
}
