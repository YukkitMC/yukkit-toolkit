package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;

/** Changes the speed the client game runs at. */
public final class Timer extends Module {
    private final Setting.Num speed = num("Speed", 2.0, 0.1, 10.0, 0.1);
    private final Setting.Bool onlyMoving = bool("Only moving", false);

    public Timer() { super("Timer", "Changes the speed the game is running at.", Category.WORLD); }

    @Override public void onEnable() { apply(); }

    @Override public void onDisable() { UtilTimer.reset(); }

    @Override public void onTick() { apply(); }

    @Override public void onWorldChange() { if (isEnabled()) apply(); }

    private void apply() {
        boolean moving = hasWorld() && (mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown() || mc.options.keyJump.isDown());
        UtilTimer.setSpeed(onlyMoving.get() && !moving ? 1.0f : speed.asFloat());
    }

    @Override public String tag() { return String.format(java.util.Locale.ROOT, "%.1fx", speed.get()); }
}
