package dev.yukkit.toolkit.modules.movement;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Input;

/** Keeps the movement keys working while an inventory-style screen is open. Applied by an input mixin. */
public final class InventoryMove extends Module {
    private final Setting.Bool jump = bool("Jump", true);
    private final Setting.Bool sneak = bool("Sneak", true);
    private final Setting.Bool sprint = bool("Sprint", true);

    public InventoryMove() { super("InventoryMove", "Walk, jump and sprint with inventories open.", Category.MOVEMENT); }

    private static boolean down(KeyMapping key) {
        try {
            InputConstants.Key k = key.getKey();
            return k.getType() == InputConstants.Type.KEYBOARD && InputConstants.isKeyDown(k.getValue());
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean typesText(Screen s) {
        String n = s.getClass().getSimpleName();
        return n.contains("Anvil") || n.contains("Creative") || n.contains("Search") || n.contains("Sign") || n.contains("Book");
    }

    public Input filter(Input in) {
        Screen s = mc.gui.screen();
        if (s == null || !(s instanceof AbstractContainerScreen<?>) || typesText(s)) return in;
        return new Input(
                down(mc.options.keyUp), down(mc.options.keyDown), down(mc.options.keyLeft), down(mc.options.keyRight),
                jump.get() && down(mc.options.keyJump),
                sneak.get() && down(mc.options.keyShift),
                sprint.get() && down(mc.options.keySprint));
    }
}
