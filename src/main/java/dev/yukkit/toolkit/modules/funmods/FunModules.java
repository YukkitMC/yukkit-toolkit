package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.ModuleManager;

/** Registers every module of the Fun category. */
public final class FunModules {
    private FunModules() {}

    public static void register() {
        ModuleManager.register(
                new DankBobbing(),
                new Derp(),
                new HandDerp(),
                new SkinDerp(),
                new Twerk()
        );
    }
}
