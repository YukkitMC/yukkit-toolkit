package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.ModuleManager;

public final class RenderModules {
    private RenderModules() {}

    public static void register() {
        ModuleManager.register(
                new ESP(), new PlayerESP(),
                new Tracers(),
                new BlockESP(),
                new HoleESP(),
                new VoidESP(),
                new BedPlates(),
                new BlockOutline(),
                new Breadcrumbs(),
                new Trajectories(),
                new LogoffSpot(),
                new NewChunks(),
                new NoHurtCam(),
                new NoBob(),
                new NoFov(),
                new Zoom(),
                new AntiBlind()
        );
    }
}
