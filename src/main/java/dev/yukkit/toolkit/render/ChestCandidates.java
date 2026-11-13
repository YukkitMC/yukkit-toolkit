package dev.yukkit.toolkit.render;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Incrementally scans only client-loaded chunks for selected block entities.
 * This is a local storage visualizer, never a remote chunk or loot query.
 */
public final class ChestCandidates {
    public static final int MAX_CHUNK_RADIUS = 32;
    public static final int MAX_RENDER_STATES_PER_FRAME = 192;
    private static final int SCAN_CHUNKS_PER_TICK = 64;
    private static final int MAX_CHESTS = 1024;
    private static final int MAX_EXTRAS = 256;
    private static final List<Offset> OFFSETS = offsets();

    public record Extra(BlockPos pos, boolean shulker) {}
    private record Offset(int x, int z) {
        int distSq() { return x*x + z*z; }
    }

    private static Object levelMarker;
    private static int centerChunkX, centerChunkZ, nextChunk;
    private static List<BlockPos> pending = new ArrayList<>();
    private static Set<BlockPos> pendingSet = new HashSet<>();
    private static List<Extra> pendingExtras = new ArrayList<>();
    private static volatile List<BlockPos> positions = List.of();
    private static volatile List<Extra> extras = List.of();
    private static volatile int lastForcedExtracted;

    private ChestCandidates() {}

    private static List<Offset> offsets() {
        List<Offset> offsets = new ArrayList<>();
        for (int z=-MAX_CHUNK_RADIUS; z<=MAX_CHUNK_RADIUS; z++) {
            for (int x=-MAX_CHUNK_RADIUS; x<=MAX_CHUNK_RADIUS; x++) {
                offsets.add(new Offset(x,z));
            }
        }
        offsets.sort(Comparator.comparingInt(Offset::distSq));
        return List.copyOf(offsets);
    }

    public static List<BlockPos> positions() { return positions; }
    public static List<Extra> extras() { return extras; }
    public static int lastForcedExtracted() { return lastForcedExtracted; }
    public static void reportForcedExtracted(int n) { lastForcedExtracted = n; }

    public static boolean inRange(BlockPos pos, BlockPos player) {
        long max = (long)ToolkitConfig.sanitizeStorageRange(ToolkitConfig.INSTANCE.storageRangeChunks)*16 + 16;
        return Math.abs((long)pos.getX()-player.getX()) <= max
                && Math.abs((long)pos.getZ()-player.getZ()) <= max;
    }

    private static boolean selectedChest(Block block, ToolkitConfig config) {
        if (block == Blocks.CHEST) return config.storageChest;
        if (block == Blocks.TRAPPED_CHEST) return config.storageTrapped;
        if (block == Blocks.ENDER_CHEST) return config.storageEnder;
        return false;
    }

    public static boolean selectedChest(Block block) {
        return selectedChest(block, ToolkitConfig.INSTANCE);
    }

    private static void reset() {
        positions=List.of();
        extras=List.of();
        pending=new ArrayList<>();
        pendingSet=new HashSet<>();
        pendingExtras=new ArrayList<>();
        nextChunk=0;
        lastForcedExtracted=0;
        levelMarker=null;
    }

    public static void tick(Minecraft client) {
        ToolkitConfig config=ToolkitConfig.INSTANCE;
        if (client.level==null || client.player==null ||
                (!config.chestXray && !config.storageShulkers && !config.storageHoppers)) {
            reset(); return;
        }

        int cx=Math.floorDiv(client.player.getBlockX(),16);
        int cz=Math.floorDiv(client.player.getBlockZ(),16);
        if(client.level!=levelMarker || Math.abs(cx-centerChunkX)>=2 || Math.abs(cz-centerChunkZ)>=2) {
            reset();
            levelMarker=client.level;
            centerChunkX=cx;
            centerChunkZ=cz;
        }

        int radius=ToolkitConfig.sanitizeStorageRange(config.storageRangeChunks);
        int processed=0;
        BlockPos player=client.player.blockPosition();
        while(nextChunk<OFFSETS.size() && processed<SCAN_CHUNKS_PER_TICK) {
            Offset offset=OFFSETS.get(nextChunk++);
            if(Math.abs(offset.x())>radius || Math.abs(offset.z())>radius) continue;
            processed++;
            var chunk=client.level.getChunkSource().getChunk(cx+offset.x(),cz+offset.z(),
                    ChunkStatus.FULL,false);
            if(!(chunk instanceof LevelChunk loaded)) continue;
            for(BlockPos pos:loaded.getBlockEntities().keySet()) {
                if(!inRange(pos,player)) continue;
                Block block=loaded.getBlockState(pos).getBlock();
                if(config.chestXray && selectedChest(block,config) && pending.size()<MAX_CHESTS) {
                    if(pendingSet.add(pos)) pending.add(pos.immutable());
                } else if(pendingExtras.size()<MAX_EXTRAS) {
                    boolean shulker=block instanceof ShulkerBoxBlock;
                    if ((shulker && config.storageShulkers) ||
                            (block==Blocks.HOPPER && config.storageHoppers)) {
                        pendingExtras.add(new Extra(pos.immutable(),shulker));
                    }
                }
            }
        }
        if(nextChunk>=OFFSETS.size()) {
            positions=List.copyOf(pending);
            extras=List.copyOf(pendingExtras);
            nextChunk=0;
            pending=new ArrayList<>();
            pendingSet=new HashSet<>();
            pendingExtras=new ArrayList<>();
        }
    }
}
