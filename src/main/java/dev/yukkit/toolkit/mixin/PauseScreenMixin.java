package dev.yukkit.toolkit.mixin;

import dev.yukkit.toolkit.YukkitToolkitClient;
import dev.yukkit.toolkit.screen.ToolkitScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

        yukkit$addWorldDownloadButton();
    }

    /** Places a World Downloader toggle directly under the vanilla disconnect / save-and-quit button. */
    @Unique
    private void yukkit$addWorldDownloadButton() {
        Button anchor = null;
        for (var child : this.children()) {
            if (!(child instanceof Button b)) continue;
            if (b.getMessage().getContents() instanceof TranslatableContents tc
                    && (tc.getKey().equals("menu.disconnect") || tc.getKey().equals("menu.returnToMenu"))) {
                anchor = b;
                break;
            }
            // Fallback: the lowest button on screen.
            if (anchor == null || b.getY() > anchor.getY()) anchor = b;
        }
        if (anchor == null) return;

        var capture = YukkitToolkitClient.WORLD_CAPTURE;
        Button button = Button.builder(yukkit$worldLabel(), btn -> {
            if (capture.isCapturing()) {
                capture.stop();
                YukkitToolkitClient.notifyPlayer("World capture stopped.");
            } else {
                capture.start(this.minecraft);
            }
            btn.setMessage(yukkit$worldLabel());
        }).bounds(anchor.getX(), anchor.getY() + anchor.getHeight() + 4, anchor.getWidth(), 20).build();
        this.addRenderableWidget(button);
    }

    @Unique
    private static Component yukkit$worldLabel() {
        var capture = YukkitToolkitClient.WORLD_CAPTURE;
        return Component.literal(capture.isCapturing()
                ? "Stop World Download (" + capture.getSavedChunks() + " chunks)"
                : "Download World");
    }
}
