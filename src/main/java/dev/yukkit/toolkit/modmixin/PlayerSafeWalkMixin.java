package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.movement.SafeWalk;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerSafeWalkMixin {
    @Inject(method = "isStayingOnGroundSurface", at = @At("RETURN"), cancellable = true, require = 0)
    private void yukkit$safeWalk(CallbackInfoReturnable<Boolean> cir) {
        try {
            if ((Object) this == Minecraft.getInstance().player && SafeWalk.active()) cir.setReturnValue(true);
        } catch (Throwable ignored) {
            // never break the player tick
        }
    }
}
