package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Selects the fastest hotbar tool for the block you are mining. */
public final class AutoTool extends Module {
    private final Setting.Bool switchBack = bool("Switch back", true);
    private final Setting.Num holdTicks = num("Hold ticks", 5, 0, 40, 1);

    private int previousSlot = -1;
    private int ourSlot = -1;
    private int idle;

    public AutoTool() {
        super("AutoTool", "Automatically chooses the best tool in your inventory to mine a block.", Category.WORLD);
    }

    @Override public void onDisable() { restore(); }

    @Override public void onWorldChange() { previousSlot = -1; idle = 0; }

    @Override
    public void onTick() {
        if (!inGame() || mc.player.isSpectator() || mc.player.hasInfiniteMaterials()) return;
        var inv = mc.player.getInventory();
        if (mc.options.keyAttack.isDown() && mc.hitResult instanceof BlockHitResult hit) {
            BlockState state = mc.level.getBlockState(hit.getBlockPos());
            if (!state.isAir()) {
                idle = 0;
                int best = UtilBreak.bestToolSlot(state);
                if (best >= 0 && best != inv.getSelectedSlot()) {
                    if (previousSlot < 0) previousSlot = inv.getSelectedSlot();
                    inv.setSelectedSlot(best);
                    ourSlot = best;
                }
                return;
            }
        }
        if (previousSlot >= 0 && ++idle > holdTicks.asInt()) restore();
    }

    private void restore() {
        if (previousSlot >= 0 && switchBack.get() && hasWorld()
                && mc.player.getInventory().getSelectedSlot() == ourSlot) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
        ourSlot = -1;
        idle = 0;
    }
}
