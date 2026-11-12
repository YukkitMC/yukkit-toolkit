package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Automatically mines only the block currently under the crosshair, within
 * ordinary reach. No pathfinding, chunk requests, or block selection.
 */
public final class AutoMiner {
    private static BlockPos target;
    private static Direction face;
    private static Object previousLevel;
    private static int swingCooldown;

    private AutoMiner() {}

    public static void stop(Minecraft mc) {
        if (target != null && mc.gameMode != null) mc.gameMode.stopDestroyBlock();
        target = null;
        face = null;
        previousLevel = null;
    }

    public static void tick(Minecraft mc) {
        if (mc.level != previousLevel) {
            stop(mc);
            previousLevel = mc.level;
        }
        if (!ToolkitConfig.INSTANCE.autoMiner || mc.player == null || mc.level == null || mc.gameMode == null
                || mc.gui.screen() != null || mc.player.isSpectator()
                || mc.player.isUsingItem() || !(mc.hitResult instanceof BlockHitResult hit)
                || mc.level.getBlockState(hit.getBlockPos()).isAir()) {
            stop(mc);
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (pos.distSqr(mc.player.blockPosition()) > 36.0) {
            stop(mc);
            return;
        }
        if (target == null || !target.equals(pos) || face != hit.getDirection()) {
            stop(mc);
            previousLevel = mc.level;
            target = pos.immutable();
            face = hit.getDirection();
            mc.gameMode.startDestroyBlock(target, face);
        } else {
            mc.gameMode.continueDestroyBlock(target, face);
        }
        if (++swingCooldown >= 6) {
            mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
            swingCooldown = 0;
        }
    }
}
