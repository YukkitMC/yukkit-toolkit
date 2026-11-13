package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Replaces your own name (and optionally every other player's name) in server system
 * messages before they reach the chat, which helps when streaming. Signed player chat
 * and the tab list / name tags are not rewritten.
 */
public final class NameProtect extends Module {
    private final Setting.Choice replacement = choice("Replacement", "Protected", "Protected", "You", "Player", "Anonymous");
    private final Setting.Bool others = bool("Other Players", false);

    private volatile String[][] pairs = new String[0][];
    private int refresh;

    public NameProtect() {
        super("NameProtect", "Hides player names in system chat messages.", Category.MISC);
    }

    @Override
    public void onDisable() { pairs = new String[0][]; }

    @Override
    public void onTick() {
        if (refresh-- > 0) return;
        refresh = 20;
        if (!hasWorld()) { pairs = new String[0][]; return; }
        List<String[]> list = new ArrayList<>();
        String own = mc.player.getGameProfile().name();
        if (own != null && !own.isEmpty()) list.add(new String[] {own, replacement.get()});
        if (others.get() && mc.getConnection() != null) {
            for (var info : mc.getConnection().getListedOnlinePlayers()) {
                String n = info.getProfile().name();
                if (n != null && n.length() >= 3 && !n.equals(own)) list.add(new String[] {n, "Player"});
            }
        }
        list.sort(Comparator.comparingInt((String[] p) -> p[0].length()).reversed());
        pairs = list.toArray(new String[0][]);
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (!(packet instanceof ClientboundSystemChatPacket chat) || chat.overlay()) return false;
        String[][] active = pairs;
        if (active.length == 0) return false;
        Component content = chat.content();
        if (content == null) return false;
        String plain = content.getString();
        boolean hit = false;
        for (String[] p : active) {
            if (plain.contains(p[0])) { hit = true; break; }
        }
        if (!hit) return false;
        // Runs on the network thread: rebuild the message on the client thread and drop the original.
        mc.execute(() -> {
            if (mc.player == null) return;
            mc.player.sendSystemMessage(rewrite(content, active));
        });
        return true;
    }

    private static Component rewrite(Component input, String[][] active) {
        MutableComponent out = Component.empty();
        input.<Boolean>visit((style, text) -> {
            String t = text;
            for (String[] p : active) t = t.replace(p[0], p[1]);
            out.append(Component.literal(t).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    @Override
    public String tag() { return replacement.get(); }
}
