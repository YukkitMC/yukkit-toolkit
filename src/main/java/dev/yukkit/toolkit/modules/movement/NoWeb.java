package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;

/** Ignores the "stuck" slowdown of cobwebs, berry bushes and similar blocks. Applied by a mixin. */
public final class NoWeb extends Module {
    public NoWeb() { super("NoWeb", "Move freely through cobwebs and berry bushes.", Category.MOVEMENT); }

    public static boolean active() { return ModuleManager.isOn(NoWeb.class); }
}
