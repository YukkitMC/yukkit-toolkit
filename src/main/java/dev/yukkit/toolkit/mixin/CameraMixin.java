package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.camera.PerspectiveController;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void yukkit$camera(float partialTicks, CallbackInfo ci) {
        if (FreecamController.isEnabled()) {
            var pos = FreecamController.cameraPosition(partialTicks);
            this.setRotation(FreecamController.yaw(), FreecamController.pitch());
            this.setPosition(pos.x, pos.y, pos.z);
        } else if (PerspectiveController.isEnabled()) {
            this.setRotation(PerspectiveController.yaw(), PerspectiveController.pitch());
        }
    }
}
