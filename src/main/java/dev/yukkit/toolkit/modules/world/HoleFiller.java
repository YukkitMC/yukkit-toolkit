package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Fills one-block-wide, two-deep holes around you (the kind players hide in during crystal PvP). */
public final class HoleFiller extends Module {
    private final Setting.Num range = num("Range", 4.0, 1.0, 6.0, 0.5);
    private final Setting.Num delay = num("Delay", 1, 0, 6, 1);
    private final Setting.Num perTick = num("Blocks per tick", 1, 1, 3, 1);
    private final Setting.Choice blocks = choice("Blocks", "Any", "Any", "Obsidian");
    private final Setting.Bool safeOnly = bool("Obsidian/bedrock holes only", true);

    private int cooldown;

    public HoleFiller() {
        super("HoleFiller", "Automatically fills safe spots for crystal PvP.", Category.WORLD);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready()) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        int slot = blocks.is("Obsidian")
                ? UtilBuild.findSlot(s -> s.is(Items.OBSIDIAN))
                : UtilBuild.findSlot(UtilBuild::isBuildBlock);
        if (slot == UtilBuild.NONE) return;

        Vec3 eye = mc.player.getEyePosition();
        double r = Math.min(range.get(), UtilBuild.reach());
        int ri = (int) Math.ceil(r);
        BlockPos feet = mc.player.blockPosition();
        List<BlockPos> holes = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    if (eye.distanceToSqr(Vec3.atCenterOf(cursor)) > r * r) continue;
                    if (isHole(cursor)) holes.add(cursor.immutable());
                }
            }
        }
        holes.sort(Comparator.comparingDouble(p -> eye.distanceToSqr(Vec3.atCenterOf(p))));
        if (UtilBuild.placeAll(holes, slot, perTick.asInt(), false) > 0) cooldown = delay.asInt();
    }

    private boolean isHole(BlockPos pos) {
        if (!UtilBuild.isReplaceable(pos) || !UtilBuild.isReplaceable(pos.above())) return false;
        // Never entomb ourselves.
        if (pos.equals(mc.player.blockPosition()) || pos.equals(mc.player.blockPosition().above())) return false;
        if (!solid(pos.below())) return false;
        for (Direction d : UtilBuild.HORIZONTAL) {
            if (!solid(pos.relative(d))) return false;
        }
        return true;
    }

    private boolean solid(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (safeOnly.get()) {
            return state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK) || state.is(Blocks.CRYING_OBSIDIAN);
        }
        return !state.isAir() && !state.canBeReplaced() && state.getFluidState().isEmpty();
    }
}
