package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public final class AutoWindCharge extends Module {
    private final Setting.Choice mode = choice("Mode", "Jump", "Jump", "Fall");
    private final Setting.Num fallHeight = num("FallHeight", 3.0, 1.0, 6.0, 0.5);

    private final BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
    private int cooldown;

    public AutoWindCharge() {
        super("AutoWindCharge", "Throws a wind charge at your feet to boost a jump or soften a fall.", Category.PLAYER);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!inGame() || mc.gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }
        Player p = mc.player;
        if (p.isSpectator() || p.isInWater() || p.isFallFlying()) return;

        boolean trigger;
        if (mode.is("Jump")) {
            trigger = p.onGround() && mc.options.keyJump.isDown();
        } else {
            trigger = !p.onGround() && p.getDeltaMovement().y < -0.5 && p.fallDistance > 3.0 && groundWithin(p, fallHeight.get());
        }
        if (!trigger) return;

        int slot = -1;
        if (!p.getOffhandItem().is(Items.WIND_CHARGE)) {
            slot = UtilInv.findHotbar(p, s -> s.is(Items.WIND_CHARGE));
            if (slot < 0) return;
        }
        UtilInv.useAimed(p, slot, 90f);
        cooldown = 12;
    }

    private boolean groundWithin(Player p, double height) {
        int y0 = p.blockPosition().getY();
        int steps = (int) Math.ceil(height);
        for (int i = 0; i <= steps; i++) {
            scan.set(p.getBlockX(), y0 - i, p.getBlockZ());
            if (!mc.level.getBlockState(scan).isAir()) return true;
        }
        return false;
    }

    @Override
    public String tag() { return mode.get(); }
}
