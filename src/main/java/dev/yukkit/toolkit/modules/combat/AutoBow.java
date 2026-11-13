package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.item.Items;

/** Releases the bow automatically once it has been drawn far enough, so you can just hold use. */
public final class AutoBow extends Module {
    private final Setting.Num drawTicks = num("Draw Ticks", 20, 3, 20, 1);

    public AutoBow() {
        super("AutoBow", "Releases a drawn bow automatically.", Category.COMBAT);
    }

    @Override public String tag() { return drawTicks.asInt() + "t"; }

    @Override public void onTick() {
        if (!inGame() || mc.gameMode == null) return;
        var p = mc.player;
        if (p.isUsingItem() && p.getUseItem().is(Items.BOW) && p.getTicksUsingItem() >= drawTicks.asInt()) {
            mc.gameMode.releaseUsingItem(p);
        }
    }
}
