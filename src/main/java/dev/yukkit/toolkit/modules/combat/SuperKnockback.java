package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.player.Player;

/** Re-triggers your sprint right before a hit so every attack deals sprint knockback. */
public final class SuperKnockback extends Module {
    private final Setting.Bool onlyWhenSprinting = bool("Only When Sprinting", true);

    public SuperKnockback() {
        super("SuperKnockback", "Resets sprint on each hit for maximum knockback.", Category.COMBAT);
    }

    @Override public boolean onSendPacket(Packet<?> packet) {
        if (!hasWorld()) return false;
        if (!(packet instanceof ServerboundAttackPacket) && !(packet instanceof ServerboundInteractPacket)) return false;
        Player p = mc.player;
        if (p.isSpectator() || p.isPassenger() || p.isInWater()) return false;
        if (onlyWhenSprinting.get() && !p.isSprinting()) return false;
        // Stop then start sprinting, so the server treats the hit as coming from a fresh sprint.
        ModuleManager.sendDirect(new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        ModuleManager.sendDirect(new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        return false;
    }
}
