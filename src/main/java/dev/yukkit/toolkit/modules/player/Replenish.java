package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class Replenish extends Module {
    private final Setting.Num threshold = num("Threshold", 8, 1, 32, 1);
    private final Setting.Num delay = num("Delay", 3, 1, 20, 1);
    private final Setting.Bool inventoryOnly = bool("InventoryOnly", false);

    private int cooldown;

    public Replenish() {
        super("Replenish", "Refills low hotbar stacks from your main inventory.", Category.PLAYER);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!UtilInv.ready()) return;
        if (cooldown > 0) { cooldown--; return; }
        Player p = mc.player;
        if (p.isSpectator() || p.containerMenu != p.inventoryMenu) return;
        if (mc.gui.screen() != null && !(mc.gui.screen() instanceof InventoryScreen)) return;
        if (inventoryOnly.get() && !(mc.gui.screen() instanceof InventoryScreen)) return;
        AbstractContainerMenu menu = p.inventoryMenu;
        if (!menu.getCarried().isEmpty()) return;

        for (int h = 0; h < 9; h++) {
            ItemStack hot = p.getInventory().getItem(h);
            if (hot.isEmpty() || !hot.isStackable() || hot.getCount() > threshold.asInt() || hot.getCount() >= hot.getMaxStackSize()) continue;
            for (int src = 9; src < 36; src++) {
                ItemStack from = p.getInventory().getItem(src);
                if (from.isEmpty() || !ItemStack.isSameItemSameComponents(hot, from)) continue;
                UtilInv.click(menu, src, 0, ContainerInput.PICKUP);
                UtilInv.click(menu, UtilInv.hotbarMenuSlot(h), 0, ContainerInput.PICKUP);
                if (!menu.getCarried().isEmpty()) UtilInv.click(menu, src, 0, ContainerInput.PICKUP);
                cooldown = delay.asInt();
                return;
            }
        }
    }
}
