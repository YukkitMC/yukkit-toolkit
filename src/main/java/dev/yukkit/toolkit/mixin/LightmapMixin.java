package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lightmap.class)
public abstract class LightmapMixin {
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void yukkit$fullbright(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (ToolkitConfig.INSTANCE.fullbright) {
            cir.setReturnValue(1.0f);
        }
    }
}
