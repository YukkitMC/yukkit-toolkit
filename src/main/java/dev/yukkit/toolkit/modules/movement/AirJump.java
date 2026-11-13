package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;

public final class AirJump extends Module {
    private final Setting.Num delay = num("Delay", 6, 2, 20, 1);
    private int cooldown;

    public AirJump() { super("AirJump", "Jump again while in mid-air.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (cooldown > 0) cooldown--;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.onGround() || p.isInWater() || p.onClimbable()) return;
        if (cooldown == 0 && UtilMove.jumpKey() && mc.gui.screen() == null) {
            p.jumpFromGround();
            cooldown = delay.asInt();
        }
    }
}
