package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.screen.ToolkitScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds a native Minecraft button while preserving the regular ESC pause menu. */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void yukkit$addToolsButton(CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        this.addRenderableWidget(Button.builder(Component.literal("Yukkit Tools"), button ->
                this.minecraft.gui.setScreen(new ToolkitScreen((Screen) (Object) this)))
                .bounds(this.width - 128, 10, 118, 20).build());
    }
}
