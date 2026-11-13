package dev.yukkit.toolkit.modules.funmods;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.lang.reflect.Field;
import java.util.Set;

/** Reads the player's enabled skin layers (the options field is private, so it is read reflectively). */
final class UtilSkin {
    private static Field field;
    private static boolean failed;

    private UtilSkin() {}

    @SuppressWarnings("unchecked")
    static Set<PlayerModelPart> enabledParts() {
        if (!failed) {
            try {
                if (field == null) {
                    field = Minecraft.getInstance().options.getClass().getDeclaredField("modelParts");
                    field.setAccessible(true);
                }
                Object value = field.get(Minecraft.getInstance().options);
                if (value instanceof Set<?> set) return (Set<PlayerModelPart>) set;
            } catch (Throwable t) {
                failed = true;
            }
        }
        return Set.of();
    }
}
