package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.hud.TeamGlow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LivingEntity overrides isCurrentlyGlowing in 26.3.
 * Return true only for locally configured, loaded nearby teammates.
 * Minecraft's glow outline is drawn through solid blocks.
 */
@Mixin(LivingEntity.class)
public abstract class TeamOutlineMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void yukkit$teamOutline(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && TeamGlow.shouldGlow(player)) {
            cir.setReturnValue(true);
        }
    }
}
