package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Switches to the best hotbar weapon when you start attacking something. */
public final class AutoWeapon extends Module {
    private final Setting.Choice prefer = choice("Prefer", "Sword", "Sword", "Axe");
    private final Setting.Bool restore = bool("Restore Slot", false);
    private final Setting.Num restoreDelay = num("Restore Delay", 20, 5, 100, 5);

    private int previousSlot = -1;
    private int idle;

    public AutoWeapon() {
        super("AutoWeapon", "Selects the strongest weapon when attacking.", Category.COMBAT);
    }

    @Override public void onDisable() { previousSlot = -1; }

    @Override public void onWorldChange() { previousSlot = -1; }

    @Override public String tag() { return prefer.get(); }

    @Override public void onTick() {
        if (!inGame()) return;
        boolean attacking = mc.options.keyAttack.isDown()
                && mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity;
        var inv = mc.player.getInventory();
        if (attacking) {
            idle = 0;
            int before = inv.getSelectedSlot();
            UtilWeapon.selectBest(prefer.is("Axe"));
            if (inv.getSelectedSlot() != before && previousSlot < 0) previousSlot = before;
        } else if (previousSlot >= 0 && restore.get()) {
            if (++idle >= restoreDelay.asInt()) {
                inv.setSelectedSlot(previousSlot);
                previousSlot = -1;
            }
        } else if (!restore.get()) {
            previousSlot = -1;
        }
    }
}
