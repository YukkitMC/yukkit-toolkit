package dev.yukkit.toolkit.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** 360-degree freelook: rotates only the viewer, not the player on the server. */
public final class PerspectiveController {
    private static boolean enabled;
    private static float yaw;
    private static float pitch;

    private PerspectiveController() {}

    public static boolean isEnabled() { return enabled; }
    public static float yaw() { return yaw; }
    public static float pitch() { return pitch; }

    public static void toggle(Minecraft client) {
        if (enabled) {
            disable();
            return;
        }
        if (client.player == null || client.level == null) return;
        FreecamController.disable();
        yaw = client.player.getYRot();
        pitch = client.player.getXRot();
        enabled = true;
    }

    public static void disable() { enabled = false; }

    public static void mouseLook(double deltaX, double deltaY) {
        yaw += (float) (deltaX * 0.15);
        pitch = Mth.clamp(pitch + (float) (deltaY * 0.15), -90.0f, 90.0f);
    }
}
