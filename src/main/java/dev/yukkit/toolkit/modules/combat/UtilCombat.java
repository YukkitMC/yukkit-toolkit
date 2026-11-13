package dev.yukkit.toolkit.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Shared target selection, aiming and attack helpers for the combat modules. */
final class UtilCombat {
    private static final Minecraft mc = Minecraft.getInstance();

    private UtilCombat() {}

    /** Which entities are acceptable and how far / in which cone they may be. */
    record Filter(boolean players, boolean mobs, boolean animals, boolean invisible, boolean teammates,
                  boolean throughWalls, double range, double fov) {}

    static boolean isTargetable(Entity e, boolean players, boolean mobs, boolean animals,
                                boolean invisible, boolean teammates) {
        Player self = mc.player;
        if (self == null || e == self || !(e instanceof LivingEntity le)) return false;
        if (!le.isAlive() || le.isRemoved() || le.isDeadOrDying() || le.isSpectator()) return false;
        if (!invisible && le.isInvisible()) return false;
        if (le instanceof Player other) {
            if (!players) return false;
            return teammates || !self.isAlliedTo(other);
        }
        if (le instanceof Enemy) return mobs;
        if (le instanceof Mob) {
            if (!animals || le instanceof OwnableEntity) return false;
            return le.getType() != EntityType.VILLAGER;
        }
        return false;
    }

    static Vec3 closestPoint(AABB box, Vec3 p) {
        return new Vec3(Mth.clamp(p.x, box.minX, box.maxX),
                Mth.clamp(p.y, box.minY, box.maxY),
                Mth.clamp(p.z, box.minZ, box.maxZ));
    }

    /** Distance from the player's eyes to the closest point of the entity's hitbox. */
    static double distance(Entity e) {
        Vec3 eye = mc.player.getEyePosition();
        return closestPoint(e.getBoundingBox(), eye).distanceTo(eye);
    }

    /** A point on the entity's body, level with the eyes when possible so the aim looks natural. */
    static Vec3 aimPoint(Entity e, Vec3 eye) {
        AABB b = e.getBoundingBox();
        double h = b.maxY - b.minY;
        double y = Mth.clamp(eye.y, b.minY + h * 0.15, b.maxY - h * 0.1);
        return new Vec3((b.minX + b.maxX) * 0.5, y, (b.minZ + b.maxZ) * 0.5);
    }

    /** @return {yaw, pitch} looking from {@code from} towards {@code to}. */
    static float[] rotationTo(Vec3 from, Vec3 to) {
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
        return new float[]{Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90f, 90f)};
    }

    /** Combined angular distance between the current view and the given rotation. */
    static float angleDiff(float[] rot) {
        float dy = Mth.wrapDegrees(rot[0] - mc.player.getYRot());
        float dp = rot[1] - mc.player.getXRot();
        return (float) Math.hypot(dy, dp);
    }

    static float yawDiff(float[] rot) {
        return Math.abs(Mth.wrapDegrees(rot[0] - mc.player.getYRot()));
    }

    /** Turns the view towards the rotation by at most {@code maxStep} degrees. */
    static void rotateToward(float yaw, float pitch, float maxStep) {
        Player p = mc.player;
        if (p == null) return;
        float dy = Mth.wrapDegrees(yaw - p.getYRot());
        float dp = pitch - p.getXRot();
        float dist = (float) Math.hypot(dy, dp);
        if (dist < 0.01f) return;
        float k = dist <= maxStep ? 1f : maxStep / dist;
        p.setYRot(p.getYRot() + dy * k);
        p.setXRot(Mth.clamp(p.getXRot() + dp * k, -90f, 90f));
    }

    static List<LivingEntity> find(Filter f) {
        List<LivingEntity> out = new ArrayList<>();
        if (mc.player == null || mc.level == null) return out;
        Vec3 eye = mc.player.getEyePosition();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!isTargetable(e, f.players(), f.mobs(), f.animals(), f.invisible(), f.teammates())) continue;
            LivingEntity le = (LivingEntity) e;
            if (distance(le) > f.range()) continue;
            if (!f.throughWalls() && !mc.player.hasLineOfSight(le)) continue;
            if (f.fov() < 359.0 && yawDiff(rotationTo(eye, aimPoint(le, eye))) > f.fov() * 0.5) continue;
            out.add(le);
        }
        return out;
    }

    static Comparator<LivingEntity> priority(String mode) {
        Vec3 eye = mc.player.getEyePosition();
        return switch (mode) {
            case "Health" -> Comparator.comparingDouble(e -> e.getHealth() + e.getAbsorptionAmount());
            case "Angle" -> Comparator.comparingDouble(e -> angleDiff(rotationTo(eye, aimPoint(e, eye))));
            default -> Comparator.comparingDouble(UtilCombat::distance);
        };
    }

    static void attack(Entity target) {
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
    }

    static boolean isShield(net.minecraft.world.item.ItemStack s) {
        return s.is(net.minecraft.world.item.Items.SHIELD);
    }
}
