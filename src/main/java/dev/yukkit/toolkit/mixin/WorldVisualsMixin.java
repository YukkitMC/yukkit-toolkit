package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Weather only. Time must not target Level#getDayTime on Minecraft 26.3:
 * it no longer exists, and targeting it crashes the game during bootstrap.
 */
@Mixin(Level.class)
public abstract class WorldVisualsMixin {
    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void yukkit$rain(float delta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientLevel && ToolkitConfig.INSTANCE.localWeather != 0) {
            cir.setReturnValue(ToolkitConfig.INSTANCE.localWeather == 2 ? 1.0f : 0.0f);
        }
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void yukkit$thunder(float delta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientLevel && ToolkitConfig.INSTANCE.localWeather != 0) {
            cir.setReturnValue(0.0f);
        }
    }
}
