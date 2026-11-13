package dev.yukkit.toolkit.modules.misc;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;

/**
 * Hold shift and the left mouse button while dragging over inventory slots to quick-move
 * every stack you pass over. The click itself is issued by
 * {@code dev.yukkit.toolkit.modules.misc.mixin.ItemScrollerMixin}.
 */
public final class ItemScroller extends Module {
    private static int lastSlot = -1;
    private static long lastActionNanos;

    private final Setting.Num delay = num("Delay (ms)", 40, 0, 500, 10);
    private final Setting.Bool requireShift = bool("Require Shift", true);

    public ItemScroller() {
        super("ItemScroller", "Drag over slots with shift held to move many stacks quickly.", Category.MISC);
    }

    @Override
    public void onDisable() { lastSlot = -1; }

    /** Called by the screen mixin for the hovered slot; true when that slot should be quick-moved now. */
    public static boolean shouldAct(int slotIndex) {
        ItemScroller self = ModuleManager.get(ItemScroller.class);
        if (self == null || !self.isEnabled()) return false;
        if (mc.mouseHandler == null || !mc.mouseHandler.isLeftPressed()) {
            lastSlot = -1;
            return false;
        }
        if (self.requireShift.get()
                && !(InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT))) {
            return false;
        }
        if (slotIndex == lastSlot) return false;
        long now = System.nanoTime();
        if (now - lastActionNanos < (long) (self.delay.get() * 1_000_000.0)) return false;
        lastActionNanos = now;
        lastSlot = slotIndex;
        return true;
    }
}
