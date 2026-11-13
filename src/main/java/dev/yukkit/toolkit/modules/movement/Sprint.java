package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;

public final class Sprint extends Module {
    private final Setting.Bool omni = bool("Omni", false);
    private final Setting.Bool whileUsing = bool("While using item", false);

    public Sprint() { super("Sprint", "Keeps you sprinting while you move.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (p.isSpectator() || p.isShiftKeyDown() || p.horizontalCollision) return;
        if (p.getFoodData().getFoodLevel() <= 6 && !p.isCreative()) return;
        if (p.isUsingItem() && !whileUsing.get()) return;
        boolean wants = omni.get() ? UtilMove.moving() : UtilMove.forwardImpulse() > 0f;
        if (wants) p.setSprinting(true);
    }

    @Override public String tag() { return omni.get() ? "Omni" : ""; }
}
