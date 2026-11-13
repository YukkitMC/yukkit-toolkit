package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.Packet;

/** Writes packet class names to the game log (latest.log) for debugging. */
public final class PacketLogger extends Module {
    private static final String[] NOISY = {
            "MovePlayerPacket", "KeepAlive", "MoveEntity", "RotateHead", "EntityPositionSync", "SetTime",
            "LevelChunk", "LightUpdate", "TeleportEntity", "SetEntityMotion", "SetEntityData", "Ping", "Pong",
            "ClientTickEnd", "ChunkBatch", "BlockUpdate", "UpdateAttributes", "ForgetLevelChunk", "BlockEntityData",
            "SetEquipment", "Animate", "Sound", "Bundle", "PlayerInput", "PlayerInfo", "EntityEvent"
    };

    private final Setting.Choice direction = choice("Direction", "Both", "Both", "Outgoing", "Incoming");
    private final Setting.Bool skipNoisy = bool("Skip Noisy", true);
    private final Setting.Bool details = bool("Details", false);

    public PacketLogger() {
        super("PacketLogger", "Logs network packets to the game log.", Category.MISC);
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (!direction.is("Incoming")) log("OUT", packet);
        return false;
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (!direction.is("Outgoing")) log("IN ", packet);
        return false;
    }

    private void log(String dir, Packet<?> packet) {
        String name = packet.getClass().getName();
        if (skipNoisy.get()) {
            for (String n : NOISY) if (name.contains(n)) return;
        }
        String simple = packet.getClass().getSimpleName();
        if (details.get()) {
            String text = String.valueOf(packet);
            if (text.length() > 240) text = text.substring(0, 240) + "...";
            ModuleManager.LOGGER.info("[PacketLogger] {} {} {}", dir, simple, text);
        } else {
            ModuleManager.LOGGER.info("[PacketLogger] {} {}", dir, simple);
        }
    }

    @Override
    public String tag() { return direction.get(); }
}
