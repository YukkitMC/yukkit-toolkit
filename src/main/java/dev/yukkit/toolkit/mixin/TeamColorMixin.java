package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.hud.TeamGlow;
import dev.yukkit.toolkit.team.TeamEspColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides only the locally-rendered team outline color of a matched player.
 * The game normally obtains glow outline colors through Entity#getTeamColor.
 * No scoreboard teams or server-synchronized entity flags are modified.
 */
@Mixin(Entity.class)
public abstract class TeamColorMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void yukkit$teamColor(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Player player && TeamGlow.shouldGlow(player)) {
            cir.setReturnValue(TeamEspColor.rgb());
        }
    }
}
