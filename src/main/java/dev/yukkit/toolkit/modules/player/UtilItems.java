package dev.yukkit.toolkit.modules.player;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;

/** Item classification helpers shared by the inventory modules. */
final class UtilItems {
    private UtilItems() {}

    enum Kind { SWORD, PICKAXE, AXE, SHOVEL, HEAD, CHEST, LEGS, FEET, NONE;
        boolean isArmor() { return this == HEAD || this == CHEST || this == LEGS || this == FEET; }
    }

    static Kind kind(ItemStack s) {
        if (s.isEmpty()) return Kind.NONE;
        if (s.is(ItemTags.SWORDS)) return Kind.SWORD;
        if (s.is(ItemTags.PICKAXES)) return Kind.PICKAXE;
        if (s.is(ItemTags.AXES)) return Kind.AXE;
        if (s.is(ItemTags.SHOVELS)) return Kind.SHOVEL;
        if (s.is(ItemTags.HEAD_ARMOR)) return Kind.HEAD;
        if (s.is(ItemTags.CHEST_ARMOR)) return Kind.CHEST;
        if (s.is(ItemTags.LEG_ARMOR)) return Kind.LEGS;
        if (s.is(ItemTags.FOOT_ARMOR)) return Kind.FEET;
        return Kind.NONE;
    }

    /** Rough quality: tier is approximated by durability, enchanted gear gets a bonus. */
    static double score(ItemStack s) {
        if (s.isEmpty()) return -1;
        double base = Math.max(1, s.getMaxDamage());
        if (s.isEnchanted()) base *= 1.5;
        return base;
    }

    static boolean isJunk(ItemStack s) {
        return s.is(Items.ROTTEN_FLESH) || s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO) || s.is(Items.BONE);
    }

    static List<MobEffectInstance> potionEffects(ItemStack s) {
        List<MobEffectInstance> out = new ArrayList<>();
        PotionContents pc = s.get(DataComponents.POTION_CONTENTS);
        if (pc == null) return out;
        for (MobEffectInstance e : pc.getAllEffects()) out.add(e);
        return out;
    }
}
