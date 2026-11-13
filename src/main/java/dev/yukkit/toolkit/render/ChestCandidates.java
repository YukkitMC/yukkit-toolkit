package dev.yukkit.toolkit.render;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.modules.XrayController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Incremental scan of locally received chunks for real chest block entities.
 *
 * The radius is the normal vanilla maximum of 32 chunks in every horizontal
 * direction. A server may send fewer chunks, and no missing chunk is loaded
 * or requested. Scanning is spread across ticks to avoid client freezes.
 */
public final class ChestCandidates {
    public static final int MAX_CHUNK_RADIUS = 32;
    private static final int BLOCK_MARGIN = 16;
    private static final int SCAN_CHUNKS_PER_TICK = 64;
    private static final int MAX_CHESTS = 1024;
    private static final List<Offset> OFFSETS = offsets();

    private static Object currentLevel;
    private static int centerChunkX;
    private static int centerChunkZ;
    private static int nextChunk;
    private static List<BlockPos> pending = new ArrayList<>();
    private static Set<BlockPos> pendingSet = new HashSet<>();
    private static volatile List<BlockPos> positions = List.of();

    private record Offset(int x, int z) {
        int distanceSquared() { return x * x + z * z; }
    }

    private ChestCandidates() {}

    private static List<Offset> offsets() {
        List<Offset> result = new ArrayList<>();
        for (int dz = -MAX_CHUNK_RADIUS; dz <= MAX_CHUNK_RADIUS; dz++) {
            for (int dx = -MAX_CHUNK_RADIUS; dx <= MAX_CHUNK_RADIUS; dx++) {
                result.add(new Offset(dx, dz));
            }
        }
        result.sort(Comparator.comparingInt(Offset::distanceSquared));
        return List.copyOf(result);
    }

    public static List<BlockPos> positions() { return positions; }

    public static boolean inRange(BlockPos chest, BlockPos player) {
        int limit = MAX_CHUNK_RADIUS * 16 + BLOCK_MARGIN;
        return Math.abs((long) chest.getX() - player.getX()) <= limit
                && Math.abs((long) chest.getZ() - player.getZ()) <= limit;
    }

    private static void reset() {
        positions = List.of();
        pending = new ArrayList<>();
        pendingSet = new HashSet<>();
        currentLevel = null;
        nextChunk = 0;
    }

    public static void tick(Minecraft client) {
        if (!ToolkitConfig.INSTANCE.chestXray || client.level == null || client.player == null) {
            reset();
            return;
        }

        int px = Math.floorDiv(client.player.getBlockX(), 16);
        int pz = Math.floorDiv(client.player.getBlockZ(), 16);
        if (client.level != currentLevel || Math.abs(px - centerChunkX) >= 2
                || Math.abs(pz - centerChunkZ) >= 2) {
            reset();
            currentLevel = client.level;
            centerChunkX = px;
            centerChunkZ = pz;
        }

        int last = Math.min(OFFSETS.size(), nextChunk + SCAN_CHUNKS_PER_TICK);
        for (int i = nextChunk; i < last; i++) {
            Offset off = OFFSETS.get(i);
            // false = do not generate, request or load chunks; read cached data only.
            var candidate = client.level.getChunkSource().getChunk(
                    centerChunkX + off.x(), centerChunkZ + off.z(), ChunkStatus.FULL, false);
            if (!(candidate instanceof LevelChunk chunk)) continue;

            for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                if (!inRange(pos, client.player.blockPosition())) continue;
                if (!XrayController.isChest(chunk.getBlockState(pos).getBlock())) continue;
                if (pendingSet.add(pos)) {
                    pending.add(pos.immutable());
                    if (pending.size() >= MAX_CHESTS) break;
                }
            }
            if (pending.size() >= MAX_CHESTS) {
                last = OFFSETS.size();
                break;
            }
        }
        nextChunk = last;
        if (nextChunk >= OFFSETS.size()) {
            positions = List.copyOf(pending);
            pending = new ArrayList<>();
            pendingSet = new HashSet<>();
            nextChunk = 0;
        }
    }
}
