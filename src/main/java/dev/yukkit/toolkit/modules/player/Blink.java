package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class Blink extends Module {
    private static final GizmoStyle GHOST = GizmoStyle.strokeAndFill(0xFF4DA3FF, 1.5f, 0x224DA3FF);

    private final Setting.Bool pulse = bool("Pulse", false);
    private final Setting.Num pulseTicks = num("PulseTicks", 20, 2, 200, 1);
    private final Setting.Num limit = num("MaxTicks", 0, 0, 600, 10);
    private final Setting.Bool ghost = bool("Ghost", true);

    private final List<Packet<?>> queue = new ArrayList<>();
    private Vec3 serverPos;
    private int ticks;

    public Blink() {
        super("Blink", "Holds back movement packets so the server sees you jump to the new position when released.", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        synchronized (queue) { queue.clear(); }
        ticks = 0;
        serverPos = mc.player != null ? mc.player.position() : null;
    }

    @Override
    public void onDisable() {
        if (hasWorld() && mc.getConnection() != null) flush(); else synchronized (queue) { queue.clear(); }
        serverPos = null;
    }

    @Override
    public void onWorldChange() {
        synchronized (queue) { queue.clear(); }
        ticks = 0;
        serverPos = mc.player != null ? mc.player.position() : null;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket) || !hasWorld()) return false;
        synchronized (queue) { queue.add(packet); }
        return true;
    }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        ticks++;
        boolean due = pulse.get() && ticks >= pulseTicks.asInt();
        int max = limit.asInt();
        if (max > 0 && ticks >= max) due = true;
        if (due) flush();
    }

    private void flush() {
        List<Packet<?>> copy;
        synchronized (queue) {
            copy = new ArrayList<>(queue);
            queue.clear();
        }
        for (Packet<?> p : copy) ModuleManager.sendDirect(p);
        ticks = 0;
        if (mc.player != null) serverPos = mc.player.position();
    }

    @Override
    public void onWorld() {
        if (!ghost.get() || serverPos == null || !hasWorld()) return;
        Player p = mc.player;
        AABB box = p.getBoundingBox().move(serverPos.x - p.getX(), serverPos.y - p.getY(), serverPos.z - p.getZ());
        Gizmos.cuboid(box, GHOST).setAlwaysOnTop();
    }

    @Override
    public String tag() {
        synchronized (queue) { return Integer.toString(queue.size()); }
    }
}
