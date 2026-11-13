package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.player.Input;

/** Holds the sneak key for you. Applied by an input mixin. */
public final class Sneak extends Module {
    private final Setting.Bool onlyMoving = bool("Only while moving", false);

    public Sneak() { super("Sneak", "Sneaks automatically.", Category.MOVEMENT); }

    public Input filter(Input in) {
        if (in.shift()) return in;
        if (onlyMoving.get() && !(in.forward() || in.backward() || in.left() || in.right())) return in;
        return new Input(in.forward(), in.backward(), in.left(), in.right(), in.jump(), true, in.sprint());
    }
}
