package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.movement.MovementInputHooks;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MovementInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void yukkit$movementInput(CallbackInfo ci) {
        try {
            Input current = this.keyPresses;
            Input next = MovementInputHooks.apply(current);
            if (next != null && !next.equals(current)) {
                this.keyPresses = next;
                this.moveVector = MovementInputHooks.vectorOf(next);
            }
        } catch (Throwable ignored) {
            // never break the input tick
        }
    }
}
