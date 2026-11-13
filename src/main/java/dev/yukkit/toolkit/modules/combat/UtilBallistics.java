package dev.yukkit.toolkit.modules.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** Simple projectile arc solver (gravity + air drag) for bows, crossbows and thrown items. */
final class UtilBallistics {
    private UtilBallistics() {}

    record Projectile(double speed, double gravity, double drag) {}

    /** Bow draw power for a number of ticks held, same curve the game uses. */
    static double bowPower(int ticks) {
        double f = ticks / 20.0;
        f = (f * f + f * 2.0) / 3.0;
        return Math.min(1.0, f);
    }

    /** @return flight parameters for the item, or null when it is not a projectile weapon we know. */
    static Projectile of(ItemStack s, int drawTicks) {
        if (s.is(Items.BOW)) return new Projectile(Math.max(0.1, bowPower(drawTicks)) * 3.0, 0.05, 0.99);
        if (s.is(Items.CROSSBOW)) return new Projectile(3.15, 0.05, 0.99);
        if (s.is(Items.TRIDENT)) return new Projectile(2.5, 0.05, 0.99);
        if (s.is(Items.SNOWBALL) || s.is(Items.EGG)) return new Projectile(1.5, 0.03, 0.99);
        return null;
    }

    /** Ticks of flight needed to cover {@code dist} blocks horizontally at the given elevation, or -1. */
    private static double flightHeight(Projectile p, double elevationDeg, double dist, double[] timeOut) {
        double e = Math.toRadians(elevationDeg);
        double vx = p.speed() * Math.cos(e), vy = p.speed() * Math.sin(e);
        double x = 0, y = 0;
        for (int t = 1; t <= 200; t++) {
            double nx = x + vx, ny = y + vy;
            if (nx >= dist) {
                double k = vx <= 1e-9 ? 0 : (dist - x) / (nx - x);
                timeOut[0] = t - 1 + k;
                return y + (ny - y) * k;
            }
            x = nx;
            y = ny;
            vx *= p.drag();
            vy = vy * p.drag() - p.gravity();
            if (y < -128) break;
        }
        timeOut[0] = -1;
        return Double.NaN;
    }

    /**
     * Finds the flattest launch angle that hits {@code to} from {@code from}.
     * @return {yaw, pitch, flightTicks} or null when the target is out of reach.
     */
    static float[] solve(Vec3 from, Vec3 to, Projectile p) {
        double dx = to.x - from.x, dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        double relY = to.y - (from.y - 0.1);
        if (dist < 0.5) return null;
        float yaw = UtilCombat.rotationTo(from, to)[0];
        double bestErr = Double.MAX_VALUE, bestElev = 0, bestTime = 0;
        double[] time = new double[1];
        for (double elev = -60; elev <= 89; elev += 0.5) {
            double h = flightHeight(p, elev, dist, time);
            if (Double.isNaN(h)) continue;
            double err = Math.abs(h - relY);
            if (err < bestErr) {
                bestErr = err;
                bestElev = elev;
                bestTime = time[0];
            }
            if (err < 0.25) break; // first (flattest) acceptable arc wins
        }
        if (bestErr > 1.5) return null;
        return new float[]{yaw, (float) -bestElev, (float) bestTime};
    }

    /** Aim solution including target lead, using velocity estimated by {@link UtilMotion}. */
    static float[] aimAt(Entity target, Vec3 eye, Projectile proj, boolean predict) {
        Vec3 point = UtilCombat.aimPoint(target, eye);
        if (!predict) return solve(eye, point, proj);
        Vec3 vel = UtilMotion.velocity(target);
        if (target.onGround()) vel = new Vec3(vel.x, 0, vel.z);
        float[] sol = solve(eye, point, proj);
        for (int i = 0; i < 3 && sol != null; i++) {
            sol = solve(eye, point.add(vel.scale(sol[2])), proj);
        }
        return sol;
    }
}
