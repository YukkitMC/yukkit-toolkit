package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Cancels movement keys that would walk you into cacti, fire, lava and similar. Applied by an input mixin. */
public final class AvoidHazards extends Module {
    private final Setting.Bool cactus = bool("Cactus", true);
    private final Setting.Bool fire = bool("Fire", true);
    private final Setting.Bool lava = bool("Lava", true);
    private final Setting.Bool berries = bool("Berry bush", true);
    private final Setting.Bool magma = bool("Magma", true);

    public AvoidHazards() { super("AvoidHazards", "Stops you from walking into harmful blocks.", Category.MOVEMENT); }

    private boolean harmful(BlockState s, boolean ground) {
        if (cactus.get() && s.is(Blocks.CACTUS)) return true;
        if (fire.get() && (s.is(Blocks.FIRE) || s.is(Blocks.SOUL_FIRE) || s.is(Blocks.CAMPFIRE) || s.is(Blocks.SOUL_CAMPFIRE))) return true;
        if (berries.get() && s.is(Blocks.SWEET_BERRY_BUSH)) return true;
        if (lava.get() && s.getFluidState().is(FluidTags.LAVA)) return true;
        return ground && magma.get() && s.is(Blocks.MAGMA_BLOCK);
    }

    private boolean hazardAt(double x, double y, double z) {
        BlockPos feet = UtilMove.pos(x, y, z);
        return harmful(UtilMove.state(feet), false) || harmful(UtilMove.state(feet.above()), false)
                || harmful(UtilMove.state(feet.below()), true);
    }

    public Input filter(Input in) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || p.isSpectator()) return in;
        float f = (in.forward() ? 1f : 0f) - (in.backward() ? 1f : 0f);
        float s = (in.left() ? 1f : 0f) - (in.right() ? 1f : 0f);
        if (f == 0f && s == 0f) return in;
        double x = p.getX(), y = p.getY(), z = p.getZ();
        if (hazardAt(x, y, z)) return in; // already inside: let the player escape
        Vec3 d = UtilMove.directionOf(p.getYRot(), f, s);
        if (hazardAt(x + d.x * 0.8, y, z + d.z * 0.8) || hazardAt(x + d.x * 1.5, y, z + d.z * 1.5)) {
            return new Input(false, false, false, false, in.jump(), in.shift(), in.sprint());
        }
        return in;
    }
}
