package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.camera.FreecamController;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void yukkit$freezePlayer(CallbackInfo ci) {
        if (!FreecamController.isEnabled()) return;
        this.keyPresses = net.minecraft.world.entity.player.Input.EMPTY;
        this.moveVector = net.minecraft.world.phys.Vec2.ZERO;
    }
}
