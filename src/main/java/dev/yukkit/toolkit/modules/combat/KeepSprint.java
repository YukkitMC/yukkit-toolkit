package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;

/** Attacking no longer slows you down or cancels your sprint. Needs {@code KeepSprintMixin}. */
public final class KeepSprint extends Module {
    private final Setting.Num motion = num("Motion Kept", 100, 0, 100, 5);
    private final Setting.Bool keepSprinting = bool("Keep Sprint State", true);

    public KeepSprint() {
        super("KeepSprint", "Keeps your speed and sprint when you hit something.", Category.COMBAT);
    }

    @Override public String tag() { return motion.asInt() + "%"; }

    /** Fraction of horizontal motion to restore after an attack (0 when the module is off). */
    public static double motionKept() {
        KeepSprint m = ModuleManager.get(KeepSprint.class);
        return m != null && m.isEnabled() ? m.motion.get() / 100.0 : 0.0;
    }

    public static boolean keepsSprintState() {
        KeepSprint m = ModuleManager.get(KeepSprint.class);
        return m != null && m.isEnabled() && m.keepSprinting.get();
    }
}
