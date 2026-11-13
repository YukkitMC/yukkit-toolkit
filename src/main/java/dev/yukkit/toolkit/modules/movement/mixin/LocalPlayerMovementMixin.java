package dev.yukkit.toolkit.modules.movement.mixin;

import dev.yukkit.toolkit.modules.movement.NoPush;
import dev.yukkit.toolkit.modules.movement.NoSlow;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMovementMixin {
    /** NoSlow: using an item no longer counts as "slowed" (keeps sprinting). */
    @Inject(method = "isSlowDueToUsingItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$noSlowSprint(CallbackInfoReturnable<Boolean> cir) {
        try {
            if (NoSlow.active()) cir.setReturnValue(false);
        } catch (Throwable ignored) {
            // never break the player tick
        }
    }

    /** NoSlow: undo the movement-vector scaling applied while an item is in use. */
    @Inject(method = "modifyInput", at = @At("RETURN"), cancellable = true, require = 0)
    private void yukkit$noSlowInput(CallbackInfoReturnable<Vec2> cir) {
        try {
            if (!NoSlow.active()) return;
            LocalPlayer self = (LocalPlayer) (Object) this;
            if (!self.isUsingItem() || self.isPassenger() || self.isShiftKeyDown()) return;
            Vec2 original = self.input.getMoveVector();
            Vec2 result = cir.getReturnValue();
            if (original == null || result == null) return;
            if (result.length() < original.length() * 0.5f) cir.setReturnValue(original);
        } catch (Throwable ignored) {
            // never break the player tick
        }
    }

    /** NoPush: skip the push-out-of-blocks nudge. */
    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$noPushBlocks(double x, double z, CallbackInfo ci) {
        try {
            if (NoPush.blocksBlockPush()) ci.cancel();
        } catch (Throwable ignored) {
            // never break the player tick
        }
    }
}
