package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class InvCleaner extends Module {
    private final Setting.Bool inventoryOnly = bool("InventoryOnly", true);
    private final Setting.Bool equipArmor = bool("EquipArmor", true);
    private final Setting.Bool dropWeaker = bool("DropWeaker", true);
    private final Setting.Bool dropJunk = bool("DropJunk", true);
    private final Setting.Bool keepHotbar = bool("KeepHotbar", false);
    private final Setting.Num delay = num("Delay", 3, 1, 20, 1);

    private static final UtilItems.Kind[] ARMOR = {
            UtilItems.Kind.HEAD, UtilItems.Kind.CHEST, UtilItems.Kind.LEGS, UtilItems.Kind.FEET};
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final UtilItems.Kind[] GEAR = {
            UtilItems.Kind.SWORD, UtilItems.Kind.PICKAXE, UtilItems.Kind.AXE, UtilItems.Kind.SHOVEL};

    private int cooldown;

    public InvCleaner() {
        super("InvCleaner", "Equips the best armor and throws away weaker gear and junk items.", Category.PLAYER);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!UtilInv.ready()) return;
        if (cooldown > 0) { cooldown--; return; }
        Player p = mc.player;
        if (p.isSpectator() || p.containerMenu != p.inventoryMenu) return;
        boolean invScreen = mc.gui.screen() instanceof InventoryScreen;
        if (mc.gui.screen() != null && !invScreen) return;
        if (inventoryOnly.get() && !invScreen) return;
        AbstractContainerMenu menu = p.inventoryMenu;
        if (!menu.getCarried().isEmpty()) return;

        if (equipArmor.get() && handleArmor(p, menu)) { cooldown = delay.asInt(); return; }
        if ((dropWeaker.get() || dropJunk.get()) && handleDrops(p, menu)) cooldown = delay.asInt();
    }

    /** Menu slots 9-44 are main inventory (9-35) and hotbar (36-44). */
    private boolean handleArmor(Player p, AbstractContainerMenu menu) {
        for (int k = 0; k < ARMOR.length; k++) {
            int best = -1;
            double bestScore = -1;
            for (int i = 9; i <= 44; i++) {
                ItemStack s = menu.getSlot(i).getItem();
                if (UtilItems.kind(s) != ARMOR[k]) continue;
                double sc = UtilItems.score(s);
                if (sc > bestScore) { best = i; bestScore = sc; }
            }
            if (best < 0) continue;
            ItemStack worn = p.getItemBySlot(ARMOR_SLOTS[k]);
            if (worn.isEmpty()) {
                UtilInv.click(menu, best, 0, ContainerInput.QUICK_MOVE);
                return true;
            }
            if (bestScore > UtilItems.score(worn) * 1.01) {
                // Shift-click the worn piece back into the inventory; the better one is equipped on a later pass.
                UtilInv.click(menu, 5 + k, 0, ContainerInput.QUICK_MOVE);
                return true;
            }
        }
        return false;
    }

    private boolean handleDrops(Player p, AbstractContainerMenu menu) {
        int first = 9;
        int last = keepHotbar.get() ? 35 : 44;
        for (int i = first; i <= last; i++) {
            ItemStack s = menu.getSlot(i).getItem();
            if (s.isEmpty()) continue;
            if (dropJunk.get() && UtilItems.isJunk(s)) {
                UtilInv.click(menu, i, 1, ContainerInput.THROW);
                return true;
            }
            if (dropWeaker.get() && isWeaker(p, menu, i, s)) {
                UtilInv.click(menu, i, 1, ContainerInput.THROW);
                return true;
            }
        }
        return false;
    }

    /** A gear stack is dropped when a better stack of the same kind exists (ties keep the first one). */
    private boolean isWeaker(Player p, AbstractContainerMenu menu, int slot, ItemStack stack) {
        UtilItems.Kind kind = UtilItems.kind(stack);
        if (kind == UtilItems.Kind.NONE) return false;
        boolean known = kind.isArmor();
        for (UtilItems.Kind g : GEAR) if (g == kind) known = true;
        if (!known) return false;

        double mine = UtilItems.score(stack);
        if (kind.isArmor()) {
            for (int k = 0; k < ARMOR.length; k++) {
                if (ARMOR[k] != kind) continue;
                ItemStack worn = p.getItemBySlot(ARMOR_SLOTS[k]);
                if (!worn.isEmpty() && UtilItems.score(worn) * 1.01 >= mine) return true;
            }
        }
        for (int i = 9; i <= 44; i++) {
            if (i == slot) continue;
            ItemStack other = menu.getSlot(i).getItem();
            if (UtilItems.kind(other) != kind) continue;
            double sc = UtilItems.score(other);
            if (sc > mine || (sc == mine && i < slot)) return true;
        }
        return false;
    }
}
