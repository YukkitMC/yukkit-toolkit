package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Breaks the blocks around you. Instant blocks are broken in bulk, other blocks one after another. */
public final class Nuker extends Module {
    private final Setting.Num range = num("Range", 4.0, 1.0, 6.0, 0.5);
    private final Setting.Choice filter = choice("Blocks", "All", "All", "Ores", "Above feet", "Layer");
    private final Setting.Num perTick = num("Instant per tick", 4, 1, 16, 1);
    private final Setting.Bool autoTool = bool("Auto tool", true);
    private final Setting.Bool throughWalls = bool("Through walls", false);
    private final Setting.Bool keepFloor = bool("Keep floor", true);

    private final UtilBreak breaker = new UtilBreak();

    public Nuker() {
        super("Nuker", "Automatically destroys all blocks in your area. Be careful!", Category.WORLD);
    }

    @Override public void onDisable() { breaker.stop(); }

    @Override public void onWorldChange() { breaker.stop(); }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready() || mc.player.isUsingItem()) {
            breaker.stop();
            return;
        }
        List<BlockPos> candidates = collect();
        BlockPos current = breaker.target();
        if (current != null && !candidates.contains(current)) {
            breaker.stop();
            current = null;
        }

        int instantBudget = perTick.asInt();
        BlockPos slow = current;
        for (BlockPos pos : candidates) {
            if (instantBudget > 0 && UtilBreak.isInstant(pos)) {
                UtilBreak.hitOnce(pos);
                instantBudget--;
            } else if (slow == null) {
                slow = pos;
            }
        }
        if (slow != null && !UtilBreak.isInstant(slow)) {
            if (autoTool.get()) breaker.selectTool(mc.level.getBlockState(slow));
            breaker.mine(slow);
        } else if (breaker.isBreaking()) {
            breaker.stop();
        }
    }

    private List<BlockPos> collect() {
        List<BlockPos> list = new ArrayList<>();
        Vec3 eye = mc.player.getEyePosition();
        double r = Math.min(range.get(), UtilBuild.reach());
        double rSqr = r * r;
        BlockPos feet = mc.player.blockPosition();
        int ri = (int) Math.ceil(r);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    if (eye.distanceToSqr(Vec3.atCenterOf(cursor)) > rSqr) continue;
                    if (!UtilBreak.isBreakable(cursor)) continue;
                    if (!accepts(cursor, feet)) continue;
                    if (!throughWalls.get() && !visible(eye, cursor)) continue;
                    list.add(cursor.immutable());
                }
            }
        }
        list.sort(Comparator.comparingDouble(p -> eye.distanceToSqr(Vec3.atCenterOf(p))));
        return list;
    }

    private boolean accepts(BlockPos pos, BlockPos feet) {
        if (keepFloor.get() && pos.getX() == feet.getX() && pos.getZ() == feet.getZ() && pos.getY() < feet.getY()) {
            return false;
        }
        BlockState state = mc.level.getBlockState(pos);
        if (filter.is("Ores")) {
            return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().endsWith("_ore");
        }
        if (filter.is("Above feet")) return pos.getY() >= feet.getY();
        if (filter.is("Layer")) return pos.getY() == feet.getY();
        return true;
    }

    private boolean visible(Vec3 eye, BlockPos pos) {
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, Vec3.atCenterOf(pos),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    @Override public String tag() { return filter.get(); }
}
