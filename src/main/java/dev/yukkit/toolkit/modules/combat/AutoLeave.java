package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Leaves the server (or runs a command) when your health gets low or another player comes close. */
public final class AutoLeave extends Module {
    private final Setting.Num health = num("Health", 6, 1, 20, 0.5);
    private final Setting.Bool nearbyPlayer = bool("Player Nearby", false);
    private final Setting.Num nearbyRange = num("Nearby Range", 12, 3, 64, 1);
    private final Setting.Choice action = choice("Action", "Disconnect", "Disconnect", "/hub", "/spawn", "/home");
    private final Setting.Bool disableAfter = bool("Disable After", true);

    public AutoLeave() {
        super("AutoLeave", "Escapes when health is low or a player approaches.", Category.COMBAT);
    }

    @Override public String tag() { return action.get(); }

    @Override public void onTick() {
        if (!hasWorld() || mc.player.isSpectator() || mc.player.isCreative()) return;
        boolean trigger = mc.player.getHealth() <= health.get() && mc.player.getHealth() > 0f;
        if (!trigger && nearbyPlayer.get()) {
            double r2 = nearbyRange.get() * nearbyRange.get();
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof Player other && other != mc.player && !other.isSpectator()
                        && other.distanceToSqr(mc.player) <= r2) {
                    trigger = true;
                    break;
                }
            }
        }
        if (!trigger) return;

        if (action.is("Disconnect")) {
            mc.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
        } else if (mc.getConnection() != null) {
            mc.getConnection().sendCommand(action.get().substring(1));
        }
        if (disableAfter.get()) setEnabled(false);
    }
}
