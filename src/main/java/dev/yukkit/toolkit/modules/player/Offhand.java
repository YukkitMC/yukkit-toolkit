package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class Offhand extends Module {
    private final Setting.Choice item = choice("Item", "Shield", "Shield", "GoldenApple", "Crystal", "Totem");
    private final Setting.Num totemHealth = num("TotemHealth", 6, 0, 19, 1);
    private final Setting.Num delay = num("Delay", 6, 1, 40, 1);

    private int cooldown;

    public Offhand() {
        super("Offhand", "Keeps a chosen item in your offhand, switching to a totem when health is low.", Category.PLAYER);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!UtilInv.ready()) return;
        if (cooldown > 0) { cooldown--; return; }
        Player p = mc.player;
        if (p.isSpectator() || p.isUsingItem() || p.containerMenu != p.inventoryMenu) return;
        AbstractContainerMenu menu = p.inventoryMenu;
        if (!menu.getCarried().isEmpty()) return;

        Item wanted = desired(p);
        if (p.getOffhandItem().is(wanted)) return;
        for (int i = 9; i <= 44; i++) {
            ItemStack s = menu.getSlot(i).getItem();
            if (s.isEmpty() || !s.is(wanted)) continue;
            UtilInv.click(menu, i, 40, ContainerInput.SWAP);
            cooldown = delay.asInt();
            return;
        }
    }

    private Item desired(Player p) {
        Item chosen = switch (item.get()) {
            case "GoldenApple" -> Items.GOLDEN_APPLE;
            case "Crystal" -> Items.END_CRYSTAL;
            case "Totem" -> Items.TOTEM_OF_UNDYING;
            default -> Items.SHIELD;
        };
        if (totemHealth.asInt() > 0 && p.getHealth() <= totemHealth.get() && hasItem(p, Items.TOTEM_OF_UNDYING)) {
            return Items.TOTEM_OF_UNDYING;
        }
        return chosen;
    }

    private static boolean hasItem(Player p, Item it) {
        for (int i = 0; i < 36; i++) if (p.getInventory().getItem(i).is(it)) return true;
        return false;
    }

    @Override
    public String tag() { return item.get(); }
}
