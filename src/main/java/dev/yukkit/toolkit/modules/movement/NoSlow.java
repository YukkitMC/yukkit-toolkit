package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;

/** Removes the slowdown from using items (eating, drawing a bow, blocking). Applied by a mixin. */
public final class NoSlow extends Module {
    public NoSlow() { super("NoSlow", "Keep full movement speed while using items.", Category.MOVEMENT); }

    public static boolean active() { return ModuleManager.isOn(NoSlow.class); }
}
