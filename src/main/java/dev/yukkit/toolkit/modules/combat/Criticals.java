package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;

/** Makes ground attacks land as critical hits by announcing a tiny hop right before the hit. */
public final class Criticals extends Module {
    private final Setting.Choice mode = choice("Mode", "Packet", "Packet", "Jump");
    private final Setting.Num height = num("Hop Height", 0.0625, 0.01, 0.2, 0.005);

    public Criticals() {
        super("Criticals", "Turns ground hits into critical hits.", Category.COMBAT);
    }

    @Override public String tag() { return mode.get(); }

    private boolean canCrit(Player p) {
        return p.onGround() && !p.isInWater() && !p.isInLava() && !p.onClimbable() && !p.isPassenger()
                && !p.isFallFlying() && !p.isSpectator();
    }

    @Override public boolean onSendPacket(Packet<?> packet) {
        if (!hasWorld() || !mode.is("Packet")) return false;
        if (!(packet instanceof ServerboundAttackPacket) && !(packet instanceof ServerboundInteractPacket)) return false;
        Player p = mc.player;
        if (!canCrit(p)) return false;
        double x = p.getX(), y = p.getY(), z = p.getZ();
        // Rise slightly, then fall back so the server sees the player as airborne and falling at hit time.
        ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Pos(x, y + height.get(), z, false, p.horizontalCollision));
        ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Pos(x, y, z, false, p.horizontalCollision));
        return false;
    }

    @Override public void onTick() {
        if (!mode.is("Jump") || !inGame()) return;
        Player p = mc.player;
        // Hop when swinging at something on the ground so the hit lands on the way down.
        if (mc.options.keyAttack.isDown() && mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult
                && canCrit(p) && p.getAttackStrengthScale(0f) > 0.9f) {
            var dm = p.getDeltaMovement();
            p.setDeltaMovement(new net.minecraft.world.phys.Vec3(dm.x, 0.42, dm.z));
        }
    }
}
