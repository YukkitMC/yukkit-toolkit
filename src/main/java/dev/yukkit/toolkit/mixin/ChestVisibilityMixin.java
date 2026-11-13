package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.render.ChestCandidates;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
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
 * Extract real chest render states even if their section wasn't included in
 * the ordinary visible-section traversal. They use the SAME vanilla
 * ChestRenderer/ChestModel path, with the depth-independent duplicate
 * supplied by TexturedChestMixin.
 *
 * No GPU calls and no chunk loading. The candidate list is produced on
 * client ticks, at most once per 20 ticks, from loaded block entities.
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
        if (!ToolkitConfig.INSTANCE.chestXray || this.level == null || this.minecraft.player == null) return;

        Set<BlockPos> alreadyExtracted = new HashSet<>();
        for (BlockEntityRenderState state : out.blockEntityRenderStates) {
            if (state instanceof ChestRenderState) alreadyExtracted.add(state.blockPos);
        }
        for (BlockPos pos : ChestCandidates.positions()) {
            if (alreadyExtracted.contains(pos)) continue;
            BlockEntity chest = this.level.getBlockEntity(pos);
            if (!(chest instanceof ChestBlockEntity || chest instanceof EnderChestBlockEntity)) continue;
            var state = this.levelRenderer.blockEntityRenderDispatcher()
                    .tryExtractRenderState(chest, partialTick, null, false);
            if (state != null) out.blockEntityRenderStates.add(state);
        }
    }
}
