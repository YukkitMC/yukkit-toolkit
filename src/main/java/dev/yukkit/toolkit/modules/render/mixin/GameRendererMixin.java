package dev.yukkit.toolkit.modules.render.mixin;

import dev.yukkit.toolkit.modules.render.NoBob;
import dev.yukkit.toolkit.modules.render.NoHurtCam;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$noHurtCam(CallbackInfo ci) {
        if (NoHurtCam.isActive()) ci.cancel();
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$noBob(CallbackInfo ci) {
        if (NoBob.isActive()) ci.cancel();
    }
}
