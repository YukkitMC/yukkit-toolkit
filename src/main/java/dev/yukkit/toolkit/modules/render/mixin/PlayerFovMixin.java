package dev.yukkit.toolkit.modules.render.mixin;

import dev.yukkit.toolkit.modules.render.NoFov;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class PlayerFovMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true, require = 0)
    private void yukkit$noFov(CallbackInfoReturnable<Float> cir) {
        if (NoFov.isActive()) cir.setReturnValue(1.0f);
    }
}
