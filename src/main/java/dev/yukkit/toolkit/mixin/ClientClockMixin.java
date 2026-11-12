package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.ClientClockManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft 26.3: the client clock supplies visual world time.
 * Never patch Level#getDayTime, removed from the 26.3 code path.
 */
@Mixin(ClientClockManager.ClientClockInstance.class)
public abstract class ClientClockMixin {
    @Inject(method = "totalTicks", at = @At("HEAD"), cancellable = true)
    private void yukkit$localDayTime(CallbackInfoReturnable<Long> cir) {
        if (ToolkitConfig.INSTANCE.localTime == 0) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        cir.setReturnValue(ToolkitConfig.INSTANCE.timeValue());
    }
}
