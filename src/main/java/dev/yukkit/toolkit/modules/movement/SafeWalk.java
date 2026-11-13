package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;

/** Stops you from walking off block edges without sneaking. Applied by a mixin. */
public final class SafeWalk extends Module {
    public SafeWalk() { super("SafeWalk", "Never walk off the edge of a block.", Category.MOVEMENT); }

    public static boolean active() { return ModuleManager.isOn(SafeWalk.class); }
}
