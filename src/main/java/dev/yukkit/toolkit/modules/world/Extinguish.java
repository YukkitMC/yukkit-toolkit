package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;

/** Puts out fire: breaks fire blocks next to you and splashes water at your feet when you burn. */
public final class Extinguish extends Module {
    private final Setting.Bool breakFire = bool("Break fire", true);
    private final Setting.Num fireRange = num("Fire range", 3.0, 1.0, 5.0, 1.0);
    private final Setting.Bool useWater = bool("Water bucket", true);
    private final Setting.Bool pickUp = bool("Pick water up", true);

    private boolean waitingForPickup;
    private int timer;
    private int cooldown;

    public Extinguish() {
        super("Extinguish", "Automatically extinguishes yourself when on fire.", Category.WORLD);
    }

    @Override public void onEnable() { waitingForPickup = false; cooldown = 0; timer = 0; }

    @Override public void onWorldChange() { waitingForPickup = false; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready() || mc.player.isUsingItem()) return;
        if (cooldown > 0) cooldown--;

        if (waitingForPickup) {
            if (--timer > 0) return;
            waitingForPickup = false;
            int slot = UtilBuild.findSlot(s -> s.is(Items.BUCKET));
            if (slot != UtilBuild.NONE) useBucketDownwards(slot);
            cooldown = 20;
            return;
        }

        if (breakFire.get()) {
            BlockPos fire = nearestFire();
            if (fire != null) {
                UtilBreak.hitOnce(fire);
                return;
            }
        }

        if (useWater.get() && cooldown == 0 && mc.player.isOnFire() && !mc.player.isInWaterOrRain()
                && mc.player.onGround()) {
            int slot = UtilBuild.findSlot(s -> s.is(Items.WATER_BUCKET));
            if (slot == UtilBuild.NONE) return;
            useBucketDownwards(slot);
            cooldown = 15;
            if (pickUp.get()) {
                waitingForPickup = true;
                timer = 8;
            }
        }
    }

    private BlockPos nearestFire() {
        Vec3 eye = mc.player.getEyePosition();
        int r = fireRange.asInt();
        double reach = UtilBuild.reach();
        double best = Math.min(r + 0.5, reach);
        best *= best;
        BlockPos found = null;
        BlockPos feet = mc.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    if (!(mc.level.getBlockState(cursor).getBlock() instanceof BaseFireBlock)) continue;
                    double d = eye.distanceToSqr(Vec3.atCenterOf(cursor));
                    if (d < best) {
                        best = d;
                        found = cursor.immutable();
                    }
                }
            }
        }
        return found;
    }

    /** Uses the bucket in {@code slot} while looking straight down, then restores view and hotbar. */
    private void useBucketDownwards(int slot) {
        Player player = mc.player;
        var inv = player.getInventory();
        int previousSlot = inv.getSelectedSlot();
        float previousPitch = player.getXRot();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (slot == UtilBuild.OFFHAND) hand = InteractionHand.OFF_HAND;
        else if (slot != previousSlot) inv.setSelectedSlot(slot);
        try {
            player.setXRot(90.0f);
            ModuleManager.sendDirect(new ServerboundMovePlayerPacket.Rot(
                    player.getYRot(), 90.0f, player.onGround(), player.horizontalCollision));
            mc.gameMode.useItem(player, hand);
        } finally {
            player.setXRot(previousPitch);
            if (slot >= 0 && slot != previousSlot) inv.setSelectedSlot(previousSlot);
        }
    }
}
