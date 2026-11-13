package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Flickers your skin layers (jacket, sleeves, pants, hat, cape). Your original layers return when it stops. */
public final class SkinDerp extends Module {
    private final Setting.Choice mode = choice("Mode", "Random", "Random", "Strobe", "Cycle");
    private final Setting.Num interval = num("Interval (ticks)", 10, 2, 100, 1);

    private final Set<PlayerModelPart> original = EnumSet.noneOf(PlayerModelPart.class);
    private boolean saved;
    private int timer;
    private int step;

    public SkinDerp() {
        super("SkinDerp", "Flickers your skin layers on and off.", Category.FUN);
    }

    @Override
    public void onEnable() {
        original.clear();
        original.addAll(UtilSkin.enabledParts());
        saved = true;
        timer = 0;
        step = 0;
    }

    @Override
    public void onDisable() {
        if (!saved) return;
        saved = false;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            mc.options.setModelPart(part, original.contains(part));
        }
    }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.getConnection() == null) return;
        if (timer-- > 0) return;
        timer = interval.asInt();
        step++;
        PlayerModelPart[] parts = PlayerModelPart.values();
        if (mode.is("Random")) {
            for (PlayerModelPart part : parts) mc.options.setModelPart(part, ThreadLocalRandom.current().nextBoolean());
        } else if (mode.is("Strobe")) {
            boolean on = step % 2 == 0;
            for (PlayerModelPart part : parts) mc.options.setModelPart(part, on);
        } else {
            int active = step % parts.length;
            for (int i = 0; i < parts.length; i++) mc.options.setModelPart(parts[i], i == active);
        }
    }

    @Override
    public String tag() { return mode.get(); }
}
