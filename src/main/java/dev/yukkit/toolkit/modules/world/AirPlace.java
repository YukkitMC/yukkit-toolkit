package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Lets you place a block in thin air at the spot your crosshair points to. */
public final class AirPlace extends Module {
    private final Setting.Num distance = num("Distance", 3.5, 1.0, 5.0, 0.5);
    private final Setting.Num delay = num("Delay", 4, 1, 10, 1);

    private int cooldown;

    public AirPlace() { super("AirPlace", "Allows you to place blocks in mid-air.", Category.WORLD); }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (cooldown > 0) cooldown--;
        if (!inGame() || !UtilBuild.ready() || cooldown > 0 || !mc.options.keyUse.isDown()) return;
        if (mc.player.isUsingItem()) return;
        // Only act when vanilla has nothing to click on.
        if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS) return;
        InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof BlockItem ? InteractionHand.MAIN_HAND
                : mc.player.getOffhandItem().getItem() instanceof BlockItem ? InteractionHand.OFF_HAND : null;
        if (hand == null) return;

        Vec3 eye = mc.player.getEyePosition();
        double dist = Math.min(distance.get(), UtilBuild.reach());
        Vec3 spot = eye.add(mc.player.getLookAngle().scale(dist));
        BlockPos pos = BlockPos.containing(spot);
        if (!UtilBuild.isReplaceable(pos)) return;
        int slot = hand == InteractionHand.MAIN_HAND ? mc.player.getInventory().getSelectedSlot() : UtilBuild.OFFHAND;
        if (UtilBuild.click(new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos.immutable(), false), slot)) {
            cooldown = delay.asInt();
        }
    }

    @Override public String tag() { return distance.display(); }
}
