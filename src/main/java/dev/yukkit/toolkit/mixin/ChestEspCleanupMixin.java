package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.render.ChestEspRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class ChestEspCleanupMixin {
    @Inject(method = "close", at = @At("RETURN"))
    private void yukkit$closeChestEsp(CallbackInfo ci) {
        ChestEspRenderer.close();
    }
}
