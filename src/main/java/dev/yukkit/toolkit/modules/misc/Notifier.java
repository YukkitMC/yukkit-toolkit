package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Chat notifications when players join or leave the server's player list. */
public final class Notifier extends Module {
    private final Setting.Bool join = bool("Join", true);
    private final Setting.Bool leave = bool("Leave", true);
    private final Setting.Bool friendsOnly = bool("Friends Only", false);
    private final Setting.Bool sound = bool("Sound", false);

    private final Map<UUID, String> names = new HashMap<>();
    private int ticks;
    private int worldTick;

    public Notifier() {
        super("Notifier", "Announces players joining and leaving the server.", Category.MISC);
    }

    @Override
    public void onEnable() { names.clear(); }

    @Override
    public void onWorldChange() {
        names.clear();
        worldTick = ticks;
    }

    @Override
    public void onTick() { ticks++; }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerInfoUpdatePacket update) {
            List<UUID> ids = new ArrayList<>();
            List<String> added = new ArrayList<>();
            for (var entry : update.newEntries()) {
                if (entry.profile() == null) continue;
                ids.add(entry.profileId());
                added.add(entry.profile().name());
            }
            if (!ids.isEmpty()) mc.execute(() -> onAdd(ids, added));
        } else if (packet instanceof ClientboundPlayerInfoRemovePacket remove) {
            List<UUID> ids = new ArrayList<>(remove.profileIds());
            mc.execute(() -> onRemove(ids));
        }
        return false;
    }

    private void onAdd(List<UUID> ids, List<String> added) {
        boolean settled = ticks - worldTick > 100; // skip the initial player list burst
        for (int i = 0; i < ids.size(); i++) {
            UUID id = ids.get(i);
            String name = added.get(i);
            boolean fresh = names.put(id, name) == null;
            if (settled && fresh && join.get()) announce(name, "joined");
        }
    }

    private void onRemove(List<UUID> ids) {
        boolean settled = ticks - worldTick > 100;
        for (UUID id : ids) {
            String name = names.remove(id);
            if (name == null && mc.getConnection() != null) {
                var info = mc.getConnection().getPlayerInfo(id);
                if (info != null) name = info.getProfile().name();
            }
            if (name != null && settled && leave.get()) announce(name, "left");
        }
    }

    private void announce(String name, String verb) {
        if (!hasWorld()) return;
        if (mc.player.getGameProfile().name().equals(name)) return;
        if (friendsOnly.get() && !UtilFriends.isFriendName(name)) return;
        UtilChat.info(name + " " + verb + " the game.");
        if (sound.get()) UtilChat.ping();
    }
}
