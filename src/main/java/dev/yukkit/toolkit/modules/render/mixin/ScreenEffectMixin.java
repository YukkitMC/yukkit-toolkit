package dev.yukkit.toolkit.modules.render.mixin;

import dev.yukkit.toolkit.modules.render.AntiBlind;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectMixin {
    /** Static target: draws the in-block (suffocation) texture overlay. */
    @Inject(method = "submitBlockSprite", at = @At("HEAD"), cancellable = true, require = 0)
    private static void yukkit$noWallOverlay(CallbackInfo ci) {
        if (AntiBlind.hideWallOverlay()) ci.cancel();
    }
}
