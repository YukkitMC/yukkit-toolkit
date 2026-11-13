package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Speed extends Module {
    private final Setting.Choice mode = choice("Mode", "BHop", "Vanilla", "BHop", "OnGround");
    private final Setting.Num speed = num("Speed", 0.32, 0.1, 1.5, 0.01);
    private final Setting.Bool effect = bool("Scale with potion", true);

    public Speed() { super("Speed", "Moves faster on the ground and in the air.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.isInWater() || p.onClimbable() || p.isShiftKeyDown()) return;
        if (!UtilMove.moving()) return;
        if (mode.is("BHop") && p.onGround()) p.jumpFromGround();
        if (mode.is("OnGround") && !p.onGround()) return;
        double s = speed.get() * (effect.get() ? UtilMove.speedEffectScale(p) : 1.0);
        Vec3 dir = UtilMove.inputDirection(p);
        UtilMove.setHorizontal(p, dir, s);
    }

    @Override public String tag() { return mode.get(); }
}
