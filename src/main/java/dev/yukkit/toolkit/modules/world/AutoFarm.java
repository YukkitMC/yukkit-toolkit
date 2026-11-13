package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

/** Harvests fully grown crops within reach and plants seeds again. */
public final class AutoFarm extends Module {
    private final Setting.Num range = num("Range", 4.0, 1.0, 6.0, 0.5);
    private final Setting.Num delay = num("Delay", 2, 0, 10, 1);
    private final Setting.Bool replant = bool("Replant", true);
    private final Setting.Bool plant = bool("Plant empty soil", true);
    private final Setting.Bool melons = bool("Melons and pumpkins", true);
    private final Setting.Bool cane = bool("Sugar cane", false);

    private final UtilBreak breaker = new UtilBreak();
    private BlockPos pendingReplant;
    private Block pendingBlock;
    private int pendingAge;
    private int cooldown;

    public AutoFarm() { super("AutoFarm", "Automatically farms crops.", Category.WORLD); }

    @Override public void onEnable() { cooldown = 0; pendingReplant = null; pendingBlock = null; }

    @Override public void onDisable() { breaker.stop(); pendingReplant = null; pendingBlock = null; }

    @Override public void onWorldChange() { breaker.stop(); pendingReplant = null; pendingBlock = null; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready() || mc.player.isUsingItem()) {
            breaker.stop();
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Finish a replant of a crop that was just harvested.
        if (pendingReplant != null) {
            if (++pendingAge > 10) {
                pendingReplant = null;
            } else if (mc.level.getBlockState(pendingReplant).isAir()) {
                final Block wanted = pendingBlock;
                int slot = UtilBuild.findSlot(s -> s.getItem() instanceof BlockItem bi && bi.getBlock() == wanted);
                if (slot != UtilBuild.NONE) UtilBuild.placeAt(pendingReplant, slot, false);
                pendingReplant = null;
                cooldown = delay.asInt();
                return;
            } else {
                return; // block not removed yet, wait for the server
            }
        }

        Vec3 eye = mc.player.getEyePosition();
        double r = Math.min(range.get(), UtilBuild.reach());
        double rSqr = r * r;
        BlockPos feet = mc.player.blockPosition();
        int ri = (int) Math.ceil(r);

        BlockPos harvest = breaker.target();
        if (harvest != null && !(ripe(harvest) && eye.distanceToSqr(Vec3.atCenterOf(harvest)) <= rSqr)) {
            breaker.stop();
            harvest = null;
        }
        BlockPos soil = null;
        double bestHarvest = harvest == null ? Double.MAX_VALUE : -1.0;
        double bestSoil = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    double d = eye.distanceToSqr(Vec3.atCenterOf(cursor));
                    if (d > rSqr) continue;
                    if (d < bestHarvest && ripe(cursor)) {
                        bestHarvest = d;
                        harvest = cursor.immutable();
                    } else if (plant.get() && d < bestSoil && emptySoil(cursor)) {
                        bestSoil = d;
                        soil = cursor.immutable();
                    }
                }
            }
        }

        if (harvest != null) {
            BlockState state = mc.level.getBlockState(harvest);
            Block block = state.getBlock();
            if (UtilBreak.isInstant(harvest)) {
                UtilBreak.hitOnce(harvest);
                if (replant.get() && (block instanceof CropBlock || block instanceof NetherWartBlock)) {
                    pendingReplant = harvest;
                    pendingBlock = block;
                    pendingAge = 0;
                }
                cooldown = delay.asInt();
            } else {
                breaker.selectTool(state);
                breaker.mine(harvest);
            }
            return;
        }
        breaker.stop();
        if (soil != null) {
            BlockPos above = soil.above();
            boolean wart = !(mc.level.getBlockState(soil).getBlock() instanceof FarmlandBlock);
            Predicate<ItemStack> seed = wart
                    ? s -> s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof NetherWartBlock
                    : s -> s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CropBlock;
            int slot = UtilBuild.findSlot(seed);
            if (slot != UtilBuild.NONE && UtilBuild.placeAt(above, slot, false)) cooldown = delay.asInt();
        }
    }

    private boolean ripe(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof CropBlock crop) return crop.isMaxAge(state);
        if (b instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        if (melons.get() && (b instanceof PumpkinBlock || b == Blocks.MELON)) return true;
        return cane.get() && b instanceof SugarCaneBlock
                && mc.level.getBlockState(pos.below()).getBlock() instanceof SugarCaneBlock;
    }

    /** Farmland (or nether wart soil) with free space above it. */
    private boolean emptySoil(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        boolean farmland = state.getBlock() instanceof FarmlandBlock;
        if (!farmland && !state.is(BlockTags.SUPPORTS_NETHER_WART)) return false;
        return mc.level.getBlockState(pos.above()).isAir();
    }

    @Override
    public String tag() { return replant.get() ? "Replant" : ""; }
}
