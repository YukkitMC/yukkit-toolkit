package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.camera.FreecamController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityLookMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void yukkit$look(double deltaX, double deltaY, CallbackInfo ci) {
        if (FreecamController.isEnabled() && (Object) this == MinecraftClient.getInstance().player) {
            FreecamController.mouseLook(deltaX, deltaY);
            ci.cancel();
        }
    }
}
