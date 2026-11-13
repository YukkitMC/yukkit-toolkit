package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;

/** Ignores FOV changes from sprinting, speed effects and bows. Needs PlayerFovMixin. */
public final class NoFov extends Module {
    public NoFov() { super("NoFov", "Stops sprint / speed / bow FOV changes.", Category.RENDER); }

    public static boolean isActive() {
        NoFov m = ModuleManager.get(NoFov.class);
        return m != null && m.isEnabled();
    }
}
