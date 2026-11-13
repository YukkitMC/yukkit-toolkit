package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class LiquidWalk extends Module {
    private final Setting.Bool water = bool("Water", true);
    private final Setting.Bool lava = bool("Lava", true);
    private final Setting.Num speed = num("Speed", 0.2, 0.05, 0.6, 0.01);

    public LiquidWalk() { super("LiquidWalk", "Walk on top of water and lava.", Category.MOVEMENT); }

    private boolean liquid(BlockPos pos) {
        return (water.get() && UtilMove.fluid(pos, FluidTags.WATER))
                || (lava.get() && UtilMove.fluid(pos, FluidTags.LAVA));
    }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || p.isShiftKeyDown()) return;

        double x = p.getX(), y = p.getY(), z = p.getZ();
        BlockPos feet = UtilMove.pos(x, y, z);
        BlockPos below = UtilMove.pos(x, y - 0.3, z);
        BlockPos top = null;
        boolean submerged = false;
        if (liquid(feet)) {
            if (liquid(feet.above())) submerged = true; else top = feet;
        } else if (liquid(below) && !liquid(below.above())) {
            top = below;
        }

        Vec3 m = p.getDeltaMovement();
        if (submerged) {
            p.setDeltaMovement(m.x, 0.25, m.z);
            return;
        }
        if (top == null) return;

        double surface = top.getY() + 0.89;
        double dy = Mth.clamp((surface - y) * 0.5, -0.1, 0.15);
        if (UtilMove.jumpKey() && Math.abs(surface - y) < 0.25) dy = 0.42;
        p.setDeltaMovement(m.x, dy, m.z);
        if (dy < 0.3) p.setOnGround(true);
        p.fallDistance = 0;
        if (UtilMove.moving()) UtilMove.setHorizontal(p, UtilMove.inputDirection(p), speed.get() * UtilMove.speedEffectScale(p));
    }
}
