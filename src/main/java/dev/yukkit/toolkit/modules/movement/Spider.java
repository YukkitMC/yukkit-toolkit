package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;

public final class Spider extends Module {
    private final Setting.Num climb = num("Climb speed", 0.2, 0.05, 0.8, 0.01);

    public Spider() { super("Spider", "Climb walls by walking into them.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.isInWater() || p.isShiftKeyDown()) return;
        if (p.horizontalCollision && UtilMove.moving()) {
            UtilMove.setVertical(p, climb.get());
            p.fallDistance = 0;
        }
    }
}
