package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.modules.XrayController;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hide non-ore block models only while X-Ray is enabled.
 * Use the vanilla block render state API, not direct OpenGL calls.
 */
@Mixin(BlockBehaviour.class)
public abstract class XrayBlockMixin {
    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    private void yukkit$hideNonOres(BlockState state, CallbackInfoReturnable<RenderShape> cir) {
        if (XrayController.enabled() && !XrayController.keep(state)) {
            cir.setReturnValue(RenderShape.INVISIBLE);
        }
    }
}
