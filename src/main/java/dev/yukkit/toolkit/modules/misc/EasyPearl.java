package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * One-shot module: throws an ender pearl so that it lands on the block under your crosshair,
 * even when that block is far beyond normal reach, then turns itself off.
 */
public final class EasyPearl extends Module {
    private final Setting.Num range = num("Range", 60, 10, 120, 5);
    private final Setting.Bool fallback = bool("Throw Straight If No Solution", false);

    private boolean pending;

    public EasyPearl() {
        super("EasyPearl", "Aims an ender pearl at the block you are looking at.", Category.MISC);
    }

    @Override
    public void onEnable() { pending = true; }

    @Override
    public void onDisable() { pending = false; }

    @Override
    public void onTick() {
        if (!pending) return;
        pending = false;
        try {
            throwOnce();
        } finally {
            setEnabled(false);
        }
    }

    private void throwOnce() {
        if (!hasWorld()) return;
        if (!UtilPearl.hasPearl()) {
            UtilChat.alert("EasyPearl: no ender pearl in the hotbar or offhand.");
            return;
        }
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = UtilPearl.direction(mc.player.getYRot(), mc.player.getXRot());
        Vec3 end = eye.add(look.scale(range.get()));
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            UtilChat.alert("EasyPearl: no block under the crosshair within range.");
            return;
        }
        float[] aim = UtilPearl.solve(hit.getLocation());
        if (aim == null) {
            if (fallback.get()) {
                UtilPearl.throwPearl(mc.player.getYRot(), mc.player.getXRot());
            } else {
                UtilChat.alert("EasyPearl: that spot is out of pearl range.");
            }
            return;
        }
        UtilPearl.throwPearl(aim[0], aim[1]);
    }
}
