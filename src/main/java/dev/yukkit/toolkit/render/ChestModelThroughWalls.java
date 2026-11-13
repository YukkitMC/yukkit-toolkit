package dev.yukkit.toolkit.render;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.yukkit.toolkit.YukkitToolkitClient;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Re-submit Minecraft's own ChestModel with the original chest atlas texture
 * and a depth-independent pipeline. All actual drawing is performed by the
 * normal SubmitNodeCollector, never by a nested GPU render pass.
 *
 * Uses the same vertex/shader layout as Minecraft's standard entity model.
 * Reflection is limited to obtaining Mojang's private entity pipeline snippet
 * (Fabric's 26.3 mappings); failures disable the overlay instead of crashing.
 */
public final class ChestModelThroughWalls {
    private static final Map<Identifier, RenderType> TYPES = new HashMap<>();
    private static RenderPipeline pipeline;
    private static boolean unavailable;

    private ChestModelThroughWalls() {}

    public static RenderType type(Identifier atlas) {
        if (unavailable) return null;
        RenderType cached = TYPES.get(atlas);
        if (cached != null) return cached;
        try {
            if (pipeline == null) {
                Field field = RenderPipelines.class.getDeclaredField("ENTITY_SNIPPET");
                field.setAccessible(true);
                RenderPipeline.Snippet snippet = (RenderPipeline.Snippet) field.get(null);
                pipeline = RenderPipelines.register(RenderPipeline.builder(snippet)
                        .withLocation(Identifier.fromNamespaceAndPath("yukkit_toolkit", "pipeline/textured_chest_esp"))
                        .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                        .withShaderDefine("PER_FACE_LIGHTING")
                        .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                        .withCull(false)
                        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                        .build());
            }

            RenderSetup setup = RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", atlas)
                    .useLightmap()
                    .useOverlay()
                    .createRenderSetup();
            RenderType result = RenderType.create("yukkit_textured_chest_esp", setup);
            TYPES.put(atlas, result);
            return result;
        } catch (Exception | LinkageError ex) {
            unavailable = true;
            YukkitToolkitClient.LOGGER.warn("Chest texture ESP disabled: could not initialize 26.3 model pipeline", ex);
            return null;
        }
    }

    public static void clear() { TYPES.clear(); }
}
