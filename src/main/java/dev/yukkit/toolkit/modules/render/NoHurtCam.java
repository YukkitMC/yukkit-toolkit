package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;

/** Removes the camera shake when taking damage. Needs GameRendererMixin. */
public final class NoHurtCam extends Module {
    public NoHurtCam() { super("NoHurtCam", "Disables the screen shake when hurt.", Category.RENDER); }

    public static boolean isActive() {
        NoHurtCam m = ModuleManager.get(NoHurtCam.class);
        return m != null && m.isEnabled();
    }
}
