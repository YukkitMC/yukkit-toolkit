package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class AntiVoid extends Module {
    private final Setting.Choice mode = choice("Mode", "Teleport", "Teleport", "Bounce");
    private final Setting.Num distance = num("FallDistance", 4.0, 2.0, 20.0, 0.5);

    private final BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
    private Vec3 lastSafe;
    private int cooldown;

    public AntiVoid() {
        super("AntiVoid", "Rescues you when you fall with nothing but void underneath.", Category.PLAYER);
    }

    @Override public void onEnable() { lastSafe = null; cooldown = 0; }
    @Override public void onWorldChange() { lastSafe = null; cooldown = 0; }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        Player p = mc.player;
        if (cooldown > 0) cooldown--;
        if (p.isSpectator() || p.isPassenger() || p.isFallFlying() || p.getAbilities().flying) return;

        if (p.onGround()) {
            BlockPos below = p.blockPosition().below();
            if (!mc.level.getBlockState(below).isAir()) lastSafe = p.position();
            return;
        }
        if (cooldown > 0 || p.fallDistance <= distance.get() || p.getDeltaMovement().y >= 0) return;
        if (!voidBelow(p)) return;

        cooldown = 20;
        if (mode.is("Bounce") || lastSafe == null) {
            Vec3 m = p.getDeltaMovement();
            p.setDeltaMovement(new Vec3(m.x * 0.5, 0.9, m.z * 0.5));
            p.fallDistance = 0;
        } else {
            p.setPos(lastSafe.x, lastSafe.y, lastSafe.z);
            p.setDeltaMovement(Vec3.ZERO);
            p.fallDistance = 0;
        }
    }

    /** True when the columns under the player's feet contain nothing down to the bottom of the world. */
    private boolean voidBelow(Player p) {
        int minY = mc.level.getMinY();
        double[] offsets = {0.0, 0.3, -0.3};
        for (double ox : offsets) {
            for (double oz : offsets) {
                if (!columnEmpty(Math.floor(p.getX() + ox), Math.floor(p.getZ() + oz), p.blockPosition().getY(), minY)) return false;
            }
        }
        return true;
    }

    private boolean columnEmpty(double x, double z, int fromY, int minY) {
        for (int y = fromY; y >= minY; y--) {
            scan.set((int) x, y, (int) z);
            if (!mc.level.getBlockState(scan).isAir()) return false;
        }
        return true;
    }

    @Override
    public String tag() { return mode.get(); }
}
