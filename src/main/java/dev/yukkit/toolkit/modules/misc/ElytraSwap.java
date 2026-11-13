package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * One-shot module: switches the chest slot between an elytra and your best chestplate, then
 * turns itself off. Bind it to a key (or click it) when you want to swap.
 */
public final class ElytraSwap extends Module {
    private static final int CHEST_SLOT = 6; // InventoryMenu: 5 head, 6 chest, 7 legs, 8 feet
    private static final Item[] CHESTPLATES = {
            Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE,
            Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE
    };

    private final Setting.Choice mode = choice("Mode", "Toggle", "Toggle", "Elytra", "Chestplate");
    private final Setting.Bool autoGlide = bool("Auto Glide", true);

    private int stage;      // 0 idle, 1 swap pending, 2 gliding countdown
    private int glideWait;

    public ElytraSwap() {
        super("ElytraSwap", "Swaps between elytra and chestplate, then turns off.", Category.MISC);
    }

    @Override
    public void onEnable() {
        stage = 1;
        glideWait = 0;
    }

    @Override
    public void onDisable() { stage = 0; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gameMode == null) return;
        Player player = mc.player;
        if (stage == 1) {
            boolean equippedElytra = swap(player);
            if (equippedElytra && autoGlide.get()) {
                stage = 2;
                glideWait = 3;
            } else {
                setEnabled(false);
            }
        } else if (stage == 2) {
            if (glideWait-- > 0) return;
            if (!player.onGround() && !player.isFallFlying() && !player.isInWater()
                    && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                ModuleManager.sendDirect(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
            setEnabled(false);
        }
    }

    /** @return true when an elytra ended up in the chest slot. */
    private boolean swap(Player player) {
        InventoryMenu menu = player.inventoryMenu;
        if (player.containerMenu != menu || !menu.getCarried().isEmpty()) {
            UtilChat.alert("ElytraSwap: close the open container first.");
            return false;
        }
        ItemStack worn = menu.getSlot(CHEST_SLOT).getItem();
        boolean wearingElytra = worn.is(Items.ELYTRA);
        boolean hadChestPiece = !worn.isEmpty();
        boolean wantElytra;
        if (mode.is("Elytra")) wantElytra = true;
        else if (mode.is("Chestplate")) wantElytra = false;
        else wantElytra = !wearingElytra;

        if (wantElytra && wearingElytra) return true;
        int source = wantElytra ? findElytra(menu) : findChestplate(menu);
        if (source < 0) {
            UtilChat.alert(wantElytra ? "ElytraSwap: no usable elytra in the inventory." : "ElytraSwap: no chestplate in the inventory.");
            return false;
        }
        click(player, menu, source);
        click(player, menu, CHEST_SLOT);
        if (hadChestPiece) click(player, menu, source); // put the old chest piece into the freed slot
        return wantElytra;
    }

    private void click(Player player, InventoryMenu menu, int slot) {
        mc.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, player);
    }

    private static int findElytra(InventoryMenu menu) {
        for (int i = 9; i <= 44; i++) {
            ItemStack s = menu.getSlot(i).getItem();
            if (!s.is(Items.ELYTRA)) continue;
            if (s.isDamageableItem() && s.getMaxDamage() - s.getDamageValue() <= 1) continue; // about to break
            return i;
        }
        return -1;
    }

    private static int findChestplate(InventoryMenu menu) {
        int best = -1;
        int bestRank = Integer.MAX_VALUE;
        for (int i = 9; i <= 44; i++) {
            ItemStack s = menu.getSlot(i).getItem();
            if (s.isEmpty()) continue;
            for (int rank = 0; rank < CHESTPLATES.length; rank++) {
                if (s.is(CHESTPLATES[rank]) && rank < bestRank) {
                    best = i;
                    bestRank = rank;
                }
            }
        }
        return best;
    }

    @Override
    public String tag() { return mode.get(); }
}
