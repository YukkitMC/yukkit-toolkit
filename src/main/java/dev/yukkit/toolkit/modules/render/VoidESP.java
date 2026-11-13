package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Marks floor cells near you that have nothing beneath them all the way to the world bottom. */
public final class VoidESP extends Module {
    private final Setting.Num radius = num("Radius", 8, 2, 16, 1);
    private final Setting.Choice style = choice("Style", "Filled", "Outline", "Filled", "Both");

    private List<BlockPos> cells = new ArrayList<>();
    private int timer;
    private int plane;

    public VoidESP() { super("VoidESP", "Highlights holes that drop into the void.", Category.RENDER); }

    @Override public void onEnable() { cells = new ArrayList<>(); timer = 0; }
    @Override public void onDisable() { cells = new ArrayList<>(); }
    @Override public void onWorldChange() { cells = new ArrayList<>(); timer = 0; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (timer-- > 0) return;
        timer = 10;
        List<BlockPos> out = new ArrayList<>();
        BlockPos c = mc.player.blockPosition();
        int minY = mc.level.getMinY();
        int top = c.getY() - 1;
        plane = top;
        int r = radius.asInt();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = -r; x <= r && out.size() < 300; x++) {
            for (int z = -r; z <= r && out.size() < 300; z++) {
                int bx = c.getX() + x, bz = c.getZ() + z;
                if (!mc.level.hasChunkAt(bx, bz)) continue;
                boolean open = true;
                for (int y = top; y >= minY; y--) {
                    p.set(bx, y, bz);
                    if (!mc.level.getBlockState(p).isAir()) { open = false; break; }
                }
                if (open) out.add(new BlockPos(bx, top, bz));
            }
        }
        cells = out;
    }

    @Override public void onWorld() {
        for (BlockPos p : cells) {
            AABB box = new AABB(p.getX(), p.getY() + 0.9, p.getZ(), p.getX() + 1, p.getY() + 1.0, p.getZ() + 1);
            UtilDraw.box(box, 0xFF3030, style.get(), true);
        }
    }
}
