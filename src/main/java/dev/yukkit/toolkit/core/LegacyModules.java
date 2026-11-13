package dev.yukkit.toolkit.core;

import dev.yukkit.toolkit.modules.framework.ModuleRegistry;

/** Exposes the original Yukkit feature toggles (modules.framework) inside the new module screen. */
public final class LegacyModules {
    private LegacyModules() {}

    public static void register() {
        for (var legacy : ModuleRegistry.all()) {
            ModuleManager.register(new Adapter(legacy, map(legacy.category())));
        }
    }

    private static Category map(dev.yukkit.toolkit.modules.framework.ModuleCategory c) {
        return switch (c) {
            case COMBAT -> Category.COMBAT;
            case PLAYER -> Category.PLAYER;
            case MOVEMENT -> Category.MOVEMENT;
            case RENDER -> Category.RENDER;
            case WORLD -> Category.WORLD;
            case EXPLOIT -> Category.EXPLOIT;
            case FUN -> Category.FUN;
            case MISC, AUTOMATION -> Category.MISC;
        };
    }

    private static final class Adapter extends Module {
        private final dev.yukkit.toolkit.modules.framework.Module inner;

        Adapter(dev.yukkit.toolkit.modules.framework.Module inner, Category category) {
            super(inner.name(), inner.description(), category);
            this.inner = inner;
        }

        @Override public boolean isEnabled() { return inner.isEnabled(); }

        @Override public void setEnabled(boolean value) { inner.setEnabled(value); }

        @Override public void onEnable() {}
        @Override public void onDisable() {}
    }
}
