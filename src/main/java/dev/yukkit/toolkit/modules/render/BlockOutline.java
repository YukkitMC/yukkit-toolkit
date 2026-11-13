package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Draws a coloured box around the block you are looking at. */
public final class BlockOutline extends Module {
    private final Setting.Choice style = choice("Style", "Both", "Outline", "Filled", "Both");
    private final Setting.Choice color = choice("Color", "Rainbow", "Rainbow", "Fixed");
    private final Setting.Num hue = num("Hue", 200, 0, 360, 10);

    public BlockOutline() { super("BlockOutline", "Coloured outline on the targeted block.", Category.RENDER); }

    @Override public void onWorld() {
        if (!hasWorld() || mc.hitResult == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        if (mc.level.getBlockState(pos).isAir()) return;
        float h = color.is("Rainbow") ? (System.currentTimeMillis() % 4000L) / 4000f * 360f : hue.asFloat();
        UtilDraw.box(new AABB(pos).inflate(0.002), UtilDraw.hue(h), style.get(), true);
    }
}
