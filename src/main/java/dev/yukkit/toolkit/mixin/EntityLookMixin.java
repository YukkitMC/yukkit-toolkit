package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.camera.PerspectiveController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityLookMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void yukkit$look(double deltaX, double deltaY, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player) return;
        if (FreecamController.isEnabled()) {
            FreecamController.mouseLook(deltaX, deltaY);
            ci.cancel();
        } else if (PerspectiveController.isEnabled()) {
            PerspectiveController.mouseLook(deltaX, deltaY);
            ci.cancel();
        }
    }
}
