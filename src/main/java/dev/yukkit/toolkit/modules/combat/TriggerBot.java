package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Random;

/** Attacks as soon as your crosshair is on a valid target and the weapon is recharged. */
public final class TriggerBot extends Module {
    private final Setting.Num cooldown = num("Cooldown", 0.95, 0.5, 1.0, 0.01);
    private final Setting.Num extraDelay = num("Extra Delay", 0, 0, 10, 1);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", true);
    private final Setting.Bool animals = bool("Animals", false);
    private final Setting.Bool teammates = bool("Teammates", false);
    private final Setting.Bool onlyWeapon = bool("Only Weapon", false);

    private final Random random = new Random();
    private int wait;

    public TriggerBot() {
        super("TriggerBot", "Attacks whatever you are aiming at.", Category.COMBAT);
    }

    @Override public void onTick() {
        if (!inGame() || mc.gameMode == null || mc.player.isSpectator()) return;
        if (wait > 0) { wait--; return; }
        if (!(mc.hitResult instanceof EntityHitResult hit)) return;
        Entity e = hit.getEntity();
        if (!UtilCombat.isTargetable(e, players.get(), mobs.get(), animals.get(), false, teammates.get())) return;
        if (mc.player.isUsingItem()) return;
        if (onlyWeapon.get() && !UtilWeapon.isMelee(mc.player.getMainHandItem())) return;
        if (e.distanceToSqr(mc.player) > Math.pow(mc.player.entityInteractionRange() + 1.0, 2)) return;
        if (mc.player.getAttackStrengthScale(0.0f) < cooldown.asFloat()) return;
        UtilCombat.attack(e);
        int extra = extraDelay.asInt();
        wait = extra > 0 ? random.nextInt(extra + 1) : 0;
    }
}
