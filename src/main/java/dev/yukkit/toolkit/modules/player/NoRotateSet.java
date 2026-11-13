package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;

public final class NoRotateSet extends Module {
    private float yaw;
    private float pitch;
    private int restore;

    public NoRotateSet() {
        super("NoRotateSet", "Keeps your own view direction when the server teleports or turns you.", Category.PLAYER);
    }

    @Override public void onEnable() { restore = 0; }
    @Override public void onWorldChange() { restore = 0; }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if ((packet instanceof ClientboundPlayerPositionPacket || packet instanceof ClientboundPlayerRotationPacket) && hasWorld()) {
            yaw = mc.player.getYRot();
            pitch = mc.player.getXRot();
            restore = 3;
        }
        return false;
    }

    @Override
    public void onTick() {
        if (restore <= 0) return;
        restore--;
        if (!hasWorld()) return;
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}
