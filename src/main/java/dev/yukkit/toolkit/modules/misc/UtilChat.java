package dev.yukkit.toolkit.modules.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** Chat / sound helpers. Must be called on the client thread. */
final class UtilChat {
    private static final String PREFIX = "§7[§bYukkit§7] §f";

    private UtilChat() {}

    static void info(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.sendSystemMessage(Component.literal(PREFIX + message));
    }

    static void alert(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.sendSystemMessage(Component.literal(PREFIX + "§c" + message));
    }

    static void ping() {
        Minecraft mc = Minecraft.getInstance();
        try {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_HURT, 1.0F));
        } catch (Throwable ignored) {
            // sound is purely cosmetic
        }
    }
}
