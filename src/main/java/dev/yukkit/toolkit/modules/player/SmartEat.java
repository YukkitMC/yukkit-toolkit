package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SmartEat extends Module {
    private final Setting.Num hunger = num("Hunger", 14, 1, 19, 1);
    private final Setting.Bool golden = bool("AllowGolden", false);
    private final Setting.Num delay = num("Delay", 10, 0, 60, 1);

    private final HelperEat eater = new HelperEat();
    private int cooldown;

    public SmartEat() {
        super("SmartEat", "Eats the hotbar food that wastes the least hunger once you are hungry.", Category.PLAYER);
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
        if (mc.gui.screen() != null || p.isUsingItem() || p.isSpectator() || mc.options.keyUse.isDown()) return;
        if (!p.getFoodData().needsFood() || p.getFoodData().getFoodLevel() > hunger.asInt()) return;

        int missing = 20 - p.getFoodData().getFoodLevel();
        int best = -1;
        int bestWaste = Integer.MAX_VALUE;
        int bestNutrition = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.isEmpty() || !acceptable(s)) continue;
            var food = s.get(DataComponents.FOOD);
            if (food == null) continue;
            int n = food.nutrition();
            int waste = Math.max(0, n - missing);
            if (waste < bestWaste || (waste == bestWaste && n > bestNutrition)) {
                best = i;
                bestWaste = waste;
                bestNutrition = n;
            }
        }
        if (best >= 0 && eater.start(best)) cooldown = delay.asInt();
    }

    private boolean acceptable(ItemStack s) {
        if (s.is(Items.ROTTEN_FLESH) || s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO)
                || s.is(Items.PUFFERFISH) || s.is(Items.CHICKEN) || s.is(Items.CHORUS_FRUIT)) return false;
        if (!golden.get() && (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE))) return false;
        return true;
    }
}
