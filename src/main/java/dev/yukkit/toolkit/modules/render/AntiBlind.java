package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.effect.MobEffects;

/** Clears vision-impairing effects locally and hides the in-wall overlay (ScreenEffectMixin). */
public final class AntiBlind extends Module {
    private final Setting.Bool blindness = bool("Blindness", true);
    private final Setting.Bool darkness = bool("Darkness", true);
    private final Setting.Bool nausea = bool("Nausea", true);
    private final Setting.Bool wallOverlay = bool("WallOverlay", true);

    public AntiBlind() { super("AntiBlind", "Removes blindness, darkness, nausea and the in-block overlay.", Category.RENDER); }

    /** True when the in-block (suffocation) overlay should be hidden. */
    public static boolean hideWallOverlay() {
        AntiBlind m = ModuleManager.get(AntiBlind.class);
        return m != null && m.isEnabled() && m.wallOverlay.get();
    }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (blindness.get() && mc.player.hasEffect(MobEffects.BLINDNESS)) mc.player.removeEffect(MobEffects.BLINDNESS);
        if (darkness.get() && mc.player.hasEffect(MobEffects.DARKNESS)) mc.player.removeEffect(MobEffects.DARKNESS);
        if (nausea.get() && mc.player.hasEffect(MobEffects.NAUSEA)) mc.player.removeEffect(MobEffects.NAUSEA);
    }
}
