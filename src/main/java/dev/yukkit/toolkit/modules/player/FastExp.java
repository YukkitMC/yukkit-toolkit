package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public final class FastExp extends Module {
    private final Setting.Num throwsPerTick = num("Throws", 1, 1, 4, 1);
    private final Setting.Num delay = num("Delay", 0, 0, 10, 1);

    private int wait;

    public FastExp() {
        super("FastExp", "Throws experience bottles every tick while the use key is held.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (!inGame() || mc.gameMode == null) return;
        if (wait > 0) { wait--; return; }
        if (!mc.options.keyUse.isDown()) return;
        boolean main = mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE);
        boolean off = !main && mc.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE);
        if (!main && !off) return;
        InteractionHand hand = main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        for (int i = 0; i < throwsPerTick.asInt(); i++) {
            if (!(main ? mc.player.getMainHandItem() : mc.player.getOffhandItem()).is(Items.EXPERIENCE_BOTTLE)) break;
            mc.gameMode.useItem(mc.player, hand);
        }
        wait = delay.asInt();
    }
}
