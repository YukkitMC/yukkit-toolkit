package dev.yukkit.toolkit.modmixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the private right click cooldown of the client. */
@Mixin(Minecraft.class)
public interface ClientDelayAccessor {
    @Accessor("rightClickDelay")
    int yukkit$getRightClickDelay();

    @Accessor("rightClickDelay")
    void yukkit$setRightClickDelay(int value);
}
