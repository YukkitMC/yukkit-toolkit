package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public final class PotionSpoof extends Module {
    private static final int DURATION = 1_000_000;

    private final Setting.Bool nightVision = bool("NightVision", true);
    private final Setting.Bool haste = bool("Haste", false);
    private final Setting.Bool speed = bool("Speed", false);
    private final Setting.Bool jumpBoost = bool("JumpBoost", false);
    private final Setting.Num level = num("Level", 1, 1, 5, 1);

    private final Set<Holder<MobEffect>> mine = new HashSet<>();

    public PotionSpoof() {
        super("PotionSpoof", "Gives you client-side potion effects (visual or movement only; servers keep the real state).", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            for (Holder<MobEffect> h : mine) mc.player.removeEffect(h);
        }
        mine.clear();
    }

    @Override public void onWorldChange() { mine.clear(); }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        Player p = mc.player;
        int amp = level.asInt() - 1;
        sync(p, nightVision.get(), MobEffects.NIGHT_VISION, 0);
        sync(p, haste.get(), MobEffects.HASTE, amp);
        sync(p, speed.get(), MobEffects.SPEED, amp);
        sync(p, jumpBoost.get(), MobEffects.JUMP_BOOST, amp);
    }

    private void sync(Player p, boolean want, Holder<MobEffect> effect, int amplifier) {
        if (want) {
            if (!p.hasEffect(effect)) {
                p.addEffect(new MobEffectInstance(effect, DURATION, amplifier));
                mine.add(effect);
            }
        } else if (mine.remove(effect)) {
            p.removeEffect(effect);
        }
    }
}
