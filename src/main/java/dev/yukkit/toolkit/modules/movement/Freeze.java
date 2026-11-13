package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Freeze extends Module {
    private final Setting.Bool lockPosition = bool("Lock position", true);
    private Vec3 anchor;

    public Freeze() { super("Freeze", "Holds you completely still in place.", Category.MOVEMENT); }

    @Override public void onEnable() { anchor = mc.player == null ? null : mc.player.position(); }

    @Override public void onDisable() { anchor = null; }

    @Override public void onWorldChange() { anchor = mc.player == null ? null : mc.player.position(); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (anchor == null) anchor = p.position();
        p.setDeltaMovement(0.0, 0.0, 0.0);
        if (lockPosition.get()) p.setPos(anchor.x, anchor.y, anchor.z);
        p.fallDistance = 0;
    }
}
