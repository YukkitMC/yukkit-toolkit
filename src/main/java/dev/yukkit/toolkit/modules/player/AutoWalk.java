package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;

public final class AutoWalk extends Module {
    private final Setting.Bool sprint = bool("Sprint", false);
    private final Setting.Bool jump = bool("AutoJump", false);

    private boolean heldForward;
    private boolean heldSprint;

    public AutoWalk() {
        super("AutoWalk", "Holds the forward key for you.", Category.PLAYER);
    }

    @Override public void onDisable() { release(); }
    @Override public void onWorldChange() { heldForward = false; heldSprint = false; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gui.screen() != null) { release(); return; }
        if (!mc.options.keyUp.isDown()) {
            mc.options.keyUp.setDown(true);
            heldForward = true;
        }
        if (sprint.get() && !mc.options.keySprint.isDown()) {
            mc.options.keySprint.setDown(true);
            heldSprint = true;
        }
        if (jump.get() && mc.player.horizontalCollision && mc.player.onGround()) mc.player.jumpFromGround();
    }

    private void release() {
        if (heldForward) { mc.options.keyUp.setDown(false); heldForward = false; }
        if (heldSprint) { mc.options.keySprint.setDown(false); heldSprint = false; }
    }
}
