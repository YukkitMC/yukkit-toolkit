package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Casts a fishing rod at approaching players to push them back, then reels it in. */
public final class AutoRod extends Module {
    private final Setting.Num minRange = num("Min Range", 3.5, 2.0, 8.0, 0.5);
    private final Setting.Num maxRange = num("Max Range", 8.0, 4.0, 20.0, 0.5);
    private final Setting.Num reelDelay = num("Reel Delay", 4, 1, 20, 1);
    private final Setting.Num pause = num("Pause", 10, 0, 60, 1);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", false);
    private final Setting.Bool restoreSlot = bool("Restore Slot", true);

    private int previousSlot = -1;
    private int hookTicks = -1;
    private int cooldown;

    public AutoRod() {
        super("AutoRod", "Hooks nearby targets with a fishing rod.", Category.COMBAT);
    }

    @Override public void onEnable() { previousSlot = -1; hookTicks = -1; cooldown = 0; }

    @Override public void onDisable() { reelAndRestore(); }

    @Override public void onWorldChange() { previousSlot = -1; hookTicks = -1; }

    private void reelAndRestore() {
        if (hasWorld() && mc.gameMode != null && hookTicks >= 0 && mc.player.fishing != null) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }
        restore();
        hookTicks = -1;
    }

    private void restore() {
        if (previousSlot >= 0 && hasWorld() && restoreSlot.get()) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
    }

    @Override public void onTick() {
        if (!inGame() || mc.gameMode == null || mc.player.isSpectator()) return;
        Player p = mc.player;
        if (cooldown > 0) cooldown--;

        // A hook is out: wait, then reel it back in.
        if (hookTicks >= 0) {
            if ((p.fishing == null && hookTicks > 4) || !p.getMainHandItem().is(Items.FISHING_ROD)) {
                restore();
                hookTicks = -1;
                cooldown = pause.asInt();
                return;
            }
            if (++hookTicks >= Math.max(reelDelay.asInt(), 2) && p.fishing != null) {
                mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                restore();
                hookTicks = -1;
                cooldown = pause.asInt();
            }
            return;
        }
        if (cooldown > 0 || p.isUsingItem()) return;

        int rod = UtilWeapon.findHotbar(s -> s.is(Items.FISHING_ROD));
        if (rod < 0) return;

        List<LivingEntity> list = UtilCombat.find(new UtilCombat.Filter(players.get(), mobs.get(), false,
                false, false, false, maxRange.get(), 360));
        list.removeIf(e -> UtilCombat.distance(e) < minRange.get());
        if (list.isEmpty()) return;
        list.sort(UtilCombat.priority("Distance"));
        LivingEntity target = list.get(0);

        Vec3 eye = p.getEyePosition();
        float[] rot = UtilCombat.rotationTo(eye, UtilCombat.aimPoint(target, eye));
        UtilCombat.rotateToward(rot[0], rot[1], 60f);
        if (UtilCombat.angleDiff(rot) > 6f) return;

        int cur = p.getInventory().getSelectedSlot();
        if (cur != rod) {
            previousSlot = cur;
            p.getInventory().setSelectedSlot(rod);
        }
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        hookTicks = 0;
    }
}
