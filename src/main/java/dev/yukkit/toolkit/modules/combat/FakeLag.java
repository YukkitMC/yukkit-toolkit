package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;

import java.util.ArrayList;
import java.util.List;

/** Holds back your movement packets for a short time so the server sees you with a small delay. */
public final class FakeLag extends Module {
    private final Setting.Num delay = num("Delay (ms)", 150, 20, 800, 10);
    private final Setting.Bool onlyNearEnemy = bool("Only Near Enemies", true);
    private final Setting.Num enemyRange = num("Enemy Range", 10, 3, 30, 1);
    private final Setting.Bool flushOnHit = bool("Flush On Attack", true);

    private final List<Packet<?>> queue = new ArrayList<>();
    private long since;

    public FakeLag() {
        super("FakeLag", "Delays movement packets to desync your position.", Category.COMBAT);
    }

    @Override public String tag() { return delay.asInt() + "ms"; }

    @Override public void onEnable() { queue.clear(); }

    @Override public void onDisable() { flush(); }

    @Override public void onWorldChange() { queue.clear(); }

    private boolean shouldLag() {
        if (!hasWorld() || mc.player.isSpectator()) return false;
        if (!onlyNearEnemy.get()) return true;
        return !UtilCombat.find(new UtilCombat.Filter(true, false, false, false, false, true,
                enemyRange.get(), 360)).isEmpty();
    }

    private void flush() {
        if (queue.isEmpty()) return;
        List<Packet<?>> copy = new ArrayList<>(queue);
        queue.clear();
        for (Packet<?> p : copy) ModuleManager.sendDirect(p);
    }

    @Override public boolean onSendPacket(Packet<?> packet) {
        if (!hasWorld()) return false;
        if (packet instanceof ServerboundMovePlayerPacket) {
            if (!shouldLag()) {
                flush(); // keep ordering: queued packets go first
                return false;
            }
            if (queue.isEmpty()) since = System.currentTimeMillis();
            queue.add(packet);
            return true;
        }
        boolean important = packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundUseItemPacket
                || (flushOnHit.get() && (packet instanceof ServerboundAttackPacket || packet instanceof ServerboundInteractPacket));
        if (important) flush();
        return false;
    }

    @Override public boolean onReceivePacket(Packet<?> packet) {
        // The server moved us: any held position is stale and would rubber-band us back.
        if (packet instanceof ClientboundPlayerPositionPacket) queue.clear();
        return false;
    }

    @Override public void onTick() {
        if (queue.isEmpty()) return;
        if (!hasWorld() || System.currentTimeMillis() - since >= delay.get() || mc.player.hurtTime > 0
                || !shouldLag()) {
            flush();
        }
    }
}
