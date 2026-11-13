package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.component.SwingAnimation;

/** Hits fireballs that come close so they are knocked back. */
public final class ProjectilePuncher extends Module {
    private final Setting.Num range = num("Range", 3.5, 1.0, 6.0, 0.5);
    private final Setting.Num delay = num("Delay", 4, 1, 20, 1);

    private int cooldown;

    public ProjectilePuncher() {
        super("ProjectilePuncher", "Shoots back incoming projectiles around you.", Category.WORLD);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (cooldown > 0) cooldown--;
        if (!inGame() || !UtilBuild.ready() || cooldown > 0 || mc.player.isUsingItem()) return;
        double r = Math.min(range.get(), mc.player.entityInteractionRange());
        Entity best = null;
        double bestDist = r * r;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Fireball fireball) || fireball.isRemoved()) continue;
            if (fireball.getOwner() == mc.player) continue;
            double d = fireball.distanceToSqr(mc.player);
            if (d < bestDist) {
                bestDist = d;
                best = fireball;
            }
        }
        if (best == null) return;
        mc.gameMode.attack(mc.player, best);
        mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
        cooldown = delay.asInt();
    }
}
