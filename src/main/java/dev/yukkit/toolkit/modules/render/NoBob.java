package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;

/** Removes the walking view bobbing. Needs GameRendererMixin. */
public final class NoBob extends Module {
    public NoBob() { super("NoBob", "Disables view bobbing while walking.", Category.RENDER); }

    public static boolean isActive() {
        NoBob m = ModuleManager.get(NoBob.class);
        return m != null && m.isEnabled();
    }
}
