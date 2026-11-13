package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Places blocks into liquid source blocks within reach. */
public final class LiquidFiller extends Module {
    private final Setting.Num range = num("Range", 4.0, 1.0, 6.0, 0.5);
    private final Setting.Choice liquid = choice("Liquid", "Lava", "Lava", "Water", "Both");
    private final Setting.Num delay = num("Delay", 1, 0, 6, 1);
    private final Setting.Num perTick = num("Blocks per tick", 1, 1, 3, 1);

    private int cooldown;

    public LiquidFiller() {
        super("LiquidFiller", "Places blocks inside of liquid source blocks within range of you.", Category.WORLD);
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
        double r = Math.min(range.get(), UtilBuild.reach());
        int ri = (int) Math.ceil(r);
        BlockPos feet = mc.player.blockPosition();
        List<BlockPos> sources = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    if (eye.distanceToSqr(Vec3.atCenterOf(cursor)) > r * r) continue;
                    if (wanted(mc.level.getBlockState(cursor).getFluidState())) sources.add(cursor.immutable());
                }
            }
        }
        sources.sort(Comparator.comparingDouble(p -> eye.distanceToSqr(Vec3.atCenterOf(p))));
        if (UtilBuild.placeAll(sources, slot, perTick.asInt(), false) > 0) cooldown = delay.asInt();
    }

    private boolean wanted(FluidState fluid) {
        if (fluid.isEmpty() || !fluid.isSource()) return false;
        boolean water = fluid.is(FluidTags.WATER);
        boolean lava = fluid.is(FluidTags.LAVA);
        return (liquid.is("Water") && water) || (liquid.is("Lava") && lava) || (liquid.is("Both") && (water || lava));
    }

    @Override public String tag() { return liquid.get(); }
}
