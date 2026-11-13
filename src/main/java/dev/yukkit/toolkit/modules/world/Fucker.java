package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Breaks beds, cakes and dragon eggs that are within reach. */
public final class Fucker extends Module {
    private final Setting.Num range = num("Range", 4.5, 1.0, 6.0, 0.5);
    private final Setting.Bool beds = bool("Beds", true);
    private final Setting.Bool cakes = bool("Cakes", true);
    private final Setting.Bool eggs = bool("Dragon eggs", true);
    private final Setting.Bool throughWalls = bool("Through walls", true);
    private final Setting.Bool autoTool = bool("Auto tool", true);

    private final UtilBreak breaker = new UtilBreak();

    public Fucker() {
        super("Fucker", "Destroys or interacts with selected blocks around you.", Category.WORLD);
    }

    @Override public void onDisable() { breaker.stop(); }

    @Override public void onWorldChange() { breaker.stop(); }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready() || mc.player.isUsingItem()) {
            breaker.stop();
            return;
        }
        Vec3 eye = mc.player.getEyePosition();
        double r = Math.min(range.get(), UtilBuild.reach());
        double rSqr = r * r;

        BlockPos target = breaker.target();
        if (target != null && !(isTarget(target) && eye.distanceToSqr(Vec3.atCenterOf(target)) <= rSqr)) {
            breaker.stop();
            target = null;
        }
        if (target == null) {
            double best = Double.MAX_VALUE;
            BlockPos feet = mc.player.blockPosition();
            int ri = (int) Math.ceil(r);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dy = -ri; dy <= ri; dy++) {
                    for (int dz = -ri; dz <= ri; dz++) {
                        cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                        double d = eye.distanceToSqr(Vec3.atCenterOf(cursor));
                        if (d > rSqr || d >= best || !isTarget(cursor)) continue;
                        if (!throughWalls.get() && !visible(eye, cursor)) continue;
                        best = d;
                        target = cursor.immutable();
                    }
                }
            }
        }
        if (target == null) return;

        if (UtilBreak.isInstant(target)) {
            UtilBreak.hitOnce(target);
        } else {
            if (autoTool.get()) breaker.selectTool(mc.level.getBlockState(target));
            breaker.mine(target);
        }
    }

    private boolean isTarget(BlockPos pos) {
        Block block = mc.level.getBlockState(pos).getBlock();
        return (beds.get() && block instanceof BedBlock)
                || (cakes.get() && (block instanceof CakeBlock || block instanceof CandleCakeBlock))
                || (eggs.get() && block instanceof DragonEggBlock);
    }

    private boolean visible(Vec3 eye, BlockPos pos) {
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, Vec3.atCenterOf(pos),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
}
