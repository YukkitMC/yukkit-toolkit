package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public final class ChestCleaner extends Module {
    private final Setting.Num delay = num("Delay", 3, 0, 20, 1);

    private int openTicks;
    private int wait;
    private int lastMenu = -1;

    public ChestCleaner() {
        super("ChestCleaner", "Throws junk items (rotten flesh, spider eyes, bones, ...) out of an opened chest.", Category.PLAYER);
    }

    @Override public void onEnable() { lastMenu = -1; }
    @Override public void onWorldChange() { lastMenu = -1; }

    @Override
    public void onTick() {
        if (!UtilInv.ready()) return;
        Player p = mc.player;
        if (!(mc.gui.screen() instanceof AbstractContainerScreen<?>)) { lastMenu = -1; return; }
        AbstractContainerMenu menu = p.containerMenu;
        if (!UtilInv.isStorageMenu(p, menu)) return;
        if (menu.containerId != lastMenu) {
            lastMenu = menu.containerId;
            openTicks = 0;
            wait = 0;
        }
        if (openTicks < 3) { openTicks++; return; }
        if (wait > 0) { wait--; return; }
        if (!menu.getCarried().isEmpty()) return;

        for (Slot slot : menu.slots) {
            if (slot.container == p.getInventory() || slot.getItem().isEmpty()) continue;
            if (!UtilItems.isJunk(slot.getItem())) continue;
            UtilInv.click(menu, slot.index, 1, ContainerInput.THROW);
            wait = delay.asInt();
            return;
        }
    }
}
