package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Fly extends Module {
    private final Setting.Choice mode = choice("Mode", "Vanilla", "Vanilla", "Glide");
    private final Setting.Num speed = num("Speed", 0.5, 0.05, 3.0, 0.05);
    private final Setting.Num vertical = num("Vertical", 0.4, 0.05, 2.0, 0.05);
    private final Setting.Num glide = num("Glide fall", 0.03, 0.0, 0.2, 0.01);
    private final Setting.Bool antiKick = bool("Anti kick", true);
    private int ticks;

    public Fly() { super("Fly", "Hover and fly freely by setting your own velocity.", Category.MOVEMENT); }

    @Override public void onEnable() { ticks = 0; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (p.isSpectator() || p.isPassenger() || p.isFallFlying()) return;
        ticks++;
        Vec3 dir = UtilMove.inputDirection(p);
        double vy = 0.0;
        if (UtilMove.jumpKey()) vy = vertical.get();
        else if (UtilMove.sneakKey()) vy = -vertical.get();
        else if (mode.is("Glide")) vy = -glide.get();
        if (antiKick.get() && vy == 0.0 && ticks % 40 == 0) vy = -0.04;
        double s = speed.get();
        p.setDeltaMovement(dir.x * s, vy, dir.z * s);
        p.fallDistance = 0;
    }

    @Override public String tag() { return mode.get(); }
}
