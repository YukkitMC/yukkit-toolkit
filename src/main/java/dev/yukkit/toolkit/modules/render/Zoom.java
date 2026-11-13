package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;

/** Narrows the field of view. Needs CameraFovMixin. */
public final class Zoom extends Module {
    private final Setting.Num factor = num("Factor", 3.0, 1.5, 10.0, 0.5);

    public Zoom() { super("Zoom", "Zooms the view in.", Category.RENDER); }

    public static boolean isActive() {
        Zoom m = ModuleManager.get(Zoom.class);
        return m != null && m.isEnabled();
    }

    /** Applies the zoom to a computed FOV in degrees. */
    public static float applyFov(float fov) {
        Zoom m = ModuleManager.get(Zoom.class);
        if (m == null || !m.isEnabled()) return fov;
        float f = fov / m.factor.asFloat();
        return Math.max(1.0f, f);
    }

    @Override public String tag() { return factor.display() + "x"; }
}
