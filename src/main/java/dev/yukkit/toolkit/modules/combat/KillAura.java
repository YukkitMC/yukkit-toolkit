package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Automatically attacks nearby entities, with cooldown-aware timing and optional smooth aiming. */
public final class KillAura extends Module {
    private final Setting.Num range = num("Range", 3.0, 1.0, 6.0, 0.1);
    private final Setting.Num aimRange = num("Aim Range", 4.5, 1.0, 8.0, 0.1);
    private final Setting.Num fov = num("FOV", 360, 30, 360, 5);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", true);
    private final Setting.Bool animals = bool("Animals", false);
    private final Setting.Bool invisible = bool("Invisible", false);
    private final Setting.Bool teammates = bool("Teammates", false);
    private final Setting.Bool walls = bool("Through Walls", false);
    private final Setting.Choice priority = choice("Priority", "Distance", "Distance", "Health", "Angle");
    private final Setting.Choice mode = choice("Mode", "Single", "Single", "Switch");
    private final Setting.Num switchHits = num("Switch Hits", 1, 1, 6, 1);
    private final Setting.Choice timing = choice("Timing", "Cooldown", "Cooldown", "CPS");
    private final Setting.Num cooldown = num("Cooldown", 0.92, 0.5, 1.0, 0.01);
    private final Setting.Num minCps = num("Min CPS", 8, 1, 20, 1);
    private final Setting.Num maxCps = num("Max CPS", 12, 1, 20, 1);
    private final Setting.Bool critTiming = bool("Crit Timing", false);
    private final Setting.Bool rotate = bool("Rotate", true);
    private final Setting.Num rotateSpeed = num("Rotate Speed", 45, 5, 180, 5);
    private final Setting.Bool onlyWeapon = bool("Only Weapon", false);
    private final Setting.Bool autoWeapon = bool("Auto Weapon", false);
    private final Setting.Bool autoBlock = bool("Auto Block", false);
    private final Setting.Bool render = bool("Render", true);

    private final Random random = new Random();
    private LivingEntity target;
    private int hitsOnTarget;
    private int switchIndex;
    private double clickTimer;
    private boolean blocking;

    public KillAura() {
        super("KillAura", "Attacks nearby targets automatically.", Category.COMBAT);
    }

    @Override public void onEnable() {
        reset();
    }

    @Override public void onDisable() {
        stopBlocking();
        reset();
    }

    @Override public void onWorldChange() {
        blocking = false;
        reset();
    }

    private void reset() {
        target = null;
        hitsOnTarget = 0;
        switchIndex = 0;
        clickTimer = 0;
    }

    @Override public String tag() { return mode.get(); }

    /** The entity currently being attacked, or null. */
    public LivingEntity currentTarget() { return target; }

    @Override public void onTick() {
        if (!hasWorld() || mc.gameMode == null || mc.gui.screen() != null) { target = null; return; }
        Player p = mc.player;
        if (p.isSpectator() || p.isDeadOrDying()) { target = null; return; }
        if (clickTimer > 0) clickTimer -= 1.0;

        double reach = range.get();
        double aim = Math.max(aimRange.get(), reach);
        List<LivingEntity> near = UtilCombat.find(new UtilCombat.Filter(players.get(), mobs.get(), animals.get(),
                invisible.get(), teammates.get(), walls.get(), aim, fov.get()));
        near.sort(UtilCombat.priority(priority.get()));

        pickTarget(near, reach);
        if (target == null) { stopBlocking(); return; }

        if (onlyWeapon.get() && !UtilWeapon.isMelee(p.getMainHandItem())) {
            if (!autoWeapon.get()) { stopBlocking(); return; }
        }

        Vec3 eye = p.getEyePosition();
        float[] rot = UtilCombat.rotationTo(eye, UtilCombat.aimPoint(target, eye));
        if (rotate.get()) {
            float speed = rotateSpeed.asFloat() * (0.85f + random.nextFloat() * 0.3f);
            UtilCombat.rotateToward(rot[0], rot[1], speed);
        }

        if (UtilCombat.distance(target) > reach) return;
        if (!ready(p)) { updateBlock(p, true); return; }

        if (autoWeapon.get()) UtilWeapon.selectBest(false);
        if (onlyWeapon.get() && !UtilWeapon.isMelee(p.getMainHandItem())) return;

        // A raised shield is lowered for the swing and raised again on the next tick.
        if (blocking) stopBlocking();
        UtilCombat.attack(target);
        hitsOnTarget++;
        scheduleNext();
    }

    private void pickTarget(List<LivingEntity> near, double reach) {
        if (near.isEmpty()) { target = null; hitsOnTarget = 0; return; }
        boolean keep = mode.is("Single") && target != null && near.contains(target);
        if (keep) return;
        if (mode.is("Switch")) {
            // Only cycle among targets that can actually be hit right now.
            List<LivingEntity> hittable = new ArrayList<>();
            for (LivingEntity e : near) if (UtilCombat.distance(e) <= reach) hittable.add(e);
            List<LivingEntity> pool = hittable.isEmpty() ? near : hittable;
            if (target != null && pool.contains(target) && hitsOnTarget < switchHits.asInt()) return;
            switchIndex = (target != null && pool.contains(target)) ? pool.indexOf(target) + 1 : 0;
            target = pool.get(switchIndex % pool.size());
            hitsOnTarget = 0;
            return;
        }
        target = near.get(0);
        hitsOnTarget = 0;
    }

    private boolean ready(Player p) {
        if (p.isUsingItem() && !blocking && !UtilCombat.isShield(p.getUseItem())) return false;
        if (critTiming.get() && !p.onGround() && !p.isInWater() && !p.onClimbable()
                && p.getDeltaMovement().y > 0.0 && p.fallDistance <= 0.0) return false;
        if (timing.is("Cooldown")) return p.getAttackStrengthScale(0.0f) >= cooldown.asFloat();
        return clickTimer <= 0;
    }

    private void scheduleNext() {
        double lo = Math.min(minCps.get(), maxCps.get());
        double hi = Math.max(minCps.get(), maxCps.get());
        double cps = lo + random.nextDouble() * (hi - lo);
        clickTimer = 20.0 / Math.max(1.0, cps);
    }

    // ---------------------------------------------------------- auto block
    private void updateBlock(Player p, boolean hasTarget) {
        if (!autoBlock.get() || !hasTarget || blocking || p.isUsingItem()) return;
        InteractionHand hand = null;
        if (UtilCombat.isShield(p.getOffhandItem())) hand = InteractionHand.OFF_HAND;
        else if (UtilCombat.isShield(p.getMainHandItem())) hand = InteractionHand.MAIN_HAND;
        if (hand == null) return;
        mc.gameMode.useItem(p, hand);
        blocking = p.isUsingItem();
    }

    private void stopBlocking() {
        if (!blocking) return;
        blocking = false;
        if (hasWorld() && mc.gameMode != null && mc.player.isUsingItem()) {
            ItemStack used = mc.player.getUseItem();
            if (UtilCombat.isShield(used)) mc.gameMode.releaseUsingItem(mc.player);
        }
    }

    @Override public void onWorld() {
        if (!render.get() || target == null || !target.isAlive()) return;
        Gizmos.cuboid(target.getBoundingBox().inflate(0.05), GizmoStyle.strokeAndFill(0xFFFF4040, 1.5f, 0x30FF4040))
                .setAlwaysOnTop();
    }
}
