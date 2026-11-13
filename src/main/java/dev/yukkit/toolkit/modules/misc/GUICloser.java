package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/** Closes container screens automatically when you take damage or after they stay open too long. */
public final class GUICloser extends Module {
    private final Setting.Bool onDamage = bool("On Damage", true);
    private final Setting.Bool timeout = bool("Timeout", false);
    private final Setting.Num seconds = num("Timeout (s)", 10, 1, 120, 1);
    private final Setting.Bool keepInventory = bool("Keep Inventory", true);

    private int openTicks;

    public GUICloser() {
        super("GUICloser", "Closes chests and other containers on damage or timeout.", Category.MISC);
    }

    @Override
    public void onEnable() { openTicks = 0; }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        var screen = mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen<?>)) {
            openTicks = 0;
            return;
        }
        if (keepInventory.get() && screen instanceof InventoryScreen) {
            openTicks = 0;
            return;
        }
        openTicks++;
        boolean close = (onDamage.get() && mc.player.hurtTime > 0)
                || (timeout.get() && openTicks >= seconds.asInt() * 20);
        if (close) {
            mc.player.closeContainer();
            openTicks = 0;
        }
    }
}
