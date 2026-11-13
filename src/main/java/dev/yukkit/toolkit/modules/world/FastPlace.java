package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import dev.yukkit.toolkit.modmixin.ClientDelayAccessor;
import net.minecraft.world.item.BlockItem;

/** Shortens the delay between two right clicks. */
public final class FastPlace extends Module {
    private final Setting.Num delay = num("Delay", 0, 0, 4, 1);
    private final Setting.Bool blocksOnly = bool("Blocks only", true);

    public FastPlace() { super("FastPlace", "Allows you to place blocks faster.", Category.WORLD); }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        if (blocksOnly.get()) {
            boolean holdsBlock = mc.player.getMainHandItem().getItem() instanceof BlockItem
                    || mc.player.getOffhandItem().getItem() instanceof BlockItem;
            if (!holdsBlock) return;
        }
        ClientDelayAccessor access = (ClientDelayAccessor) (Object) mc;
        if (access.yukkit$getRightClickDelay() > delay.asInt()) access.yukkit$setRightClickDelay(delay.asInt());
    }

    @Override public String tag() { return delay.display(); }
}
