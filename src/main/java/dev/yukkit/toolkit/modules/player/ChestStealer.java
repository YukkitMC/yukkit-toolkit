package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ChestStealer extends Module {
    private final Setting.Num delay = num("Delay", 2, 0, 20, 1);
    private final Setting.Num perTick = num("ClicksPerTick", 1, 1, 6, 1);
    private final Setting.Bool skipJunk = bool("SkipJunk", true);
    private final Setting.Bool autoClose = bool("AutoClose", true);

    private int openTicks;
    private int wait;
    private int lastMenu = -1;

    public ChestStealer() {
        super("ChestStealer", "Takes everything out of an opened chest, barrel, shulker box or hopper.", Category.PLAYER);
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

        int clicks = 0;
        boolean pending = false;
        for (Slot slot : menu.slots) {
            if (slot.container == p.getInventory()) continue;
            ItemStack s = slot.getItem();
            if (s.isEmpty()) continue;
            if (skipJunk.get() && UtilItems.isJunk(s)) continue;
            if (!UtilInv.hasRoomFor(p, s)) continue;
            pending = true;
            UtilInv.click(menu, slot.index, 0, ContainerInput.QUICK_MOVE);
            if (++clicks >= perTick.asInt()) break;
        }
        if (clicks > 0) {
            wait = delay.asInt();
        } else if (!pending && autoClose.get()) {
            p.closeContainer();
        }
    }
}
