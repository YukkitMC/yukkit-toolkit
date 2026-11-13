package dev.yukkit.toolkit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.render.ChestModelThroughWalls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 26.3: draws a second copy of the exact textured vanilla
 * ChestModel in the game's standard model submission system. The ESP copy
 * ignores terrain depth, with no gizmo outlines and no manual GPU commands.
 */
@Mixin(ChestRenderer.class)
public abstract class TexturedChestMixin {
    @Shadow @Final private MultiblockChestResources<ChestModel> models;
    @Shadow @Final private SpriteGetter sprites;

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void yukkit$submitThroughWalls(ChestRenderState state, PoseStack poseStack,
                                          SubmitNodeCollector collector, CameraRenderState camera,
                                          CallbackInfo ci) {
        if (!ToolkitConfig.INSTANCE.chestXray || state.blockPos == null) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (state.blockPos.distSqr(client.player.blockPosition()) > 64.0 * 64.0) return;

        SpriteId sprite = Sheets.chooseSprite(state.material, state.type);
        RenderType type = ChestModelThroughWalls.type(sprite.atlasLocation());
        if (type == null) return;

        ChestModel model = this.models.select(state.type);
        float open = 1.0f - state.open;
        open = 1.0f - open * open * open;

        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(state.facing));
        collector.submitModel(model, open, poseStack, type,
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                -1, this.sprites.get(sprite), 0);
        poseStack.popPose();
    }
}
