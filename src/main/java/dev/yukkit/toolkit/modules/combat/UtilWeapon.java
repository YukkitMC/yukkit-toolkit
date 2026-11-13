package dev.yukkit.toolkit.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Hotbar weapon scoring shared by KillAura, AutoWeapon and friends. */
final class UtilWeapon {
    private static final Minecraft mc = Minecraft.getInstance();

    private UtilWeapon() {}

    static boolean isMelee(ItemStack s) {
        return !s.isEmpty() && (s.is(ItemTags.SWORDS) || s.is(ItemTags.AXES) || s.is(ItemTags.SPEARS)
                || s.is(Items.MACE) || s.is(Items.TRIDENT));
    }

    private static double tier(ItemStack s) {
        if (s.is(Items.NETHERITE_SWORD) || s.is(Items.NETHERITE_AXE)) return 6;
        if (s.is(Items.DIAMOND_SWORD) || s.is(Items.DIAMOND_AXE)) return 5;
        if (s.is(Items.IRON_SWORD) || s.is(Items.IRON_AXE)) return 4;
        if (s.is(Items.STONE_SWORD) || s.is(Items.STONE_AXE)) return 3;
        if (s.is(Items.GOLDEN_SWORD) || s.is(Items.GOLDEN_AXE)) return 2;
        if (s.is(Items.WOODEN_SWORD) || s.is(Items.WOODEN_AXE)) return 1;
        return 3.5; // copper and anything newer sits between stone and iron
    }

    /** Higher is better, -1 when the stack is not a melee weapon. */
    static double score(ItemStack s, boolean preferAxe) {
        if (!isMelee(s)) return -1;
        double base;
        if (s.is(ItemTags.SWORDS)) base = tier(s) + (preferAxe ? 0 : 0.5);
        else if (s.is(ItemTags.AXES)) base = tier(s) + (preferAxe ? 0.5 : 0);
        else if (s.is(Items.MACE)) base = 5.2;
        else if (s.is(Items.TRIDENT)) base = 3.8;
        else base = 3.0; // spears
        // Prefer the less damaged tool when two are otherwise equal.
        if (s.isDamageableItem() && s.getMaxDamage() > 0) {
            base += 0.1 * (1.0 - (double) s.getDamageValue() / s.getMaxDamage());
        }
        return base;
    }

    /** Selects the best hotbar weapon locally; the next attack syncs the slot to the server. */
    static void selectBest(boolean preferAxe) {
        Player p = mc.player;
        if (p == null) return;
        int best = -1;
        double bestScore = score(p.getInventory().getItem(p.getInventory().getSelectedSlot()), preferAxe);
        int current = p.getInventory().getSelectedSlot();
        for (int i = 0; i < 9; i++) {
            double s = score(p.getInventory().getItem(i), preferAxe);
            if (s > bestScore + 1e-9) {
                bestScore = s;
                best = i;
            }
        }
        if (best >= 0 && best != current) p.getInventory().setSelectedSlot(best);
    }

    static int findHotbar(java.util.function.Predicate<ItemStack> test) {
        Player p = mc.player;
        if (p == null) return -1;
        for (int i = 0; i < 9; i++) if (test.test(p.getInventory().getItem(i))) return i;
        return -1;
    }
}
