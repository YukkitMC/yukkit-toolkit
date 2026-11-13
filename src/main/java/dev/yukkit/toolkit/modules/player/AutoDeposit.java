package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AutoDeposit extends Module {
    private final Setting.Choice mode = choice("Mode", "Matching", "Matching", "All");
    private final Setting.Bool hotbar = bool("Hotbar", false);
    private final Setting.Bool keepGear = bool("KeepGear", true);
    private final Setting.Num delay = num("Delay", 2, 0, 20, 1);
    private final Setting.Bool autoClose = bool("AutoClose", false);

    private int openTicks;
    private int wait;
    private int lastMenu = -1;
    private boolean done;

    public AutoDeposit() {
        super("AutoDeposit", "Moves items from your inventory into an opened chest.", Category.PLAYER);
    }

    @Override public void onEnable() { lastMenu = -1; }
    @Override public void onWorldChange() { lastMenu = -1; }

    @Override
    public void onTick() {
        if (!UtilInv.ready()) return;
        LocalPlayer p = mc.player;
        if (!(mc.gui.screen() instanceof AbstractContainerScreen<?>)) { lastMenu = -1; return; }
        AbstractContainerMenu menu = p.containerMenu;
        if (!UtilInv.isStorageMenu(p, menu)) return;
        if (menu.containerId != lastMenu) {
            lastMenu = menu.containerId;
            openTicks = 0;
            wait = 0;
            done = false;
        }
        if (done) return;
        if (openTicks < 3) { openTicks++; return; }
        if (wait > 0) { wait--; return; }
        if (!menu.getCarried().isEmpty()) return;

        List<Slot> own = new ArrayList<>();
        List<Slot> chest = new ArrayList<>();
        for (Slot s : menu.slots) {
            if (s.container == p.getInventory()) own.add(s); else chest.add(s);
        }
        // In storage menus the player's slots come as 27 main slots followed by 9 hotbar slots.
        int limit = hotbar.get() ? own.size() : Math.min(27, own.size());
        for (int i = 0; i < limit; i++) {
            Slot s = own.get(i);
            ItemStack st = s.getItem();
            if (st.isEmpty()) continue;
            if (keepGear.get() && UtilItems.kind(st) != UtilItems.Kind.NONE) continue;
            if (mode.is("Matching") && !chestHas(chest, st)) continue;
            if (!chestHasRoom(chest, st)) continue;
            UtilInv.click(menu, s.index, 0, ContainerInput.QUICK_MOVE);
            wait = delay.asInt();
            return;
        }
        done = true;
        if (autoClose.get()) p.closeContainer();
    }

    private static boolean chestHas(List<Slot> chest, ItemStack st) {
        for (Slot c : chest) if (c.getItem().getItem() == st.getItem()) return true;
        return false;
    }

    private static boolean chestHasRoom(List<Slot> chest, ItemStack st) {
        for (Slot c : chest) {
            ItemStack s = c.getItem();
            if (s.isEmpty()) return true;
            if (s.getCount() < s.getMaxStackSize() && ItemStack.isSameItemSameComponents(s, st)) return true;
        }
        return false;
    }
}
