package dev.yukkit.toolkit.render;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.modules.XrayController;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Chest ESP with the vanilla Minecraft 26.3 Gizmos API.
 *
 * IMPORTANT: Do not allocate/upload GPU buffers or create a RenderPass inside
 * AFTER_TRANSLUCENT_TERRAIN. That callback runs with an active render pass
 * (especially with Iris), resulting in IllegalStateException on upload().
 *
 * BEFORE_GIZMOS only registers vanilla gizmos; Minecraft schedules their GPU
 * work at the appropriate point in the render frame.
 */
public final class ChestEspRenderer {
    private static final int CHUNK_RADIUS = 4;
    private static final int RANGE_BLOCKS = 64;
    private static final int MAX_BOXES = 96;
    private static final int RESCAN_INTERVAL_TICKS = 20;
    private static final int OUTLINE_COLOR = 0xFF20BFFF;
    private static final int FILL_COLOR = 0x3020BFFF;
    private static final GizmoStyle BOX_STYLE =
            GizmoStyle.strokeAndFill(OUTLINE_COLOR, 2.0f, FILL_COLOR);

    // Snapshot is replaced as a whole on the main client thread.
    private static volatile List<Box> boxes = List.of();
    private static Object currentLevel;
    private static int scanCountdown;

    private record Box(int x, int y, int z) {
        AABB bounds() {
            return new AABB(x + 0.05, y + 0.05, z + 0.05,
                            x + 0.95, y + 0.95, z + 0.95);
        }
    }

    private ChestEspRenderer() {}

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            if (!ToolkitConfig.INSTANCE.chestXray) return;
            for (Box box : boxes) {
                Gizmos.cuboid(box.bounds(), BOX_STYLE).setAlwaysOnTop();
            }
        });
    }

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null || !ToolkitConfig.INSTANCE.chestXray) {
            clear();
            return;
        }

        if (client.level != currentLevel) {
            currentLevel = client.level;
            boxes = List.of();
            scanCountdown = 0;
        }
        if (scanCountdown-- > 0) return;
        scanCountdown = RESCAN_INTERVAL_TICKS - 1;

        BlockPos playerPos = client.player.blockPosition();
        int centerChunkX = Math.floorDiv(playerPos.getX(), 16);
        int centerChunkZ = Math.floorDiv(playerPos.getZ(), 16);
        int distanceSquaredLimit = RANGE_BLOCKS * RANGE_BLOCKS;
        List<Box> found = new ArrayList<>();

        outer:
        for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                var chunk = client.level.getChunkSource().getChunk(
                        centerChunkX + dx, centerChunkZ + dz, ChunkStatus.FULL, false);
                if (!(chunk instanceof LevelChunk loaded)) continue;

                // Only block entities which already exist in loaded client chunks.
                for (BlockPos pos : loaded.getBlockEntities().keySet()) {
                    if (pos.distSqr(playerPos) > distanceSquaredLimit) continue;
                    if (!XrayController.isChest(loaded.getBlockState(pos).getBlock())) continue;
                    found.add(new Box(pos.getX(), pos.getY(), pos.getZ()));
                    if (found.size() >= MAX_BOXES) break outer;
                }
            }
        }

        boxes = List.copyOf(found);
    }

    public static void clear() {
        boxes = List.of();
        currentLevel = null;
        scanCountdown = 0;
    }

    // Kept as a harmless compatibility method for older code until cleanup.
    public static void close() {
        clear();
    }
}
