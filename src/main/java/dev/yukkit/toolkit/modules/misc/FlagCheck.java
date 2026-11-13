package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;

/** Counts server-side position corrections ("flags" / setbacks) and shows them. */
public final class FlagCheck extends Module {
    private final Setting.Num threshold = num("Min Distance", 0.5, 0.1, 5.0, 0.1);
    private final Setting.Bool chat = bool("Chat Alert", true);
    private final Setting.Bool sound = bool("Sound", false);
    private final Setting.Bool hud = bool("HUD", true);

    private int ticks;
    private int worldTick;
    private int flags;
    private int lastFlagTick = Integer.MIN_VALUE / 2;

    public FlagCheck() {
        super("FlagCheck", "Detects server setbacks (teleports back) and counts them.", Category.MISC);
    }

    @Override
    public void onEnable() { flags = 0; }

    @Override
    public void onWorldChange() { worldTick = ticks; }

    @Override
    public void onTick() { ticks++; }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerPositionPacket pos) {
            Vec3 target = pos.change().position();
            // Queued ahead of the game's own handling, so the player is still at the old position here.
            mc.execute(() -> handle(target));
        }
        return false;
    }

    private void handle(Vec3 target) {
        if (!hasWorld() || ticks - worldTick < 40) return;
        double distance = target.distanceTo(mc.player.position());
        if (distance < threshold.get()) return;
        flags++;
        lastFlagTick = ticks;
        if (chat.get()) UtilChat.info(String.format(java.util.Locale.ROOT, "§eSetback #%d (%.1f blocks)", flags, distance));
        if (sound.get()) UtilChat.ping();
    }

    @Override
    public void onHud(GuiGraphicsExtractor g) {
        if (!hud.get() || ticks - lastFlagTick > 200) return;
        String text = "Flags: " + flags;
        g.text(mc.font, text, g.guiWidth() / 2 - mc.font.width(text) / 2, 24, 0xFFFF5555, true);
    }

    @Override
    public String tag() { return flags == 0 ? "" : Integer.toString(flags); }
}
