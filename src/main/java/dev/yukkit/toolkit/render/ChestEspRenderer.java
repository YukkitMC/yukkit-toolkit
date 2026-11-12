package dev.yukkit.toolkit.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.modules.XrayController;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Chest ESP: outlines nearby known storage blocks with translucent 3D volumes.
 * Ordinary block rendering stays untouched; only this overlay ignores world depth.
 *
 * Chunk iteration uses loaded chunks without forcing loads or scanning the server.
 * World access happens during client ticks, never in the GPU drawing callback.
 */
public final class ChestEspRenderer {
    private static final int CHUNK_RADIUS = 4;
    private static final int RANGE = 64;
    private static final int MAX_BOXES = 96;
    private static final int RESCAN_TICKS = 20;
    private static final float R = 0.07f, G = 0.75f, B = 1.0f, ALPHA = 0.24f;

    private static final RenderPipeline THROUGH_WALLS = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("yukkit_toolkit", "pipeline/chest_esp"))
            .withDepthStencilState(Optional.empty())
            .build());

    private static final Vector4f WHITE = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE = new Matrix4f();
    private static final StagedVertexBuffer BUFFER =
            new StagedVertexBuffer(() -> "Yukkit Chest ESP", RenderType.SMALL_BUFFER_SIZE);

    private static List<Box> known = List.of();
    private static List<Box> extracted = List.of();
    private static Object lastLevel;
    private static int ticks;

    private record Box(int x, int y, int z) {}

    private ChestEspRenderer() {}

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(ChestEspRenderer::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ChestEspRenderer::render);
    }

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null || !ToolkitConfig.INSTANCE.chestXray) {
            known = List.of();
            lastLevel = null;
            ticks = 0;
            return;
        }
        if (client.level != lastLevel) {
            lastLevel = client.level;
            ticks = RESCAN_TICKS;
            known = List.of();
        }
        if (++ticks < RESCAN_TICKS) return;
        ticks = 0;

        BlockPos playerPos = client.player.blockPosition();
        int cx = Math.floorDiv(playerPos.getX(), 16);
        int cz = Math.floorDiv(playerPos.getZ(), 16);
        int radiusSq = RANGE * RANGE;
        List<Box> found = new ArrayList<>();
        outer:
        for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                var candidate = client.level.getChunkSource().getChunk(
                        cx + dx, cz + dz, ChunkStatus.FULL, false);
                if (!(candidate instanceof LevelChunk chunk)) continue;

                for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                    if (pos.distSqr(playerPos) > radiusSq) continue;
                    if (!XrayController.isChest(client.level.getBlockState(pos).getBlock())) continue;
                    found.add(new Box(pos.getX(), pos.getY(), pos.getZ()));
                    if (found.size() >= MAX_BOXES) break outer;
                }
            }
        }
        known = List.copyOf(found);
    }

    private static void extract(LevelExtractionContext context) {
        extracted = ToolkitConfig.INSTANCE.chestXray ? known : List.of();
    }

    private static void render(LevelRenderContext context) {
        List<Box> positions = extracted;
        if (positions.isEmpty()) return;
        VertexFormat format = THROUGH_WALLS.getVertexFormatBinding(0);
        if (format == null) return;
        PrimitiveTopology primitive = THROUGH_WALLS.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = BUFFER.appendDraw(format, primitive,
            primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        PoseStack pose = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer vertices = BUFFER.getVertexBuilder(draw);
        Matrix4fc matrix = pose.last().pose();

        for (Box box : positions) {
            cube(matrix, vertices, box.x + 0.045f, box.y + 0.045f, box.z + 0.045f,
                box.x + 0.955f, box.y + 0.955f, box.z + 0.955f);
        }
        pose.popPose();
        BUFFER.upload();
        StagedVertexBuffer.ExecuteInfo info = BUFFER.getExecuteInfo(draw);
        if (info != null) draw(Minecraft.getInstance(), info);
        BUFFER.endFrame();
    }

    private static void cube(Matrix4fc m, VertexConsumer v,
                             float x0, float y0, float z0, float x1, float y1, float z1) {
        // Quad faces rendered with disabled depth testing and low alpha.
        quad(m,v,x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1);
        quad(m,v,x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0);
        quad(m,v,x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0);
        quad(m,v,x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1);
        quad(m,v,x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0);
        quad(m,v,x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1);
    }

    private static void quad(Matrix4fc m, VertexConsumer v,
                             float x0,float y0,float z0, float x1,float y1,float z1,
                             float x2,float y2,float z2, float x3,float y3,float z3) {
        v.addVertex(m,x0,y0,z0).setColor(R,G,B,ALPHA);
        v.addVertex(m,x1,y1,z1).setColor(R,G,B,ALPHA);
        v.addVertex(m,x2,y2,z2).setColor(R,G,B,ALPHA);
        v.addVertex(m,x3,y3,z3).setColor(R,G,B,ALPHA);
    }

    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info) {
        GpuBufferSlice uniforms = RenderSystem.getDynamicUniforms().writeTransform(
            RenderSystem.getModelViewMatrixCopy(), WHITE, OFFSET, TEXTURE);
        RenderTarget target = client.gameRenderer.mainRenderTarget();
        GpuTextureView color = target.getColorTextureView();
        if (color == null) return;
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Yukkit Chest ESP", color, Optional.empty(),
                target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(THROUGH_WALLS);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", uniforms);
            pass.setVertexBuffer(0, info.vertexBuffer().slice());
            pass.setIndexBuffer(info.indexBuffer(), info.indexType());
            pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    public static void close() {
        BUFFER.close();
    }
}
