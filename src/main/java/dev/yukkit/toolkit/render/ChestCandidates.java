package dev.yukkit.toolkit.render;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.modules.XrayController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache known client-side chest locations once a second, never requesting
 * unloaded chunks. Used to extract chests that vanilla section occlusion
 * would otherwise skip, so their real textured model is still submitted.
 */
public final class ChestCandidates {
    private static final int CHUNK_RADIUS = 4;
    private static final int DISTANCE_SQUARED = 64 * 64;
    private static final int MAX_CHESTS = 96;

    private static Object currentLevel;
    private static int ticks;
    private static volatile List<BlockPos> positions = List.of();

    private ChestCandidates() {}

    public static List<BlockPos> positions() { return positions; }

    public static void tick(Minecraft client) {
        if (!ToolkitConfig.INSTANCE.chestXray || client.level == null || client.player == null) {
            positions = List.of();
            currentLevel = null;
            ticks = 0;
            return;
        }
        if (client.level != currentLevel) {
            currentLevel = client.level;
            positions = List.of();
            ticks = 20;
        }
        if (++ticks < 20) return;
        ticks = 0;

        BlockPos player = client.player.blockPosition();
        int chunkX = Math.floorDiv(player.getX(), 16);
        int chunkZ = Math.floorDiv(player.getZ(), 16);
        List<BlockPos> found = new ArrayList<>();

        outer:
        for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
            for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
                var chunk = client.level.getChunkSource().getChunk(
                    chunkX + x, chunkZ + z, ChunkStatus.FULL, false);
                if (!(chunk instanceof LevelChunk loaded)) continue;
                for (BlockPos p : loaded.getBlockEntities().keySet()) {
                    if (p.distSqr(player) > DISTANCE_SQUARED) continue;
                    if (!XrayController.isChest(loaded.getBlockState(p).getBlock())) continue;
                    found.add(p.immutable());
                    if (found.size() >= MAX_CHESTS) break outer;
                }
            }
        }
        positions = List.copyOf(found);
    }
}
