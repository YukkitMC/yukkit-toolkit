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
 * Higher-resolution terrain preview: one block per pixel instead of sampling
 * every fourth block. A bounded row budget prevents sudden CPU spikes.
 *
 * The minimap uses cached colors and compact horizontal fill runs: nothing
 * requests new chunks or performs world queries during UI rendering.
 */
public final class ToolkitMap {
    private static final int CELLS = 88;
    private static final int[] PIXELS = new int[CELLS * CELLS];
    private static List<Marker> MARKERS = List.of();
    private static Object lastLevel;
    private static int ticks;
    private static int centerX;
    private static int centerZ;
    private static int nextRow = CELLS;
    private static boolean ready;

    public record Marker(int dx, int dz, boolean teammate) {}
    private ToolkitMap() {}

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            ready = false;
            lastLevel = null;
            nextRow = CELLS;
            MARKERS = List.of();
            return;
        }
        if (!ToolkitConfig.INSTANCE.minimap && !ToolkitConfig.INSTANCE.mapPreview) return;

        int playerX = mc.player.getBlockX();
        int playerZ = mc.player.getBlockZ();
        if (mc.level != lastLevel || (!ready && nextRow == CELLS)
                || (nextRow >= CELLS && Math.abs(playerX - centerX) + Math.abs(playerZ - centerZ) >= 4)) {
            lastLevel = mc.level;
            centerX = playerX;
            centerZ = playerZ;
            nextRow = 0;
            ready = true;
            java.util.Arrays.fill(PIXELS, 0xFF232832);
        }

        // At most 6 rows * 88 block samples per tick. No on-frame scans.
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int limit = Math.min(CELLS, nextRow + 6);
        for (int row = nextRow; row < limit; row++) {
            int bz = centerZ + row - CELLS / 2;
            LevelChunk lastChunk = null;
            int lastChunkX = Integer.MIN_VALUE;
            for (int col = 0; col < CELLS; col++) {
                int bx = centerX + col - CELLS / 2;
                int cx = Math.floorDiv(bx, 16);
                int cz = Math.floorDiv(bz, 16);
                if (cx != lastChunkX) {
                    var found = mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                    lastChunk = found instanceof LevelChunk loaded ? loaded : null;
                    lastChunkX = cx;
                }
                int color = 0xFF232832;
                if (lastChunk != null) {
                    int highest = lastChunk.getHeight(Heightmap.Types.WORLD_SURFACE, bx & 15, bz & 15);
                    if (highest > lastChunk.getMinY()) {
                        BlockState state = lastChunk.getBlockState(pos.set(bx, highest - 1, bz));
                        color = terrainColor(state, highest);
                    }
                }
                PIXELS[row * CELLS + col] = color;
            }
        }
        nextRow = limit;

        if (++ticks % 5 == 0) {
            List<Marker> found = new ArrayList<>();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player player) || player == mc.player) continue;
                int dx = player.getBlockX() - centerX;
                int dz = player.getBlockZ() - centerZ;
                if (Math.abs(dx) < CELLS / 2 && Math.abs(dz) < CELLS / 2) {
                    found.add(new Marker(dx, dz, TeamNames.matches(player)));
                }
            }
            MARKERS = List.copyOf(found);
        }
    }

    private static int terrainColor(BlockState state, int elevation) {
        int base;
        if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)) base = 0xFF3674B2;
        else if (state.is(Blocks.LAVA)) base = 0xFFF49B38;
        else if (state.is(Blocks.GRASS_BLOCK)) base = 0xFF558F47;
        else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) base = 0xFFC8BB80;
        else if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)) base = 0xFFE2E5E8;
        else if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.GRAVEL)) base = 0xFF818991;
        else if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)) base = 0xFF387C44;
        else if (state.isAir()) base = 0xFF2B3341;
        else base = 0xFF83765A;

        // Restrained 4-level height shading adds relief without coarse pixels.
        int shade = Math.floorMod(elevation / 3, 4) * 6 - 9;
        int r = Math.max(0, Math.min(255, ((base >> 16) & 255) + shade));
        int g = Math.max(0, Math.min(255, ((base >> 8) & 255) + shade));
        int b = Math.max(0, Math.min(255, (base & 255) + shade));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public static void render(GuiGraphicsExtractor g, int x, int y, int tile) {
        int side = CELLS * tile;
        g.fill(x - 3, y - 16, x + side + 3, y + side + 3, 0xDD101010);
        g.text(Minecraft.getInstance().font, "MAP  " + centerX + ", " + centerZ,
                x, y - 12, 0xFFFFFFFF, true);
        if (!ready) {
            g.text(Minecraft.getInstance().font, "Loading terrain...", x, y + 8, 0xFFBBBBBB, true);
            return;
        }
        // Merge consecutive pixels of the same color into horizontal runs.
        for (int row = 0; row < CELLS; row++) {
            int col = 0;
            while (col < CELLS) {
                int current = PIXELS[row * CELLS + col];
                int end = col + 1;
                while (end < CELLS && PIXELS[row * CELLS + end] == current) end++;
                g.fill(x + col * tile, y + row * tile, x + end * tile, y + (row + 1) * tile, current);
                col = end;
            }
        }
        int middle = (CELLS / 2) * tile;
        g.fill(x + middle - 2, y + middle - 2, x + middle + 2, y + middle + 2, 0xFFFFFFFF);
        for (Marker marker : MARKERS) {
            int mx = x + middle + marker.dx * tile;
            int my = y + middle + marker.dz * tile;
            if (mx < x || mx >= x + side || my < y || my >= y + side) continue;
            g.fill(mx - 2, my - 2, mx + 2, my + 2,
                    marker.teammate ? 0xFF28E2FF : 0xFFF15E5E);
        }
    }

    public static int centerX() { return centerX; }
    public static int centerZ() { return centerZ; }
}
