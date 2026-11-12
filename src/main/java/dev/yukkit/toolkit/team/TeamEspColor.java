package dev.yukkit.toolkit.team;

import dev.yukkit.toolkit.ToolkitConfig;
import java.util.Locale;

/** Local outline colors, stored as a 24-bit RGB value in the toolkit JSON config. */
public final class TeamEspColor {
    public static final String[] NAMES = {
            "Blue", "Cyan", "Green", "Red", "Yellow", "Purple", "White", "Orange"
    };
    public static final int[] VALUES = {
            0x006EFE, 0x00D9FF, 0x36D777, 0xFF4848,
            0xFFD43B, 0xAD65FF, 0xFFFFFF, 0xFF9140
    };

    private TeamEspColor() {}

    public static String name(int color) {
        for (int i = 0; i < VALUES.length; i++) {
            if (VALUES[i] == (color & 0xFFFFFF)) return NAMES[i];
        }
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    public static int rgb() {
        return ToolkitConfig.INSTANCE.teamGlowColor & 0xFFFFFF;
    }

    public static String hex() {
        return String.format(Locale.ROOT, "#%06X", rgb());
    }

    public static boolean setHex(String input) {
        if (input == null) return false;
        String trimmed = input.trim();
        if (!trimmed.matches("(?i)#?[0-9a-f]{6}")) return false;
        String value = trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;
        ToolkitConfig.INSTANCE.teamGlowColor = Integer.parseInt(value, 16);
        ToolkitConfig.INSTANCE.save();
        return true;
    }

    public static void setPreset(int index) {
        if (index < 0 || index >= VALUES.length) return;
        ToolkitConfig.INSTANCE.teamGlowColor = VALUES[index];
        ToolkitConfig.INSTANCE.save();
    }
}
