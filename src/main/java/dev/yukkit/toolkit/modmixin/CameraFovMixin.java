package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.render.Zoom;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraFovMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void yukkit$zoom(CallbackInfoReturnable<Float> cir) {
        if (Zoom.isActive() && cir.getReturnValue() != null) {
            cir.setReturnValue(Zoom.applyFov(cir.getReturnValue()));
        }
    }
}
