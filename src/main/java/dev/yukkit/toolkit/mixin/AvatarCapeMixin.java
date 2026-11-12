package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.cosmetics.CapeCosmetics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarCapeMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void yukkit$localCape(Avatar avatar, AvatarRenderState renderState, float delta, CallbackInfo ci) {
        if (ToolkitConfig.INSTANCE.capeStyle == 0) return;
        var client = Minecraft.getInstance();
        if (client.player == null || avatar != client.player) return;
        renderState.skin = CapeCosmetics.apply(renderState.skin);
        renderState.showCape = true;
    }
}
