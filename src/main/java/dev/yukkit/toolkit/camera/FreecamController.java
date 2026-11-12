package dev.yukkit.toolkit.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only camera movement. The player remains controlled by the server.
 * A damped velocity and partial-tick interpolation eliminate fixed 20 TPS camera jumps.
 */
public final class FreecamController {
    private static boolean enabled;
    private static Vec3 position = Vec3.ZERO;
    private static Vec3 previousPosition = Vec3.ZERO;
    private static Vec3 velocity = Vec3.ZERO;
    private static float yaw;
    private static float pitch;

    private FreecamController() {}

    public static boolean isEnabled() { return enabled; }
    public static float yaw() { return yaw; }
    public static float pitch() { return pitch; }

    public static Vec3 cameraPosition(float partialTick) {
        double t = Mth.clamp((double) partialTick, 0.0, 1.0);
        return new Vec3(
                Mth.lerp(t, previousPosition.x, position.x),
                Mth.lerp(t, previousPosition.y, position.y),
                Mth.lerp(t, previousPosition.z, position.z));
    }

    public static void toggle(Minecraft client) {
        if (enabled) {
            disable();
            return;
        }
        if (client.player == null || client.level == null) return;
        PerspectiveController.disable();
        position = client.player.getEyePosition();
        previousPosition = position;
        velocity = Vec3.ZERO;
        yaw = client.player.getYRot();
        pitch = client.player.getXRot();
        enabled = true;
    }

    public static void disable() {
        enabled = false;
        velocity = Vec3.ZERO;
    }

    public static void mouseLook(double deltaX, double deltaY) {
        yaw += (float) (deltaX * 0.15);
        pitch = Mth.clamp(pitch + (float) (deltaY * 0.15), -90.0f, 90.0f);
    }

    public static void tick(Minecraft client) {
        if (!enabled || client.player == null) return;
        previousPosition = position;

        // Do not move the camera when menus are open. Reset velocity so
        // reopening the world never produces a sudden large displacement.
        if (client.gui.screen() != null) {
            velocity = Vec3.ZERO;
            return;
        }
        double forward = (client.options.keyUp.isDown() ? 1 : 0)
                - (client.options.keyDown.isDown() ? 1 : 0);
        double side = (client.options.keyRight.isDown() ? 1 : 0)
                - (client.options.keyLeft.isDown() ? 1 : 0);
        double vertical = (client.options.keyJump.isDown() ? 1 : 0)
                - (client.options.keyShift.isDown() ? 1 : 0);

        double radians = Math.toRadians(yaw);
        Vec3 direction = new Vec3(
                -Math.sin(radians) * forward - Math.cos(radians) * side,
                vertical,
                Math.cos(radians) * forward - Math.sin(radians) * side
        );
        double speed = client.options.keySprint.isDown() ? 1.15 : 0.48;
        Vec3 target = direction.lengthSqr() > 0 ? direction.normalize().scale(speed) : Vec3.ZERO;
        // Acceleration and deceleration are smoothed without scanning the world or allocating tasks.
        velocity = velocity.scale(0.68).add(target.scale(0.32));
        if (velocity.lengthSqr() < 0.000004) velocity = Vec3.ZERO;
        position = position.add(velocity);
    }
}
