package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Walls in the closest other player. */
public final class BlockTrap extends Module {
    private final Setting.Num range = num("Range", 4.5, 1.0, 6.0, 0.5);
    private final Setting.Num delay = num("Delay", 1, 0, 5, 1);
    private final Setting.Num perTick = num("Blocks per tick", 2, 1, 4, 1);
    private final Setting.Bool roof = bool("Roof", true);

    private int cooldown;

    public BlockTrap() { super("BlockTrap", "Traps enemies in blocks.", Category.WORLD); }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready()) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        Player target = null;
        double best = range.get() * range.get();
        for (Player other : mc.level.players()) {
            if (other == mc.player || !other.isAlive() || other.isSpectator()) continue;
            double d = other.distanceToSqr(mc.player);
            if (d < best) {
                best = d;
                target = other;
            }
        }
        if (target == null) return;
        int slot = UtilBuild.findSlot(UtilBuild::isBuildBlock);
        if (slot == UtilBuild.NONE) return;
        List<BlockPos> cage = UtilBuild.cage(target.blockPosition(), false, 2, roof.get());
        // Never wall ourselves in by accident.
        cage.removeIf(p -> p.equals(mc.player.blockPosition()) || p.equals(mc.player.blockPosition().above()));
        if (UtilBuild.placeAll(cage, slot, perTick.asInt(), false) > 0) cooldown = delay.asInt();
    }
}
