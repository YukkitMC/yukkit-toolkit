package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;

public final class NoFall extends Module {
    private final Setting.Num distance = num("Distance", 2.0, 0.5, 3.0, 0.1);
    private final Setting.Bool skipWater = bool("SkipWater", true);

    public NoFall() {
        super("NoFall", "Tells the server you landed before fall damage can build up.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        Player p = mc.player;
        if (p.onGround() || p.isSpectator() || p.isPassenger() || p.isFallFlying()) return;
        if (p.getAbilities().flying || p.getAbilities().instabuild) return;
        if (skipWater.get() && (p.isInWater() || p.isInLiquid())) return;
        if (p.fallDistance > distance.get()) {
            ModuleManager.sendDirect(new ServerboundMovePlayerPacket.StatusOnly(true, p.horizontalCollision));
            p.fallDistance = 0;
        }
    }

    @Override
    public String tag() { return String.format(java.util.Locale.ROOT, "%.1f", distance.get()); }
}
