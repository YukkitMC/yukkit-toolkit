package dev.yukkit.toolkit.map;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.team.TeamNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Small, bounded minimap from already-loaded client chunks.
 * No chunk loading, downloads, network traffic or world-file scans.
 */
public final class ToolkitMap {
    private static final int CELLS = 25;
    private static final int STEP = 4;
    private static final int[] PIXELS = new int[CELLS * CELLS];
    private static List<Marker> MARKERS = List.of();
    private static int ticks;
    private static int centerX;
    private static int centerZ;
    private static boolean ready;

    public record Marker(int dx, int dz, boolean teammate) {}

    private ToolkitMap() {}

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            ready = false;
            MARKERS = List.of();
            return;
        }
        if (!ToolkitConfig.INSTANCE.minimap && !ToolkitConfig.INSTANCE.mapPreview) return;
        if (++ticks % 12 != 0) return;

        centerX = mc.player.getBlockX();
        centerZ = mc.player.getBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int row = 0; row < CELLS; row++) {
            for (int col = 0; col < CELLS; col++) {
                int bx = centerX + (col - CELLS / 2) * STEP;
                int bz = centerZ + (row - CELLS / 2) * STEP;
                var chunk = mc.level.getChunkSource().getChunk(Math.floorDiv(bx, 16), Math.floorDiv(bz, 16), ChunkStatus.FULL, false);
                int color = 0xFF151C27;
                if (chunk instanceof LevelChunk loaded) {
                    int topY = loaded.getHeight(Heightmap.Types.WORLD_SURFACE, bx & 15, bz & 15);
                    if (topY > loaded.getMinY()) {
                        BlockState state = loaded.getBlockState(pos.set(bx, topY - 1, bz));
                        color = surfaceColor(state, topY);
                    }
                }
                PIXELS[row * CELLS + col] = color;
            }
        }

        List<Marker> markers = new ArrayList<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Player player) || player == mc.player) continue;
            int dx = player.getBlockX() - centerX;
            int dz = player.getBlockZ() - centerZ;
            if (Math.abs(dx) <= CELLS / 2 * STEP && Math.abs(dz) <= CELLS / 2 * STEP) {
                markers.add(new Marker(dx, dz, TeamNames.matches(player)));
            }
        }
        MARKERS = List.copyOf(markers);
        ready = true;
    }

    private static int surfaceColor(BlockState s, int y) {
        if (s.is(Blocks.WATER)) return 0xFF306DA7;
        if (s.is(Blocks.LAVA)) return 0xFFF48B32;
        if (s.is(Blocks.GRASS_BLOCK)) return 0xFF4B9150;
        if (s.is(Blocks.SAND) || s.is(Blocks.RED_SAND)) return 0xFFC7BB7B;
        if (s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.SNOW)) return 0xFFDDDEE5;
        if (s.is(Blocks.STONE) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.GRAVEL)) return 0xFF747B86;
        if (s.is(Blocks.OAK_LEAVES) || s.is(Blocks.BIRCH_LEAVES) || s.is(Blocks.SPRUCE_LEAVES)) return 0xFF36744B;
        if (s.isAir()) return 0xFF1E2936;
        return y > 110 ? 0xFF9EAA98 : 0xFF696F54;
    }

    public static void render(GuiGraphicsExtractor g, int x, int y, int tile) {
        int size = CELLS * tile;
        g.fill(x - 4, y - 19, x + size + 4, y + size + 4, 0xDF11161F);
        g.text(Minecraft.getInstance().font, "YUKKIT MAP", x, y - 14, 0xFFEEEEEE, true);
        if (!ready) {
            g.text(Minecraft.getInstance().font, "Loading map...", x, y + 6, 0xFFC9C9C9, true);
            return;
        }
        for (int row = 0; row < CELLS; row++) {
            for (int col = 0; col < CELLS; col++) {
                int sx = x + col * tile;
                int sy = y + row * tile;
                g.fill(sx, sy, sx + tile, sy + tile, PIXELS[row * CELLS + col]);
            }
        }
        int midpoint = CELLS / 2 * tile + tile / 2;
        g.fill(x + midpoint - 2, y + midpoint - 2, x + midpoint + 2, y + midpoint + 2, 0xFFFFFFFF);
        for (Marker marker : MARKERS) {
            int mx = x + midpoint + marker.dx * tile / STEP;
            int my = y + midpoint + marker.dz * tile / STEP;
            if (mx < x || mx >= x + size || my < y || my >= y + size) continue;
            g.fill(mx - 2, my - 2, mx + 2, my + 2,
                    marker.teammate ? 0xFF00D7FF : 0xFFEA6C70);
        }
    }

    public static int centerX() { return centerX; }
    public static int centerZ() { return centerZ; }
}
