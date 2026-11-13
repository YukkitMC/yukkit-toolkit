package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.exploit.NoPitchLimit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class NoPitchLimitMixin {
    @Shadow
    private float xRot;

    @Unique
    private boolean yukkit$unlimited() {
        return NoPitchLimit.active() && (Object) this == Minecraft.getInstance().player;
    }

    /** Mouse look clamps the pitch through Mth.clamp. */
    @Redirect(method = {"turn", "absSnapRotationTo"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"),
            require = 0)
    private float yukkit$skipPitchClamp(float value, float min, float max) {
        return yukkit$unlimited() ? value : Mth.clamp(value, min, max);
    }

    /** setXRot clamps again, so write the raw value for the local player. */
    @Inject(method = "setXRot", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$setRawPitch(float pitch, CallbackInfo ci) {
        if (Float.isFinite(pitch) && yukkit$unlimited()) {
            this.xRot = pitch;
            ci.cancel();
        }
    }
}
