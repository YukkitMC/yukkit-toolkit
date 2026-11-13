package dev.yukkit.toolkit.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

/** Small shared helpers for the movement modules. */
final class UtilMove {
    private static final Minecraft mc = Minecraft.getInstance();

    private UtilMove() {}

    // ------------------------------------------------------------- keys
    static boolean up() { return mc.options.keyUp.isDown(); }
    static boolean down() { return mc.options.keyDown.isDown(); }
    static boolean left() { return mc.options.keyLeft.isDown(); }
    static boolean right() { return mc.options.keyRight.isDown(); }
    static boolean jumpKey() { return mc.options.keyJump.isDown(); }
    static boolean sneakKey() { return mc.options.keyShift.isDown(); }

    static float forwardImpulse() { return (up() ? 1f : 0f) - (down() ? 1f : 0f); }
    static float strafeImpulse() { return (left() ? 1f : 0f) - (right() ? 1f : 0f); }
    static boolean moving() { return forwardImpulse() != 0f || strafeImpulse() != 0f; }

    /** Unit vector (world space) of the direction the movement keys point to, or ZERO. */
    static Vec3 inputDirection(LocalPlayer p) {
        return directionOf(p.getYRot(), forwardImpulse(), strafeImpulse());
    }

    static Vec3 directionOf(float yawDeg, float forward, float strafe) {
        if (forward == 0f && strafe == 0f) return Vec3.ZERO;
        double len = Math.sqrt(forward * forward + strafe * strafe);
        double f = forward / len;
        double s = strafe / len;
        double yaw = Math.toRadians(yawDeg);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        return new Vec3(s * cos - f * sin, 0.0, f * cos + s * sin);
    }

    // ------------------------------------------------------------- motion
    static double horizontalSpeed(Vec3 v) { return Math.sqrt(v.x * v.x + v.z * v.z); }

    /** Replaces the horizontal velocity, keeping the vertical one. */
    static void setHorizontal(LocalPlayer p, Vec3 dir, double speed) {
        Vec3 m = p.getDeltaMovement();
        p.setDeltaMovement(dir.x * speed, m.y, dir.z * speed);
    }

    static void setVertical(LocalPlayer p, double y) {
        Vec3 m = p.getDeltaMovement();
        p.setDeltaMovement(m.x, y, m.z);
    }

    /** Speed potion factor (1.0 without the effect). */
    static double speedEffectScale(LocalPlayer p) {
        MobEffectInstance e = p.getEffect(MobEffects.SPEED);
        return e == null ? 1.0 : 1.0 + 0.2 * (e.getAmplifier() + 1);
    }

    /** True when ground-style movement tweaks should not run. */
    static boolean busy(LocalPlayer p) {
        return p.isSpectator() || p.isPassenger() || p.isFallFlying();
    }

    // ------------------------------------------------------------- world
    static BlockPos pos(double x, double y, double z) {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    static BlockState state(BlockPos pos) {
        return mc.level.getBlockState(pos);
    }

    static boolean fluid(BlockPos pos, TagKey<Fluid> tag) {
        return mc.level.getBlockState(pos).getFluidState().is(tag);
    }

    /** True when the block at {@code pos} has any collision shape. */
    static boolean solid(BlockPos pos) {
        return !mc.level.getBlockState(pos).getCollisionShape(mc.level, pos).isEmpty();
    }
}
