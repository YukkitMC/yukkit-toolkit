package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.modules.movement.mixin.LivingEntityJumpAccessor;

public final class NoJumpDelay extends Module {
    public NoJumpDelay() { super("NoJumpDelay", "Removes the delay between jumps while holding jump.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        Object player = mc.player;
        if (player instanceof LivingEntityJumpAccessor accessor) accessor.yukkit$setNoJumpDelay(0);
    }
}
