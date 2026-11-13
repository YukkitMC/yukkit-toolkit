package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Scales or removes the knockback the server applies to you. */
public final class Velocity extends Module {
    private final Setting.Num horizontal = num("Horizontal", 0, 0, 100, 5);
    private final Setting.Num vertical = num("Vertical", 100, 0, 100, 5);
    private final Setting.Num chance = num("Chance", 100, 0, 100, 5);
    private final Setting.Bool explosions = bool("Explosions", true);
    private final Setting.Bool onlyWhenHurt = bool("Skip In Water", true);

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback from hits and explosions.", Category.COMBAT);
    }

    @Override public String tag() {
        return horizontal.asInt() + "% / " + vertical.asInt() + "%";
    }

    private boolean active() {
        if (!hasWorld()) return false;
        if (onlyWhenHurt.get() && (mc.player.isInWater() || mc.player.isInLava())) return false;
        return ThreadLocalRandom.current().nextDouble(100.0) < chance.get();
    }

    @Override public boolean onReceivePacket(Packet<?> packet) {
        if (!hasWorld()) return false;
        if (packet instanceof ClientboundSetEntityMotionPacket motion) {
            if (motion.id() != mc.player.getId() || mc.player.isPassenger()) return false;
            double h = horizontal.get() / 100.0, v = vertical.get() / 100.0;
            if (h >= 0.999 && v >= 0.999) return false;
            if (!active()) return false;
            final Vec3 pushed = motion.movement();
            mc.execute(() -> {
                if (mc.player == null) return;
                Vec3 cur = mc.player.getDeltaMovement();
                // 0% keeps the motion the player already had instead of freezing them.
                double x = h <= 0.0 ? cur.x : pushed.x * h;
                double z = h <= 0.0 ? cur.z : pushed.z * h;
                double y = v <= 0.0 ? cur.y : pushed.y * v;
                mc.player.setDeltaMovement(new Vec3(x, y, z));
            });
            return true;
        }
        if (explosions.get() && packet instanceof ClientboundExplodePacket blast) {
            Optional<Vec3> push = blast.playerKnockback();
            if (push.isEmpty()) return false;
            double h = horizontal.get() / 100.0, v = vertical.get() / 100.0;
            if (h >= 0.999 && v >= 0.999) return false;
            if (!active()) return false;
            final Vec3 pushed = push.get();
            mc.execute(() -> {
                if (mc.player == null) return;
                Vec3 cur = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(cur.add(pushed.x * h, pushed.y * v, pushed.z * h));
            });
            return true;
        }
        return false;
    }
}
