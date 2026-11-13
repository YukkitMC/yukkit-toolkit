package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Places a block under you when you fall and there is nothing below to catch you. */
public final class Clutch extends Module {
    private final Setting.Num minFall = num("Min fall distance", 3.0, 0.5, 20.0, 0.5);
    private final Setting.Num scanDepth = num("Void scan depth", 24, 4, 64, 1);
    private final Setting.Bool onlyVoid = bool("Only when nothing below", true);
    private final Setting.Bool airPlace = bool("Air place", true);

    private int cooldown;

    public Clutch() {
        super("Clutch", "Automatically places a block beneath you when falling into the void.", Category.WORLD);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!UtilBuild.ready() || mc.player == null) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (mc.player.onGround() || mc.player.isFallFlying() || mc.player.getAbilities().flying
                || mc.player.isInWater() || mc.player.isPassenger()) return;
        Vec3 motion = mc.player.getDeltaMovement();
        if (motion.y > -0.3 || mc.player.fallDistance < minFall.asFloat()) return;

        BlockPos below = BlockPos.containing(mc.player.getX(), mc.player.getBoundingBox().minY - 0.5, mc.player.getZ());
        if (onlyVoid.get() && groundWithin(below, scanDepth.asInt())) return;

        int slot = UtilBuild.findSlot(UtilBuild::isBuildBlock);
        if (slot == UtilBuild.NONE) return;
        if (UtilBuild.placeAt(below, slot, airPlace.get())) cooldown = 2;
    }

    /** True when anything other than air lies in the column below {@code start}. */
    private boolean groundWithin(BlockPos start, int depth) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= depth; i++) {
            cursor.set(start.getX(), start.getY() - i, start.getZ());
            if (!mc.level.getBlockState(cursor).isAir()) return true;
        }
        return false;
    }
}
