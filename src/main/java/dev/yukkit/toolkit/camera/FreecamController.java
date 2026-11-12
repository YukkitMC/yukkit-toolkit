package dev.yukkit.toolkit.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FreecamController {
    private static boolean enabled;
    private static Vec3 position = Vec3.ZERO;
    private static float yaw;
    private static float pitch;

    private FreecamController() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static Vec3 position() {
        return position;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static void toggle(Minecraft client) {
        if (enabled) {
            disable();
            return;
        }
        if (client.player == null || client.level == null) return;
        position = client.player.getEyePosition();
        yaw = client.player.getYRot();
        pitch = client.player.getXRot();
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static void mouseLook(double deltaX, double deltaY) {
        yaw += (float) (deltaX * 0.15);
        pitch = Mth.clamp(pitch + (float) (deltaY * 0.15), -90.0f, 90.0f);
    }

    public static void tick(Minecraft client) {
        if (!enabled || client.player == null || client.gui.screen() != null) return;

        double forward = (client.options.keyUp.isDown() ? 1 : 0)
                - (client.options.keyDown.isDown() ? 1 : 0);
        double side = (client.options.keyRight.isDown() ? 1 : 0)
                - (client.options.keyLeft.isDown() ? 1 : 0);
        double vertical = (client.options.keyJump.isDown() ? 1 : 0)
                - (client.options.keyShift.isDown() ? 1 : 0);

        double radians = Math.toRadians(yaw);
        Vec3 motion = new Vec3(-Math.sin(radians) * forward - Math.cos(radians) * side,
                vertical, Math.cos(radians) * forward - Math.sin(radians) * side);
        if (motion.lengthSqr() > 0.0) {
            double speed = client.options.keySprint.isDown() ? 1.5 : 0.42;
            position = position.add(motion.normalize().scale(speed));
        }
    }
}
