package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.movement.NoPush;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientLevel.class)
public abstract class ClientLevelPushMixin {
    @Inject(method = "getPushableEntities", at = @At("HEAD"), cancellable = true, require = 0)
    private void yukkit$noEntityPush(Entity pusher, AABB box, CallbackInfoReturnable<List<Entity>> cir) {
        try {
            if (pusher != null && pusher == Minecraft.getInstance().player && NoPush.blocksEntityPush()) {
                cir.setReturnValue(List.of());
            }
        } catch (Throwable ignored) {
            // never break the player tick
        }
    }
}
