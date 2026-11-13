package dev.yukkit.toolkit.modules.world.mixin;

import dev.yukkit.toolkit.modules.world.UtilTimer;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Scales the amount of game time that passes per frame, which changes the client tick rate. */
@Mixin(DeltaTracker.Timer.class)
public abstract class TimerMixin {
    @Shadow
    private float deltaTicks;

    @Inject(method = "advanceGameTime(J)I",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;deltaTicks:F",
                    shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD),
            require = 0)
    private void yukkit$scaleDeltaTicks(CallbackInfoReturnable<Integer> cir) {
        float speed = UtilTimer.speed();
        if (speed != 1.0f) deltaTicks *= speed;
    }
}
