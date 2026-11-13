package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;

/** Rapidly toggles sneaking. */
public final class Twerk extends Module {
    private final Setting.Num delay = num("Delay (ticks)", 2, 1, 10, 1);
    private final Setting.Bool onlyStill = bool("Only When Still", false);

    private int timer;
    private boolean down;

    public Twerk() {
        super("Twerk", "Toggles sneak repeatedly.", Category.FUN);
    }

    @Override
    public void onEnable() { timer = 0; down = false; }

    @Override
    public void onDisable() {
        mc.options.keyShift.setDown(false);
        down = false;
    }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        if (mc.gui.screen() != null) {
            mc.options.keyShift.setDown(false);
            down = false;
            return;
        }
        if (onlyStill.get() && mc.player.getDeltaMovement().horizontalDistance() > 0.05) {
            if (down) mc.options.keyShift.setDown(false);
            down = false;
            return;
        }
        if (timer-- > 0) return;
        timer = delay.asInt() - 1;
        down = !down;
        mc.options.keyShift.setDown(down);
    }
}
