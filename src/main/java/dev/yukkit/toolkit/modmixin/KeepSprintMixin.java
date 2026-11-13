package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.combat.KeepSprint;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class KeepSprintMixin {
    @Unique private Vec3 yukkit$motionBefore;
    @Unique private boolean yukkit$sprintBefore;

    @Inject(method = "attack", at = @At("HEAD"), require = 0)
    private void yukkit$rememberMotion(Entity target, CallbackInfo ci) {
        yukkit$motionBefore = null;
        Player self = (Player) (Object) this;
        if (self != Minecraft.getInstance().player) return;
        if (KeepSprint.motionKept() <= 0.0 && !KeepSprint.keepsSprintState()) return;
        yukkit$motionBefore = self.getDeltaMovement();
        yukkit$sprintBefore = self.isSprinting();
    }

    @Inject(method = "attack", at = @At("RETURN"), require = 0)
    private void yukkit$restoreMotion(Entity target, CallbackInfo ci) {
        Vec3 before = yukkit$motionBefore;
        yukkit$motionBefore = null;
        if (before == null) return;
        Player self = (Player) (Object) this;
        double keep = KeepSprint.motionKept();
        if (keep > 0.0) {
            Vec3 now = self.getDeltaMovement();
            self.setDeltaMovement(new Vec3(now.x + (before.x - now.x) * keep, now.y, now.z + (before.z - now.z) * keep));
        }
        if (yukkit$sprintBefore && KeepSprint.keepsSprintState()) self.setSprinting(true);
    }
}
