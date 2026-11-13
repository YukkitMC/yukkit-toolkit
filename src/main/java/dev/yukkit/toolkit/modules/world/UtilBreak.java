package dev.yukkit.toolkit.modules.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tracks one block that is being mined over several ticks (survival) and offers
 * tool selection. Use one instance per module.
 */
public final class UtilBreak {
    private static final Minecraft mc = Minecraft.getInstance();

    private BlockPos target;
    private Direction face = Direction.UP;
    private int swingTimer;
    private int previousSlot = -1;

    public BlockPos target() { return target; }

    public boolean isBreaking() { return target != null; }

    /** True when the block breaks in a single hit (creative, or instant mining speed). */
    public static boolean isInstant(BlockPos pos) {
        if (mc.player == null || mc.level == null) return false;
        if (mc.player.hasInfiniteMaterials()) return true;
        BlockState state = mc.level.getBlockState(pos);
        return !state.isAir() && state.getDestroyProgress(mc.player, mc.level, pos) >= 1.0f;
    }

    /** Mining hardness check: unbreakable blocks (bedrock, barriers, ...) report a negative value. */
    public static boolean isBreakable(BlockPos pos) {
        if (mc.level == null) return false;
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;
        return state.getDestroySpeed(mc.level, pos) >= 0.0f;
    }

    /** One hit now, regardless of progress (for instant blocks). */
    public static void hitOnce(BlockPos pos) {
        if (mc.gameMode == null || mc.player == null) return;
        Direction face = UtilBuild.faceTowardPlayer(pos);
        mc.gameMode.startDestroyBlock(pos, face);
        mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
    }

    /** Continues (or starts) mining {@code pos}; call every tick while it is the active target. */
    public void mine(BlockPos pos) {
        if (mc.gameMode == null || mc.player == null) return;
        if (target == null || !target.equals(pos)) {
            stopTarget();
            target = pos.immutable();
            face = UtilBuild.faceTowardPlayer(pos);
            mc.gameMode.startDestroyBlock(target, face);
        } else {
            face = UtilBuild.faceTowardPlayer(pos);
            mc.gameMode.continueDestroyBlock(target, face);
        }
        if (++swingTimer >= 4) {
            swingTimer = 0;
            mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
        }
    }

    private void stopTarget() {
        if (target != null && mc.gameMode != null) mc.gameMode.stopDestroyBlock();
        target = null;
    }

    /** Stops mining and puts the hotbar selection back. */
    public void stop() {
        stopTarget();
        restoreSlot();
    }

    // ------------------------------------------------------------- tools
    /** Hotbar slot with the fastest tool for {@code state}; the held slot wins ties. */
    public static int bestToolSlot(BlockState state) {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        int current = inv.getSelectedSlot();
        float bestSpeed = speed(inv.getItem(current), state);
        int best = current;
        for (int i = 0; i < 9; i++) {
            float s = speed(inv.getItem(i), state);
            if (s > bestSpeed + 1.0e-3f) {
                bestSpeed = s;
                best = i;
            }
        }
        return best;
    }

    private static float speed(ItemStack stack, BlockState state) {
        return stack.isEmpty() ? 1.0f : stack.getDestroySpeed(state);
    }

    /** Selects the best tool for {@code state}, remembering the old slot for {@link #restoreSlot()}. */
    public void selectTool(BlockState state) {
        if (mc.player == null) return;
        int best = bestToolSlot(state);
        var inv = mc.player.getInventory();
        if (best >= 0 && best != inv.getSelectedSlot()) {
            if (previousSlot < 0) previousSlot = inv.getSelectedSlot();
            inv.setSelectedSlot(best);
        }
    }

    public void restoreSlot() {
        if (previousSlot >= 0 && mc.player != null) mc.player.getInventory().setSelectedSlot(previousSlot);
        previousSlot = -1;
    }
}
