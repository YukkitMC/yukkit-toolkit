package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Throws an ender pearl automatically: back to the last safe ground when you fall into the void,
 * and/or away from nearby players when your health is low.
 */
public final class AutoPearl extends Module {
    private final Setting.Choice trigger = choice("Trigger", "Void", "Void", "Low Health", "Both");
    private final Setting.Num fallDistance = num("Void Fall Distance", 6.0, 3.0, 30.0, 1.0);
    private final Setting.Num health = num("Health", 6.0, 1.0, 19.0, 1.0);
    private final Setting.Num enemyRange = num("Enemy Range", 8.0, 3.0, 20.0, 1.0);

    private Vec3 lastSafe;
    private int cooldown;

    public AutoPearl() {
        super("AutoPearl", "Throws an ender pearl to escape the void or a fight.", Category.MISC);
    }

    @Override
    public void onEnable() { lastSafe = null; cooldown = 0; }

    @Override
    public void onWorldChange() { lastSafe = null; cooldown = 20; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gameMode == null) return;
        Player player = mc.player;
        if (cooldown > 0) cooldown--;
        if (player.isSpectator() || player.isDeadOrDying()) return;
        if (player.onGround() && !player.isInWater()) lastSafe = player.position();
        if (cooldown > 0 || !UtilPearl.hasPearl()) return;

        if ((trigger.is("Void") || trigger.is("Both")) && fallingIntoVoid(player) && lastSafe != null) {
            float[] aim = UtilPearl.solve(lastSafe);
            if (aim != null && UtilPearl.throwPearl(aim[0], aim[1])) {
                cooldown = 60;
                UtilChat.info("AutoPearl: pearl thrown back to safety.");
                return;
            }
        }
        if (trigger.is("Low Health") || trigger.is("Both")) {
            Player threat = nearestThreat(player);
            if (threat != null && player.getHealth() <= health.get()) {
                Vec3 away = player.position().subtract(threat.position());
                Vec3 flat = new Vec3(away.x, 0.0, away.z);
                if (flat.lengthSqr() < 1.0E-4) flat = new Vec3(1.0, 0.0, 0.0);
                Vec3 target = player.position().add(flat.normalize().scale(22.0));
                float[] aim = UtilPearl.solve(target);
                float yaw = aim != null ? aim[0] : UtilPearl.yawTo(player.position(), target);
                float pitch = aim != null ? aim[1] : -35f;
                if (UtilPearl.throwPearl(yaw, pitch)) {
                    cooldown = 100;
                    UtilChat.info("AutoPearl: escaping with a pearl.");
                }
            }
        }
    }

    private boolean fallingIntoVoid(Player player) {
        if (player.onGround() || player.isFallFlying() || player.getDeltaMovement().y > -0.3) return false;
        if (player.fallDistance < fallDistance.get()) return false;
        Vec3 from = player.position();
        Vec3 to = new Vec3(from.x, from.y - 160.0, from.z);
        BlockHitResult hit = mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit == null || hit.getType() == HitResult.Type.MISS;
    }

    private Player nearestThreat(Player self) {
        double rangeSq = enemyRange.get() * enemyRange.get();
        Player best = null;
        double bestDist = rangeSq;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Player other) || other == self) continue;
            if (AntiBot.isBot(other) || Teams.isTeammate(other) || UtilFriends.isFriend(other)) continue;
            if (other.isSpectator()) continue;
            double d = self.distanceToSqr(other);
            if (d <= bestDist) {
                bestDist = d;
                best = other;
            }
        }
        return best;
    }

    @Override
    public String tag() { return trigger.get(); }
}
