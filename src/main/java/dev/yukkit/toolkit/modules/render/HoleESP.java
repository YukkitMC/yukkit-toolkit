package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Highlights 1x1 pits whose walls and floor are bedrock / obsidian. */
public final class HoleESP extends Module {
    private final Setting.Num radius = num("Radius", 8, 3, 16, 1);
    private final Setting.Bool bedrock = bool("Bedrock", true);
    private final Setting.Bool obsidian = bool("Obsidian", true);
    private final Setting.Choice style = choice("Style", "Filled", "Outline", "Filled", "Both");

    private record Hole(BlockPos pos, boolean safe) {}

    private List<Hole> holes = new ArrayList<>();
    private int timer;

    public HoleESP() { super("HoleESP", "Marks safe holes (bedrock / obsidian pits).", Category.RENDER); }

    @Override public void onEnable() { holes = new ArrayList<>(); timer = 0; }
    @Override public void onDisable() { holes = new ArrayList<>(); }
    @Override public void onWorldChange() { holes = new ArrayList<>(); timer = 0; }

    /** 0 = not acceptable, 1 = obsidian, 2 = bedrock. */
    private int quality(BlockPos p) {
        Block b = mc.level.getBlockState(p).getBlock();
        if (b == Blocks.BEDROCK) return bedrock.get() ? 2 : 0;
        if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN) return obsidian.get() ? 1 : 0;
        return 0;
    }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (timer-- > 0) return;
        timer = 10;
        List<Hole> out = new ArrayList<>();
        BlockPos c = mc.player.blockPosition();
        int r = radius.asInt();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -4; y <= 4; y++) {
                    BlockPos p = c.offset(x, y, z);
                    if (!mc.level.hasChunkAt(p.getX(), p.getZ())) continue;
                    if (!mc.level.getBlockState(p).isAir()) continue;
                    if (!mc.level.getBlockState(p.above()).isAir()) continue;
                    int worst = quality(p.below());
                    if (worst == 0) continue;
                    boolean ok = true;
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        int q = quality(p.relative(d));
                        if (q == 0) { ok = false; break; }
                        worst = Math.min(worst, q);
                    }
                    if (!ok) continue;
                    out.add(new Hole(p, worst == 2));
                    if (out.size() >= 100) { holes = out; return; }
                }
            }
        }
        holes = out;
    }

    @Override public void onWorld() {
        for (Hole h : holes) {
            AABB box = new AABB(h.pos().getX(), h.pos().getY(), h.pos().getZ(),
                    h.pos().getX() + 1, h.pos().getY() + 0.1, h.pos().getZ() + 1);
            UtilDraw.box(box, h.safe() ? 0x30FF60 : 0xFFB020, style.get(), true);
        }
    }
}
