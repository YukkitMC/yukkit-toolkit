package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.core.ModuleManager;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lets modules inspect and cancel packets. Loaded from the optional modules mixin config. */
@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void yukkit$send(Packet<?> packet, CallbackInfo ci) {
        if (ModuleManager.fireSend(packet)) ci.cancel();
    }

    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true, require = 0)
    private static void yukkit$receive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (ModuleManager.fireReceive(packet)) ci.cancel();
    }
}
