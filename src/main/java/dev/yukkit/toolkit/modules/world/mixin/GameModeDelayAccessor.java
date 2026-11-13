package dev.yukkit.toolkit.modules.world.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the block breaking cooldown of the client game mode. */
@Mixin(MultiPlayerGameMode.class)
public interface GameModeDelayAccessor {
    @Accessor("destroyDelay")
    int yukkit$getDestroyDelay();

    @Accessor("destroyDelay")
    void yukkit$setDestroyDelay(int value);
}
