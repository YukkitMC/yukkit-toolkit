package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;

/** Stops you being pushed by entities and by blocks you are stuck inside. Applied by mixins. */
public final class NoPush extends Module {
    private final Setting.Bool entities = bool("Entities", true);
    private final Setting.Bool blocks = bool("Blocks", true);

    public NoPush() { super("NoPush", "Prevents being pushed by entities and blocks.", Category.MOVEMENT); }

    public static boolean blocksEntityPush() {
        NoPush m = ModuleManager.get(NoPush.class);
        return m != null && m.isEnabled() && m.entities.get();
    }

    public static boolean blocksBlockPush() {
        NoPush m = ModuleManager.get(NoPush.class);
        return m != null && m.isEnabled() && m.blocks.get();
    }
}
