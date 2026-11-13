package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Gently pulls your view towards the best target inside a cone, without attacking. */
public final class Aimbot extends Module {
    private final Setting.Num range = num("Range", 4.5, 1.0, 8.0, 0.1);
    private final Setting.Num fov = num("FOV", 90, 10, 360, 5);
    private final Setting.Num speed = num("Speed", 10, 1, 60, 1);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", false);
    private final Setting.Bool animals = bool("Animals", false);
    private final Setting.Bool walls = bool("Through Walls", false);
    private final Setting.Bool onlyOnClick = bool("Only While Attacking", true);
    private final Setting.Bool onlyWeapon = bool("Only With Weapon", true);
    private final Setting.Bool lockPitch = bool("Vertical", true);
    private final Setting.Choice priority = choice("Priority", "Angle", "Angle", "Distance", "Health");

    public Aimbot() {
        super("Aimbot", "Smoothly aims at nearby targets.", Category.COMBAT);
    }

    @Override public void onTick() {
        if (!inGame() || mc.player.isSpectator()) return;
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) return;
        if (onlyWeapon.get() && !UtilWeapon.isMelee(mc.player.getMainHandItem())) return;

        List<LivingEntity> list = UtilCombat.find(new UtilCombat.Filter(players.get(), mobs.get(), animals.get(),
                false, false, walls.get(), range.get(), fov.get()));
        if (list.isEmpty()) return;
        list.sort(UtilCombat.priority(priority.get()));
        LivingEntity t = list.get(0);

        Vec3 eye = mc.player.getEyePosition();
        float[] rot = UtilCombat.rotationTo(eye, UtilCombat.aimPoint(t, eye));
        float pitch = lockPitch.get() ? rot[1] : mc.player.getXRot();
        UtilCombat.rotateToward(rot[0], pitch, speed.asFloat());
    }
}
