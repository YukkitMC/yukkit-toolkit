package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import dev.yukkit.toolkit.modules.world.mixin.GameModeDelayAccessor;

/** Removes the pause between two broken blocks. */
public final class FastBreak extends Module {
    private final Setting.Num delay = num("Delay", 0, 0, 5, 1);

    public FastBreak() { super("FastBreak", "Allows you to break blocks faster.", Category.WORLD); }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gameMode == null) return;
        GameModeDelayAccessor access = (GameModeDelayAccessor) (Object) mc.gameMode;
        if (access.yukkit$getDestroyDelay() > delay.asInt()) access.yukkit$setDestroyDelay(delay.asInt());
    }

    @Override public String tag() { return delay.display(); }
}
