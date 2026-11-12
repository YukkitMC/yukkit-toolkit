package dev.yukkit.toolkit;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.camera.PerspectiveController;
import dev.yukkit.toolkit.screen.ToolkitScreen;
import dev.yukkit.toolkit.world.WorldCapture;
import dev.yukkit.toolkit.hud.ToolkitHud;
import dev.yukkit.toolkit.map.ToolkitMap;
import dev.yukkit.toolkit.modules.AutoTotem;
import dev.yukkit.toolkit.modules.AutoClicker;
import dev.yukkit.toolkit.modules.AutoMiner;
import dev.yukkit.toolkit.modules.MaceSwap;
import dev.yukkit.toolkit.modules.AutoEat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YukkitToolkitClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("YukkitToolkit");
    public static final WorldCapture WORLD_CAPTURE = new WorldCapture();
    private static KeyMapping menuKey;
    private static KeyMapping perspectiveKey;

    @Override
    public void onInitializeClient() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("yukkit_toolkit", "main"));
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yukkit_toolkit.menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, category
        ));
        perspectiveKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yukkit_toolkit.perspective", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, category
        ));

        ToolkitHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) {
                if (client.level != null && client.player != null) {
                    if (client.gui.screen() instanceof ToolkitScreen) client.gui.setScreen(null);
                    else client.gui.setScreen(new ToolkitScreen());
                }
            }
            while (perspectiveKey.consumeClick()) {
                if (client.gui.screen() == null && client.player != null) PerspectiveController.toggle(client);
            }
            if (client.level == null || client.player == null) {
                FreecamController.disable();
                PerspectiveController.disable();
                WORLD_CAPTURE.stop();
                return;
            }
            FreecamController.tick(client);
            WORLD_CAPTURE.tick(client);
            ToolkitHud.tick(client);
            ToolkitMap.tick(client);
            AutoTotem.tick(client);
            AutoEat.tick(client);
            MaceSwap.tick(client);
            AutoClicker.tick(client);
            AutoMiner.tick(client);
        });
        LOGGER.info("Yukkit Toolkit initialized (Minecraft 26.3 / Fabric)");
    }

    public static void notifyPlayer(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) client.player.sendSystemMessage(Component.literal("[Yukkit] " + message));
    }
}
