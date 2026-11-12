package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.modules.XrayController;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Non-rendered neighboring stone should not hide the faces of ore models. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class XrayOcclusionMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "getFaceOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void yukkit$noOcclusion(Direction face, CallbackInfoReturnable<VoxelShape> cir) {
        if (XrayController.enabled() && !(dev.yukkit.toolkit.ToolkitConfig.INSTANCE.xray && XrayController.isOre(getBlock()))
                && !(dev.yukkit.toolkit.ToolkitConfig.INSTANCE.chestXray && XrayController.isChest(getBlock()))) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}
