package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Picks a projectile weapon from the hotbar, aims with arc and lead, and shoots at the nearest target. */
public final class AutoShoot extends Module {
    private final Setting.Num range = num("Range", 30, 6, 80, 1);
    private final Setting.Num speed = num("Aim Speed", 40, 5, 120, 5);
    private final Setting.Num drawTicks = num("Draw Ticks", 20, 5, 20, 1);
    private final Setting.Num delay = num("Delay", 4, 0, 40, 1);
    private final Setting.Bool bow = bool("Bow", true);
    private final Setting.Bool crossbow = bool("Crossbow", true);
    private final Setting.Bool throwables = bool("Snowballs/Eggs", true);
    private final Setting.Bool predict = bool("Predict Movement", true);
    private final Setting.Bool players = bool("Players", true);
    private final Setting.Bool mobs = bool("Mobs", false);
    private final Setting.Bool restoreSlot = bool("Restore Slot", true);

    private int cooldown;
    private int previousSlot = -1;

    public AutoShoot() {
        super("AutoShoot", "Automatically shoots projectiles at targets.", Category.COMBAT);
    }

    @Override public void onEnable() { cooldown = 0; previousSlot = -1; }

    @Override public void onDisable() { releaseIfDrawing(); restore(); }

    @Override public void onWorldChange() { previousSlot = -1; }

    private void restore() {
        if (previousSlot >= 0 && hasWorld() && restoreSlot.get()) mc.player.getInventory().setSelectedSlot(previousSlot);
        previousSlot = -1;
    }

    private void releaseIfDrawing() {
        if (hasWorld() && mc.gameMode != null && mc.player.isUsingItem() && mc.player.getUseItem().is(Items.BOW)) {
            mc.gameMode.releaseUsingItem(mc.player);
        }
    }

    private int pickSlot() {
        return UtilWeapon.findHotbar(s -> (bow.get() && s.is(Items.BOW))
                || (crossbow.get() && s.is(Items.CROSSBOW))
                || (throwables.get() && (s.is(Items.SNOWBALL) || s.is(Items.EGG))));
    }

    @Override public void onTick() {
        if (!inGame() || mc.gameMode == null || mc.player.isSpectator()) return;
        UtilMotion.update();
        Player p = mc.player;
        if (cooldown > 0) cooldown--;

        // Continue a draw that is already in progress.
        ItemStack using = p.isUsingItem() ? p.getUseItem() : ItemStack.EMPTY;
        boolean drawing = using.is(Items.BOW) || using.is(Items.CROSSBOW);
        if (p.isUsingItem() && !drawing) return; // eating, blocking, etc.

        List<LivingEntity> list = UtilCombat.find(new UtilCombat.Filter(players.get(), mobs.get(), false,
                false, false, false, range.get(), 360));
        if (list.isEmpty()) {
            if (!drawing) restore(); // keep an already drawn bow ready for the next target
            return;
        }
        list.sort(UtilCombat.priority("Distance"));

        ItemStack held = p.getMainHandItem();
        if (!drawing) {
            if (cooldown > 0) return;
            int slot = pickSlot();
            if (slot < 0) return;
            int cur = p.getInventory().getSelectedSlot();
            if (slot != cur) {
                if (previousSlot < 0) previousSlot = cur;
                p.getInventory().setSelectedSlot(slot);
                held = p.getInventory().getItem(slot);
            }
        }

        int ticks = drawing ? p.getTicksUsingItem() : (held.is(Items.BOW) ? drawTicks.asInt() : 0);
        UtilBallistics.Projectile proj = UtilBallistics.of(drawing ? using : held,
                held.is(Items.BOW) || using.is(Items.BOW) ? Math.max(ticks, drawTicks.asInt()) : 0);
        if (proj == null) return;

        Vec3 eye = p.getEyePosition();
        float[] sol = null;
        for (LivingEntity t : list) {
            sol = UtilBallistics.aimAt(t, eye, proj, predict.get());
            if (sol != null) break;
        }
        if (sol == null) return;
        UtilCombat.rotateToward(sol[0], sol[1], speed.asFloat());
        boolean aimed = UtilCombat.angleDiff(sol) < 4f;

        if (drawing) {
            if (using.is(Items.BOW)) {
                if (p.getTicksUsingItem() >= drawTicks.asInt() && aimed) {
                    mc.gameMode.releaseUsingItem(p);
                    cooldown = delay.asInt();
                }
            }
            return; // a crossbow finishes loading on its own
        }
        if (held.is(Items.CROSSBOW)) {
            if (CrossbowItem.isCharged(held)) {
                if (aimed) { mc.gameMode.useItem(p, InteractionHand.MAIN_HAND); cooldown = delay.asInt(); }
            } else {
                mc.gameMode.useItem(p, InteractionHand.MAIN_HAND); // start loading
            }
        } else if (held.is(Items.BOW)) {
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND); // start drawing
        } else if (aimed) {
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            cooldown = Math.max(delay.asInt(), 4);
        }
    }
}
