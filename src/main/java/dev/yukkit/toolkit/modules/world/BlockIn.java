package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;

import java.util.List;

/** Walls yourself in: two layers of walls and a roof. */
public final class BlockIn extends Module {
    private final Setting.Num delay = num("Delay", 1, 0, 5, 1);
    private final Setting.Num perTick = num("Blocks per tick", 2, 1, 4, 1);
    private final Setting.Bool roof = bool("Roof", true);
    private final Setting.Bool floor = bool("Fill floor", false);
    private final Setting.Bool autoDisable = bool("Disable when done", true);

    private int cooldown;
    private int stuck;

    public BlockIn() { super("BlockIn", "Builds blocks to cover yourself.", Category.WORLD); }

    @Override public void onEnable() { cooldown = 0; stuck = 0; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready()) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        List<BlockPos> targets = UtilBuild.cage(mc.player.blockPosition(), floor.get(), 2, roof.get());
        if (UtilBuild.allFilled(targets)) {
            if (autoDisable.get()) setEnabled(false);
            return;
        }
        int slot = UtilBuild.findSlot(UtilBuild::isBuildBlock);
        if (slot == UtilBuild.NONE) return;
        int placed = UtilBuild.placeAll(targets, slot, perTick.asInt(), false);
        cooldown = delay.asInt();
        // Give up when nothing could be placed for a while (no support, entities in the way, ...).
        stuck = placed > 0 ? 0 : stuck + 1;
        if (stuck > 40 && autoDisable.get()) setEnabled(false);
    }
}
