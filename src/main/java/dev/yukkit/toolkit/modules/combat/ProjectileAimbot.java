package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** While drawing a bow, holding a charged crossbow or a throwable, aims with arc and target lead. */
public final class ProjectileAimbot extends Module {
    private final Setting.Num range = num("Range", 40, 8, 100, 1);
    private final Setting.Num fov = num("FOV", 120, 10, 360, 5);
    private final Setting.Num speed = num("Speed", 25, 2, 90, 1);
    private final Setting.Bool predict = bool("Predict Movement", true);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", false);
    private final Setting.Bool animals = bool("Animals", false);
    private final Setting.Bool walls = bool("Through Walls", false);
    private final Setting.Choice priority = choice("Priority", "Angle", "Angle", "Distance", "Health");

    public ProjectileAimbot() {
        super("ProjectileAimbot", "Aims bows, crossbows and throwables with drop and lead.", Category.COMBAT);
    }

    /** The stack whose projectile will fly next, or null when the player is not shooting. */
    static UtilBallistics.Projectile active(boolean requireUse) {
        var p = mc.player;
        ItemStack main = p.getMainHandItem();
        if (p.isUsingItem()) {
            ItemStack used = p.getUseItem();
            return UtilBallistics.of(used, p.getTicksUsingItem());
        }
        if (requireUse) return null;
        if (main.is(net.minecraft.world.item.Items.SNOWBALL) || main.is(net.minecraft.world.item.Items.EGG)) {
            return UtilBallistics.of(main, 0);
        }
        if (main.is(net.minecraft.world.item.Items.CROSSBOW) && CrossbowItem.isCharged(main)) {
            return UtilBallistics.of(main, 0);
        }
        return null;
    }

    @Override public void onTick() {
        if (!inGame() || mc.player.isSpectator()) return;
        UtilMotion.update();
        UtilBallistics.Projectile proj = active(false);
        if (proj == null) return;

        List<LivingEntity> list = UtilCombat.find(new UtilCombat.Filter(players.get(), mobs.get(), animals.get(),
                false, false, walls.get(), range.get(), fov.get()));
        if (list.isEmpty()) return;
        list.sort(UtilCombat.priority(priority.get()));
        Vec3 eye = mc.player.getEyePosition();
        for (LivingEntity t : list) {
            float[] sol = UtilBallistics.aimAt(t, eye, proj, predict.get());
            if (sol == null) continue;
            UtilCombat.rotateToward(sol[0], sol[1], speed.asFloat());
            return;
        }
    }
}
