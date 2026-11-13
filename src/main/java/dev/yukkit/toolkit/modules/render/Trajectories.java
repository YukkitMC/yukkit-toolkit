package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Predicts the flight path of held projectile items and projectiles in the air. */
public final class Trajectories extends Module {
    private final Setting.Bool held = bool("HeldItem", true);
    private final Setting.Bool inFlight = bool("InFlight", true);
    private final Setting.Num steps = num("Steps", 120, 20, 300, 10);

    private static final int HELD_COLOR = 0x55FFFF;
    private static final int FLIGHT_COLOR = 0xFFAA00;

    public Trajectories() { super("Trajectories", "Shows where arrows, pearls and throwables will land.", Category.RENDER); }

    /** {speed, gravity, pitchOffsetDegrees} for a held item, or null when it is not a projectile. */
    private double[] profile(ItemStack s) {
        if (s == null || s.isEmpty()) return null;
        if (s.is(Items.ENDER_PEARL) || s.is(Items.SNOWBALL) || s.is(Items.EGG)) return new double[]{1.5, 0.03, 0};
        if (s.is(Items.EXPERIENCE_BOTTLE)) return new double[]{0.7, 0.07, -20};
        if (s.is(Items.SPLASH_POTION)) return new double[]{0.5, 0.05, -20};
        if (s.is(Items.BOW) && mc.player.isUsingItem() && mc.player.getUseItem() == s) {
            float f = mc.player.getTicksUsingItem() / 20.0f;
            f = (f * f + f * 2.0f) / 3.0f;
            if (f < 0.1f) return null;
            if (f > 1.0f) f = 1.0f;
            return new double[]{f * 3.0, 0.05, 0};
        }
        if (s.is(Items.TRIDENT) && mc.player.isUsingItem() && mc.player.getUseItem() == s
                && mc.player.getTicksUsingItem() >= 10) return new double[]{2.5, 0.05, 0};
        return null;
    }

    private List<Vec3> simulate(Vec3 start, Vec3 velocity, double gravity, int maxSteps) {
        List<Vec3> pts = new ArrayList<>();
        pts.add(start);
        Vec3 pos = start;
        Vec3 vel = velocity;
        for (int i = 0; i < maxSteps; i++) {
            Vec3 next = pos.add(vel);
            BlockHitResult hit = mc.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, mc.player));
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                pts.add(hit.getLocation());
                return pts;
            }
            pts.add(next);
            pos = next;
            vel = vel.scale(0.99).add(0, -gravity, 0);
            if (pos.y < mc.level.getMinY() - 32) break;
        }
        return pts;
    }

    private void draw(List<Vec3> pts, int rgb) {
        for (int i = 1; i < pts.size(); i++) UtilDraw.line(pts.get(i - 1), pts.get(i), rgb, 2.0f, true);
        Vec3 end = pts.get(pts.size() - 1);
        UtilDraw.box(new AABB(end.x - 0.15, end.y - 0.15, end.z - 0.15, end.x + 0.15, end.y + 0.15, end.z + 0.15),
                rgb, "Both", true);
    }

    @Override public void onWorld() {
        if (!hasWorld()) return;
        int maxSteps = steps.asInt();
        if (held.get() && mc.gui.screen() == null) {
            ItemStack stack = mc.player.getMainHandItem();
            double[] p = profile(stack);
            if (p == null) {
                stack = mc.player.getOffhandItem();
                p = profile(stack);
            }
            if (p != null) {
                float pt = UtilDraw.partial();
                float yaw = mc.player.getYRot(), pitch = mc.player.getXRot() + (float) p[2];
                double ry = Math.toRadians(yaw), rp = Math.toRadians(pitch);
                Vec3 dir = new Vec3(-Math.sin(ry) * Math.cos(rp), -Math.sin(rp), Math.cos(ry) * Math.cos(rp));
                Vec3 pm = mc.player.getDeltaMovement();
                Vec3 vel = dir.scale(p[0]).add(pm.x, mc.player.onGround() ? 0 : pm.y, pm.z);
                Vec3 start = mc.player.getPosition(pt).add(0, mc.player.getEyeHeight() - 0.1, 0);
                draw(simulate(start, vel, p[1], maxSteps), HELD_COLOR);
            }
        }
        if (inFlight.get()) {
            int count = 0;
            for (Entity e : mc.level.entitiesForRendering()) {
                double g;
                if (e instanceof ThrownExperienceBottle) g = 0.07;
                else if (e instanceof AbstractThrownPotion) g = 0.05;
                else if (e instanceof ThrowableItemProjectile) g = 0.03;
                else if (e instanceof AbstractArrow) g = 0.05;
                else continue;
                Vec3 v = e.getDeltaMovement();
                if (v.lengthSqr() < 0.09) continue;
                if (e.distanceToSqr(mc.player) > 100.0 * 100.0) continue;
                draw(simulate(e.position(), v, g, maxSteps), FLIGHT_COLOR);
                if (++count >= 20) break;
            }
        }
    }
}
