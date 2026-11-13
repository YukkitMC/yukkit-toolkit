package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class AntiBounce extends Module {
    public AntiBounce() { super("AntiBounce", "Cancels the bounce from slime blocks and beds.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (UtilMove.busy(p) || UtilMove.jumpKey()) return;
        Vec3 m = p.getDeltaMovement();
        if (m.y < 0.2) return;
        BlockPos under = UtilMove.pos(p.getX(), p.getY() - 0.2, p.getZ());
        BlockState s = UtilMove.state(under);
        if (s.is(Blocks.SLIME_BLOCK) || s.is(BlockTags.BEDS)) {
            p.setDeltaMovement(m.x, 0.0, m.z);
        }
    }
}
