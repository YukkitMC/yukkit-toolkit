package dev.yukkit.toolkit.modmixin;

import dev.yukkit.toolkit.modules.exploit.MultiActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MultiActionsMixin {
    /** Vanilla refuses to continue breaking while an item is in use. */
    @Redirect(method = "continueAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"),
            require = 0)
    private boolean yukkit$breakWhileUsing(LocalPlayer player) {
        return player.isUsingItem() && !MultiActions.mayBreakWhileUsing();
    }

    /** Vanilla refuses to use an item while a block is being broken. */
    @Redirect(method = "startUseItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"),
            require = 0)
    private boolean yukkit$useWhileBreaking(MultiPlayerGameMode gameMode) {
        return gameMode.isDestroying() && !MultiActions.mayUseWhileBreaking();
    }
}
