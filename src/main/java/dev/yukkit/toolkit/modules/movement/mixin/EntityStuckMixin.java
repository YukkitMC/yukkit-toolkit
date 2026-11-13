package dev.yukkit.toolkit.modules.movement.mixin;

import dev.yukkit.toolkit.modules.movement.NoWeb;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityStuckMixin {
    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$noWeb(CallbackInfo ci) {
        try {
            if ((Object) this == Minecraft.getInstance().player && NoWeb.active()) ci.cancel();
        } catch (Throwable ignored) {
            // never break the player tick
        }
    }
}
