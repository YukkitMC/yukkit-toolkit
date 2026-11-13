package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes your head spin, nod or twitch. By default only the rotation other players see is
 * changed (sent to the server); your own camera stays put. Enable "Camera" to turn your view too.
 */
public final class Derp extends Module {
    private final Setting.Choice mode = choice("Mode", "Spin", "Spin", "Random", "Nod");
    private final Setting.Num speed = num("Speed", 25, 1, 90, 1);
    private final Setting.Bool camera = bool("Camera", false);

    private float angle;
    private int tick;

    public Derp() {
        super("Derp", "Spins or twitches your head for everyone else to see.", Category.FUN);
    }

    @Override
    public void onEnable() { angle = 0f; tick = 0; }

    @Override
    public void onDisable() {
        Player player = mc.player;
        if (player == null || mc.getConnection() == null || camera.get()) return;
        ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
    }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.getConnection() == null) return;
        Player player = mc.player;
        float baseYaw = player.getYRot();
        float basePitch = player.getXRot();
        float yaw = baseYaw;
        float pitch = basePitch;
        tick++;
        angle = Mth.wrapDegrees(angle + speed.asFloat());

        if (mode.is("Spin")) {
            yaw = baseYaw + (camera.get() ? speed.asFloat() : angle);
        } else if (mode.is("Random")) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            yaw = rng.nextFloat() * 360f - 180f;
            pitch = rng.nextFloat() * 180f - 90f;
        } else {
            pitch = Mth.sin(tick * speed.asFloat() * 0.0349f) * 90f;
        }
        pitch = Mth.clamp(pitch, -90f, 90f);

        if (camera.get()) {
            player.setYRot(yaw);
            player.setXRot(pitch);
        } else {
            ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), player.horizontalCollision));
        }
    }

    @Override
    public String tag() { return mode.get(); }
}
