package dev.yukkit.toolkit.cosmetics;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Optional;

/**
 * First stage of cosmetics: two bundled capes, rendered on the local player.
 * No invisible network traffic or claims of cross-client synchronization.
 */
public final class CapeCosmetics {
    private static final PlayerSkin.Patch AZURE = cape("azure");
    private static final PlayerSkin.Patch EMBER = cape("ember");
    private static PlayerSkin previous;
    private static PlayerSkin result;
    private static int previousSelection = -1;

    private CapeCosmetics() {}

    private static PlayerSkin.Patch cape(String name) {
        ClientAsset.ResourceTexture texture = new ClientAsset.ResourceTexture(
                Identifier.fromNamespaceAndPath("yukkit_toolkit", "textures/cape/" + name + ".png"));
        return new PlayerSkin.Patch(Optional.empty(), Optional.of(texture), Optional.empty(), Optional.empty());
    }

    public static String label() {
        return switch (ToolkitConfig.INSTANCE.capeStyle) {
            case 1 -> "Azure";
            case 2 -> "Ember";
            default -> "Vanilla";
        };
    }

    public static PlayerSkin apply(PlayerSkin original) {
        int selected = ToolkitConfig.INSTANCE.capeStyle;
        if (selected == 0 || original == null) return original;
        if (previous == original && previousSelection == selected && result != null) return result;
        previous = original;
        previousSelection = selected;
        result = original.with(selected == 1 ? AZURE : EMBER);
        return result;
    }
}
