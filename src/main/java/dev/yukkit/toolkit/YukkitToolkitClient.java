package dev.yukkit.toolkit;

import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.screen.ToolkitScreen;
import dev.yukkit.toolkit.world.WorldCapture;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YukkitToolkitClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("YukkitToolkit");
    public static final WorldCapture WORLD_CAPTURE = new WorldCapture();
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.yukkit_toolkit.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.yukkit_toolkit"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.world != null && client.player != null) {
                    if (client.currentScreen instanceof ToolkitScreen) client.setScreen(null);
                    else client.setScreen(new ToolkitScreen());
                }
            }
            if (client.world == null || client.player == null) {
                FreecamController.disable();
                WORLD_CAPTURE.stop();
                return;
            }
            FreecamController.tick(client);
            WORLD_CAPTURE.tick(client);
        });
        LOGGER.info("Yukkit Toolkit initialized (Minecraft 1.21.1 / Fabric)");
    }

    public static void notifyPlayer(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal("[Yukkit] " + message), false);
    }
}
