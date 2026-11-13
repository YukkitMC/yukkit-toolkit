package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class NoSlotSet extends Module {
    public NoSlotSet() {
        super("NoSlotSet", "Ignores the server forcing your selected hotbar slot and tells it your real slot instead.", Category.PLAYER);
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof ClientboundSetHeldSlotPacket) || !hasWorld()) return false;
        ModuleManager.sendDirect(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
        return true;
    }
}
