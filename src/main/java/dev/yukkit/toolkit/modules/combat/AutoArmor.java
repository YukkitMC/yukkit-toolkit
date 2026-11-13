package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Equips the best armor found in your inventory, one click at a time. */
public final class AutoArmor extends Module {
    private final Setting.Num delay = num("Delay", 4, 1, 20, 1);
    private final Setting.Bool keepElytra = bool("Keep Elytra", true);
    private final Setting.Bool swapWorn = bool("Replace Worn", true);

    private static final TagKey<Item>[] TAGS = armorTags();
    /** Menu slots of the armor slots in the player inventory menu: head, chest, legs, feet. */
    private static final int[] ARMOR_SLOTS = {5, 6, 7, 8};

    private int cooldown;

    public AutoArmor() {
        super("AutoArmor", "Automatically equips the best armor.", Category.COMBAT);
    }

    @SuppressWarnings("unchecked")
    private static TagKey<Item>[] armorTags() {
        return new TagKey[]{ItemTags.HEAD_ARMOR, ItemTags.CHEST_ARMOR, ItemTags.LEG_ARMOR, ItemTags.FOOT_ARMOR};
    }

    private static double material(ItemStack s) {
        String id = BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
        double base;
        if (id.contains("netherite")) base = 5.0;
        else if (id.contains("diamond")) base = 4.5;
        else if (id.contains("iron")) base = 3.5;
        else if (id.contains("copper")) base = 3.0;
        else if (id.contains("chainmail")) base = 3.0;
        else if (id.contains("gold")) base = 2.5;
        else if (id.contains("turtle")) base = 2.5;
        else if (id.contains("leather")) base = 1.5;
        else base = 3.0;
        if (s.isEnchanted()) base += 0.3;
        if (s.isDamageableItem() && s.getMaxDamage() > 0) {
            base *= 0.5 + 0.5 * (1.0 - (double) s.getDamageValue() / s.getMaxDamage());
        }
        return base;
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override public void onTick() {
        if (cooldown > 0) { cooldown--; return; }
        if (!inGame() || mc.gameMode == null) return;
        Player p = mc.player;
        if (p.isSpectator() || p.isDeadOrDying()) return;
        AbstractContainerMenu menu = p.inventoryMenu;
        if (p.containerMenu != menu || !menu.getCarried().isEmpty()) return;

        for (int piece = 0; piece < 4; piece++) {
            ItemStack worn = menu.getSlot(ARMOR_SLOTS[piece]).getItem();
            if (piece == 1 && keepElytra.get() && worn.is(Items.ELYTRA)) continue;
            double wornScore = worn.isEmpty() ? -1.0 : material(worn);

            int bestSlot = -1;
            double bestScore = wornScore;
            for (int i = 9; i <= 44; i++) {
                ItemStack cand = menu.getSlot(i).getItem();
                if (cand.isEmpty() || !cand.is(TAGS[piece])) continue;
                double sc = material(cand);
                if (sc > bestScore + 0.05) { bestScore = sc; bestSlot = i; }
            }
            if (bestSlot < 0) continue;

            if (worn.isEmpty()) {
                mc.gameMode.handleContainerInput(menu.containerId, bestSlot, 0, ContainerInput.QUICK_MOVE, p);
            } else if (swapWorn.get()) {
                // Take the old piece off first; the better one is shift-clicked on a following tick.
                mc.gameMode.handleContainerInput(menu.containerId, ARMOR_SLOTS[piece], 0, ContainerInput.QUICK_MOVE, p);
            } else {
                continue;
            }
            cooldown = delay.asInt();
            return;
        }
    }
}
