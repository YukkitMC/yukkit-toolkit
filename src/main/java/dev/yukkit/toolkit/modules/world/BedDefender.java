package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Covers every open side of nearby beds with blocks. */
public final class BedDefender extends Module {
    private final Setting.Num range = num("Range", 4.5, 1.0, 6.0, 0.5);
    private final Setting.Num delay = num("Delay", 1, 0, 6, 1);
    private final Setting.Num perTick = num("Blocks per tick", 1, 1, 3, 1);
    private final Setting.Bool top = bool("Cover top", true);

    private int cooldown;

    public BedDefender() {
        super("BedDefender", "Automatically places blocks to defend your bed in BedWars.", Category.WORLD);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready()) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        int slot = UtilBuild.findSlot(UtilBuild::isBuildBlock);
        if (slot == UtilBuild.NONE) return;

        Vec3 eye = mc.player.getEyePosition();
        double r = Math.min(range.get() + 2.0, 8.0);
        int ri = (int) Math.ceil(r);
        BlockPos feet = mc.player.blockPosition();
        Set<BlockPos> open = new LinkedHashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    if (!(mc.level.getBlockState(cursor).getBlock() instanceof BedBlock)) continue;
                    for (Direction d : UtilBuild.SUPPORT_ORDER) {
                        if (d == Direction.UP && !top.get()) continue;
                        BlockPos side = cursor.relative(d);
                        if (UtilBuild.isReplaceable(side)) open.add(side.immutable());
                    }
                }
            }
        }
        double reachSqr = Math.min(range.get(), UtilBuild.reach()) * Math.min(range.get(), UtilBuild.reach());
        List<BlockPos> targets = new ArrayList<>(open);
        targets.removeIf(p -> eye.distanceToSqr(Vec3.atCenterOf(p)) > reachSqr);
        targets.sort(Comparator.comparingDouble(p -> eye.distanceToSqr(Vec3.atCenterOf(p))));
        if (UtilBuild.placeAll(targets, slot, perTick.asInt(), false) > 0) cooldown = delay.asInt();
    }
}
