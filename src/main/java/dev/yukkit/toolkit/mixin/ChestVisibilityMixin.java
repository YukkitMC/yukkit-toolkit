package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.render.ChestCandidates;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Include occluded chests from loaded chunks in the normal block entity model
 * submission path. Extraction is not gated by the vanilla 64-block render
 * distance: only these configured chest candidates bypass that visual limit.
 */
@Mixin(LevelExtractor.class)
public abstract class ChestVisibilityMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LevelRenderer levelRenderer;
    @Shadow @Nullable private ClientLevel level;

    @Inject(method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at = @At("TAIL"))
    private void yukkit$extractHiddenChests(Camera camera, float partialTick,
                                           LevelRenderState out, CallbackInfo ci) {
        if (!ToolkitConfig.INSTANCE.chestXray || level == null || minecraft.player == null) return;

        Set<BlockPos> already = new HashSet<>();
        for (BlockEntityRenderState state : out.blockEntityRenderStates) {
            if (state instanceof ChestRenderState && state.blockPos != null) already.add(state.blockPos);
        }

        Frustum frustum = camera.getCullFrustum();
        Vec3 cameraPos = camera.position();
        for (BlockPos pos : ChestCandidates.positions()) {
            if (already.contains(pos)) continue;
            if (!frustum.isVisible(new AABB(pos))) continue; // Ignore off-screen, not occluded, blocks.
            BlockEntity entity = level.getBlockEntity(pos);
            if (!(entity instanceof ChestBlockEntity || entity instanceof EnderChestBlockEntity)) continue;

            // The usual tryExtractRenderState enforces ChestRenderer's 64-block
            // default; build the same vanilla render state directly for ESP.
            BlockEntityRenderState state = yukkit$makeChestState(entity, partialTick, cameraPos);
            if (state != null) out.blockEntityRenderStates.add(state);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BlockEntityRenderState yukkit$makeChestState(BlockEntity entity, float partialTick, Vec3 cameraPos) {
        BlockEntityRenderer renderer = this.levelRenderer.blockEntityRenderDispatcher().getRenderer(entity);
        if (renderer == null) return null;
        BlockEntityRenderState state = (BlockEntityRenderState) renderer.createRenderState();
        renderer.extractRenderState(entity, state, partialTick, cameraPos, null);
        return state;
    }
}
