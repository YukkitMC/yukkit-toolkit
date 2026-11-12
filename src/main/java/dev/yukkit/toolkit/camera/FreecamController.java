package dev.yukkit.toolkit.camera;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class FreecamController {
    private static boolean enabled;
    private static Vec3d position = Vec3d.ZERO;
    private static float yaw;
    private static float pitch;

    private FreecamController() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static Vec3d position() {
        return position;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static void toggle(MinecraftClient client) {
        if (enabled) {
            disable();
            return;
        }
        if (client.player == null || client.world == null) return;
        position = client.player.getEyePos();
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static void mouseLook(double deltaX, double deltaY) {
        yaw += (float) (deltaX * 0.15);
        pitch = MathHelper.clamp(pitch + (float) (deltaY * 0.15), -90.0f, 90.0f);
    }

    public static void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.currentScreen != null) return;

        double forward = (client.options.forwardKey.isPressed() ? 1 : 0)
                - (client.options.backKey.isPressed() ? 1 : 0);
        double side = (client.options.rightKey.isPressed() ? 1 : 0)
                - (client.options.leftKey.isPressed() ? 1 : 0);
        double vertical = (client.options.jumpKey.isPressed() ? 1 : 0)
                - (client.options.sneakKey.isPressed() ? 1 : 0);

        double radians = Math.toRadians(yaw);
        Vec3d motion = new Vec3d(-Math.sin(radians) * forward - Math.cos(radians) * side,
                vertical, Math.cos(radians) * forward - Math.sin(radians) * side);
        if (motion.lengthSquared() > 0.0) {
            double speed = client.options.sprintKey.isPressed() ? 1.5 : 0.42;
            position = position.add(motion.normalize().multiply(speed));
        }
    }
}
