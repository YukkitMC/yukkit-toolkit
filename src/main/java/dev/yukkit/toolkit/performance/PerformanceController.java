package dev.yukkit.toolkit.performance;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;

/**
 * Reversible lightweight preset for integrated vanilla graphics settings.
 * No fake FPS claims, GPU hooks, or changes to server gameplay.
 */
public final class PerformanceController {
    private static boolean enabled;
    private static ParticleStatus oldParticles;
    private static CloudStatus oldClouds;
    private static Integer oldRenderDistance;

    private PerformanceController() {}

    public static boolean isEnabled() { return enabled; }

    public static void toggle(Minecraft client) {
        if (enabled) {
            restore(client);
            return;
        }
        oldParticles = client.options.particles().get();
        oldClouds = client.options.cloudStatus().get();
        oldRenderDistance = client.options.renderDistance().get();
        client.options.particles().set(ParticleStatus.MINIMAL);
        client.options.cloudStatus().set(CloudStatus.OFF);
        // Avoid unexpectedly increasing render distance.
        client.options.renderDistance().set(Math.min(8, oldRenderDistance));
        client.options.save();
        enabled = true;
    }

    public static void restore(Minecraft client) {
        if (!enabled) return;
        if (oldParticles != null) client.options.particles().set(oldParticles);
        if (oldClouds != null) client.options.cloudStatus().set(oldClouds);
        if (oldRenderDistance != null) client.options.renderDistance().set(oldRenderDistance);
        client.options.save();
        enabled = false;
        oldParticles = null;
        oldClouds = null;
        oldRenderDistance = null;
    }
}
