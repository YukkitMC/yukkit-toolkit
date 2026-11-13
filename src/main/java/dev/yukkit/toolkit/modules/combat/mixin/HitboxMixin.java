package dev.yukkit.toolkit.modules.combat.mixin;

import dev.yukkit.toolkit.modules.combat.Hitbox;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class HitboxMixin {
    @Inject(method = "getPickRadius", at = @At("RETURN"), cancellable = true, require = 0)
    private void yukkit$expandPick(CallbackInfoReturnable<Float> cir) {
        float extra = Hitbox.margin((Entity) (Object) this);
        if (extra > 0f) cir.setReturnValue(cir.getReturnValueF() + extra);
    }
}
