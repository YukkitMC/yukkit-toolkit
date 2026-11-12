package dev.yukkit.toolkit;

import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.screen.ToolkitScreen;
import dev.yukkit.toolkit.world.WorldCapture;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YukkitToolkitClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("YukkitToolkit");
    public static final WorldCapture WORLD_CAPTURE = new WorldCapture();
    private static KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yukkit_toolkit.menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("yukkit_toolkit", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) {
                if (client.level != null && client.player != null) {
                    if (client.gui.screen() instanceof ToolkitScreen) client.gui.setScreen(null);
                    else client.gui.setScreen(new ToolkitScreen());
                }
            }
            if (client.level == null || client.player == null) {
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
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) client.player.sendSystemMessage(Component.literal("[Yukkit] " + message));
    }
}
