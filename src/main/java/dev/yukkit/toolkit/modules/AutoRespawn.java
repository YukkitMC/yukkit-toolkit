package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/**
 * Optional simple automatic respawn via the normal vanilla client action.
 * Sends at most one action per displayed death screen.
 */
public final class AutoRespawn {
    private static boolean handled;
    private AutoRespawn() {}
    public static void tick(Minecraft client) {
        if(!ToolkitConfig.INSTANCE.autoRespawn || client.player==null
                || !(client.gui.screen() instanceof DeathScreen)) {
            handled=false;
            return;
        }
        if(handled) return;
        handled=true;
        client.player.respawn();
        client.gui.setScreen(null);
    }
}
