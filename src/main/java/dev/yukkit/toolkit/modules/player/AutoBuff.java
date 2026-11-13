package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class AutoBuff extends Module {
    private final Setting.Num health = num("Health", 10, 2, 19, 1);
    private final Setting.Bool healPotions = bool("HealingPotions", true);
    private final Setting.Bool goldenApple = bool("GoldenApple", true);
    private final Setting.Bool speed = bool("Speed", false);
    private final Setting.Bool strength = bool("Strength", false);
    private final Setting.Bool resistance = bool("Resistance", false);
    private final Setting.Num delay = num("Delay", 15, 5, 100, 1);

    private final HelperEat eater = new HelperEat();
    private int cooldown;

    public AutoBuff() {
        super("AutoBuff", "Throws healing/buff splash potions at your feet and eats golden apples when needed.", Category.PLAYER);
    }

    @Override public void onEnable() { cooldown = 0; }
    @Override public void onDisable() { eater.abort(); }
    @Override public void onWorldChange() { eater.abort(); }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gameMode == null) { eater.abort(); return; }
        if (eater.active()) {
            if (mc.gui.screen() != null) eater.abort(); else eater.tick();
            return;
        }
        if (cooldown > 0) { cooldown--; return; }
        Player p = mc.player;
        if (mc.gui.screen() != null || p.isUsingItem() || p.isSpectator() || p.isDeadOrDying()) return;

        boolean low = p.getHealth() <= health.get();
        if (low && healPotions.get()) {
            int slot = UtilInv.findHotbar(p, s -> s.is(Items.SPLASH_POTION) && isHealing(s));
            if (slot >= 0) { throwAt(p, slot); return; }
        }
        if (low && goldenApple.get() && !p.hasEffect(MobEffects.ABSORPTION)) {
            int slot = UtilInv.findHotbar(p, s -> s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE));
            if (slot >= 0 && !mc.options.keyUse.isDown() && eater.start(slot)) { cooldown = delay.asInt(); return; }
        }
        if (speed.get() || strength.get() || resistance.get()) {
            int slot = UtilInv.findHotbar(p, s -> s.is(Items.SPLASH_POTION) && wantedBuff(p, s));
            if (slot >= 0) throwAt(p, slot);
        }
    }

    private void throwAt(Player p, int slot) {
        UtilInv.useAimed(p, slot, 90f);
        cooldown = delay.asInt();
    }

    private static boolean onlyBeneficial(List<MobEffectInstance> effects) {
        if (effects.isEmpty()) return false;
        for (MobEffectInstance e : effects) {
            if (e.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL) return false;
        }
        return true;
    }

    private static boolean isHealing(ItemStack s) {
        List<MobEffectInstance> effects = UtilItems.potionEffects(s);
        if (!onlyBeneficial(effects)) return false;
        for (MobEffectInstance e : effects) {
            if (e.getEffect() == MobEffects.INSTANT_HEALTH) return true;
        }
        return false;
    }

    private boolean wantedBuff(Player p, ItemStack s) {
        List<MobEffectInstance> effects = UtilItems.potionEffects(s);
        if (!onlyBeneficial(effects)) return false;
        for (MobEffectInstance e : effects) {
            Holder<MobEffect> h = e.getEffect();
            if (wants(h) && !p.hasEffect(h)) return true;
        }
        return false;
    }

    private boolean wants(Holder<MobEffect> h) {
        return (speed.get() && h == MobEffects.SPEED)
                || (strength.get() && h == MobEffects.STRENGTH)
                || (resistance.get() && h == MobEffects.RESISTANCE);
    }
}
