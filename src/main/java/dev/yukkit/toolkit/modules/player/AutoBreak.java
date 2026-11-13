package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoBreak extends Module {
    private final Setting.Bool correctTool = bool("OnlyCorrectTool", false);

    private boolean pressed;

    public AutoBreak() {
        super("AutoBreak", "Keeps mining the block your crosshair is on.", Category.PLAYER);
    }

    @Override public void onDisable() { release(); }
    @Override public void onWorldChange() { pressed = false; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gui.screen() != null) { release(); return; }
        if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            BlockState state = mc.level.getBlockState(hit.getBlockPos());
            boolean ok = !state.isAir() && state.getDestroySpeed(mc.level, hit.getBlockPos()) >= 0f;
            if (ok && correctTool.get() && state.requiresCorrectToolForDrops()
                    && !mc.player.getMainHandItem().isCorrectToolForDrops(state)) ok = false;
            if (ok) {
                if (!mc.options.keyAttack.isDown()) {
                    mc.options.keyAttack.setDown(true);
                    pressed = true;
                }
                return;
            }
        }
        release();
    }

    private void release() {
        if (pressed) {
            mc.options.keyAttack.setDown(false);
            pressed = false;
        }
    }
}
