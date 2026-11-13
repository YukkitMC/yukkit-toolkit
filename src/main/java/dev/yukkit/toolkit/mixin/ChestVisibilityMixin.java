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
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
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
 * Force the actual vanilla chest render state into the scene, independent of
 * vanilla visible-section occlusion. The old code also frustum-cropped these
 * candidates; that extra gate could incorrectly discard a chest behind walls.
 *
 * To prevent stutters, only the first 192 nearest-first cached candidates
 * are considered for extra extraction per frame. No chunk loading occurs here.
 */
@Mixin(LevelExtractor.class)
public abstract class ChestVisibilityMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LevelRenderer levelRenderer;
    @Shadow @Nullable private ClientLevel level;

    @Inject(method="extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at=@At("TAIL"))
    private void yukkit$extractHiddenChests(Camera camera, float partialTick,
                                           LevelRenderState state, CallbackInfo ci) {
        if(!ToolkitConfig.INSTANCE.chestXray || level==null || minecraft.player==null) {
            ChestCandidates.reportForcedExtracted(0);
            return;
        }
        Set<BlockPos> already=new HashSet<>();
        for(BlockEntityRenderState existing: state.blockEntityRenderStates) {
            if(existing instanceof ChestRenderState && existing.blockPos!=null) {
                already.add(existing.blockPos);
            }
        }

        int inspected=0, forced=0;
        Vec3 cameraPosition=camera.position();
        for(BlockPos pos:ChestCandidates.positions()) {
            if(inspected++>=ChestCandidates.MAX_RENDER_STATES_PER_FRAME) break;
            if(already.contains(pos)) continue;

            BlockEntity chest=level.getBlockEntity(pos);
            if(!(chest instanceof ChestBlockEntity || chest instanceof EnderChestBlockEntity)) continue;
            if(!ChestCandidates.selectedChest(level.getBlockState(pos).getBlock())) continue;

            BlockEntityRenderState extracted=yukkit$extract(chest,partialTick,cameraPosition);
            if(extracted!=null) {
                state.blockEntityRenderStates.add(extracted);
                forced++;
            }
        }
        ChestCandidates.reportForcedExtracted(forced);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private BlockEntityRenderState yukkit$extract(BlockEntity entity,float partialTick,Vec3 cameraPosition) {
        BlockEntityRenderer renderer=levelRenderer.blockEntityRenderDispatcher().getRenderer(entity);
        if(renderer==null) return null;
        BlockEntityRenderState state=(BlockEntityRenderState)renderer.createRenderState();
        renderer.extractRenderState(entity,state,partialTick,cameraPosition,null);
        return state;
    }
}
