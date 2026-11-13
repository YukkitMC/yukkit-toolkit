package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Compensates the vanilla mining penalties for being in the air or under water by advancing the local
 * break progress several times per tick. The server still applies its own timing.
 */
public final class NoSlowBreak extends Module {
    private final Setting.Bool air = bool("In air", true);
    private final Setting.Bool water = bool("Under water", true);

    public NoSlowBreak() {
        super("NoSlowBreak", "Automatically adjusts breaking speed when in negatively affecting situations.", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (!inGame() || mc.gameMode == null || mc.player.hasInfiniteMaterials()) return;
        if (!mc.options.keyAttack.isDown() || !(mc.hitResult instanceof BlockHitResult hit)) return;
        if (!mc.gameMode.isDestroying()) return;
        int factor = 1;
        if (air.get() && !mc.player.onGround()) factor *= 5;
        if (water.get() && mc.player.isUnderWater()) factor *= 5;
        int extra = Math.min(factor, 25) - 1;
        for (int i = 0; i < extra; i++) {
            if (!mc.gameMode.isDestroying() || mc.level.getBlockState(hit.getBlockPos()).isAir()) break;
            mc.gameMode.continueDestroyBlock(hit.getBlockPos(), hit.getDirection());
        }
    }
}
