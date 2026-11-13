package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Lets you place blocks into the liquid you are pointing at (normally the crosshair looks through liquids). */
public final class LiquidPlace extends Module {
    private final Setting.Num delay = num("Delay", 4, 1, 10, 1);

    private int cooldown;

    public LiquidPlace() { super("LiquidPlace", "Allows you to place blocks on liquids.", Category.WORLD); }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (cooldown > 0) cooldown--;
        if (!inGame() || !UtilBuild.ready() || cooldown > 0 || !mc.options.keyUse.isDown()) return;
        if (mc.player.isUsingItem()) return;
        boolean main = mc.player.getMainHandItem().getItem() instanceof BlockItem;
        boolean off = !main && mc.player.getOffhandItem().getItem() instanceof BlockItem;
        if (!main && !off) return;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(UtilBuild.reach()));
        BlockHitResult fluidHit = mc.level.clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, mc.player));
        if (fluidHit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = fluidHit.getBlockPos();
        if (mc.level.getBlockState(pos).getFluidState().isEmpty() || !UtilBuild.isReplaceable(pos)) return;
        // Leave it to vanilla when a solid block is at least as close as the liquid.
        if (mc.hitResult instanceof BlockHitResult normal && normal.getType() == HitResult.Type.BLOCK
                && eye.distanceToSqr(normal.getLocation()) <= eye.distanceToSqr(fluidHit.getLocation()) + 1.0e-6) {
            return;
        }
        int slot = main ? mc.player.getInventory().getSelectedSlot() : UtilBuild.OFFHAND;
        if (UtilBuild.click(new BlockHitResult(fluidHit.getLocation(), fluidHit.getDirection(), pos.immutable(), false), slot)) {
            cooldown = delay.asInt();
        }
    }
}
