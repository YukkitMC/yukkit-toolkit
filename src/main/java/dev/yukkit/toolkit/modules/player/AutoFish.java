package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;

public final class AutoFish extends Module {
    private final Setting.Bool autoCast = bool("AutoCast", true);
    private final Setting.Num recast = num("RecastDelay", 15, 5, 60, 1);
    private final Setting.Num reelDelay = num("ReelDelay", 2, 0, 10, 1);
    private final Setting.Num sensitivity = num("Sensitivity", 0.12, 0.05, 0.4, 0.01);

    private int wait;
    private int reelIn = -1;
    private double lastY = Double.NaN;

    public AutoFish() {
        super("AutoFish", "Reels in when the bobber dips and casts again.", Category.PLAYER);
    }

    @Override public void onEnable() { wait = 20; reelIn = -1; lastY = Double.NaN; }
    @Override public void onWorldChange() { wait = 20; reelIn = -1; lastY = Double.NaN; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gameMode == null || mc.gui.screen() != null) return;
        InteractionHand hand;
        if (mc.player.getMainHandItem().is(Items.FISHING_ROD)) hand = InteractionHand.MAIN_HAND;
        else if (mc.player.getOffhandItem().is(Items.FISHING_ROD)) hand = InteractionHand.OFF_HAND;
        else { lastY = Double.NaN; return; }

        FishingHook hook = findHook();
        if (reelIn >= 0) {
            if (reelIn-- == 0) {
                mc.gameMode.useItem(mc.player, hand);
                wait = recast.asInt();
                lastY = Double.NaN;
            }
            return;
        }
        if (wait > 0) { wait--; return; }

        if (hook == null) {
            lastY = Double.NaN;
            if (autoCast.get()) {
                mc.gameMode.useItem(mc.player, hand);
                wait = recast.asInt();
            }
            return;
        }

        boolean dip = false;
        if (hook.getHookedIn() == null && hook.isInWater() && hook.tickCount > 20) {
            double vy = hook.getDeltaMovement().y;
            double drop = Double.isNaN(lastY) ? 0.0 : lastY - hook.getY();
            dip = vy < -sensitivity.get() || drop > sensitivity.get();
        }
        lastY = hook.getY();
        if (dip) reelIn = reelDelay.asInt();
    }

    private FishingHook findHook() {
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof FishingHook h && h.getPlayerOwner() == mc.player) return h;
        }
        return null;
    }
}
