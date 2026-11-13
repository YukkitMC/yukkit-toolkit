package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Places end crystals next to an enemy and detonates them, weighing damage dealt against damage taken. */
public final class CrystalAura extends Module {
    private final Setting.Num targetRange = num("Target Range", 10, 4, 20, 1);
    private final Setting.Num placeRange = num("Place Range", 4.5, 2.0, 5.5, 0.1);
    private final Setting.Num breakRange = num("Break Range", 4.5, 2.0, 5.5, 0.1);
    private final Setting.Num minDamage = num("Min Damage", 5, 1, 20, 0.5);
    private final Setting.Num maxSelf = num("Max Self Damage", 8, 1, 20, 0.5);
    private final Setting.Num delay = num("Delay", 2, 1, 10, 1);
    private final Setting.Bool place = bool("Place", true);
    private final Setting.Bool breakCrystals = bool("Break", true);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", false);
    private final Setting.Bool autoSwitch = bool("Switch To Crystal", true);

    private int cooldown;
    private int previousSlot = -1;

    public CrystalAura() {
        super("CrystalAura", "Places and detonates end crystals around enemies.", Category.COMBAT);
    }

    @Override public void onEnable() { cooldown = 0; previousSlot = -1; }

    @Override public void onDisable() {
        if (previousSlot >= 0 && hasWorld()) mc.player.getInventory().setSelectedSlot(previousSlot);
        previousSlot = -1;
    }

    @Override public void onWorldChange() { previousSlot = -1; }

    /** Worst-case (no armor, full exposure) explosion damage of a power-6 crystal at the given distance. */
    private static double blastDamage(Vec3 center, Entity e) {
        double dist = e.position().distanceTo(center);
        if (dist >= 12.0) return 0.0;
        double impact = 1.0 - dist / 12.0;
        return (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;
    }

    private double selfDamage(Vec3 center) {
        return blastDamage(center, mc.player) * 0.6; // rough allowance for armor
    }

    @Override public void onTick() {
        if (cooldown > 0) { cooldown--; return; }
        if (!inGame() || mc.gameMode == null) return;
        Player p = mc.player;
        if (p.isSpectator() || p.isDeadOrDying()) return;

        List<LivingEntity> targets = UtilCombat.find(new UtilCombat.Filter(players.get(), mobs.get(), false,
                false, false, true, targetRange.get(), 360));
        if (targets.isEmpty()) return;
        targets.sort(UtilCombat.priority("Distance"));
        LivingEntity target = targets.get(0);
        double health = p.getHealth() + p.getAbsorptionAmount();

        if (breakCrystals.get() && breakOne(p, target, health)) { cooldown = delay.asInt(); return; }
        if (place.get() && placeOne(p, target, health)) cooldown = delay.asInt();
    }

    private boolean breakOne(Player p, LivingEntity target, double health) {
        EndCrystal best = null;
        double bestScore = -Double.MAX_VALUE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof EndCrystal c) || c.isRemoved()) continue;
            if (UtilCombat.distance(c) > breakRange.get()) continue;
            Vec3 center = c.position();
            double self = selfDamage(center);
            if (self > maxSelf.get() || self >= health - 1.0) continue;
            double dealt = blastDamage(center, target);
            if (dealt < minDamage.get()) continue;
            double score = dealt - self;
            if (score > bestScore) { bestScore = score; best = c; }
        }
        if (best == null) return false;
        UtilCombat.attack(best);
        return true;
    }

    private boolean placeOne(Player p, LivingEntity target, double health) {
        InteractionHand hand = null;
        if (p.getOffhandItem().is(Items.END_CRYSTAL)) hand = InteractionHand.OFF_HAND;
        else if (p.getMainHandItem().is(Items.END_CRYSTAL)) hand = InteractionHand.MAIN_HAND;
        else if (autoSwitch.get()) {
            int slot = UtilWeapon.findHotbar(s -> s.is(Items.END_CRYSTAL));
            if (slot >= 0) {
                if (previousSlot < 0) previousSlot = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(slot);
                hand = InteractionHand.MAIN_HAND;
            }
        }
        if (hand == null) return false;

        Vec3 eye = p.getEyePosition();
        int r = (int) Math.ceil(placeRange.get());
        BlockPos origin = p.blockPosition();
        BlockPos bestPos = null;
        double bestScore = -Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos base = origin.offset(dx, dy, dz);
                    var state = mc.level.getBlockState(base);
                    if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) continue;
                    Vec3 face = new Vec3(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
                    if (face.distanceTo(eye) > placeRange.get()) continue;
                    BlockPos above = base.above();
                    if (!mc.level.isEmptyBlock(above)) continue;
                    AABB space = new AABB(above.getX(), above.getY(), above.getZ(),
                            above.getX() + 1.0, above.getY() + 2.0, above.getZ() + 1.0);
                    if (!mc.level.getEntities((Entity) null, space).isEmpty()) continue;

                    Vec3 center = new Vec3(face.x, face.y, face.z);
                    double self = selfDamage(center);
                    if (self > maxSelf.get() || self >= health - 1.0) continue;
                    double dealt = blastDamage(center, target);
                    if (dealt < minDamage.get()) continue;
                    double score = dealt - self;
                    if (score > bestScore) { bestScore = score; bestPos = base; }
                }
            }
        }
        if (bestPos == null) return false;
        Vec3 hit = new Vec3(bestPos.getX() + 0.5, bestPos.getY() + 1.0, bestPos.getZ() + 0.5);
        mc.gameMode.useItemOn(p, hand, new BlockHitResult(hit, Direction.UP, bestPos.immutable(), false));
        p.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return true;
    }
}
