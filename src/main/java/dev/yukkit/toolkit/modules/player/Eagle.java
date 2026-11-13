package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Eagle extends Module {
    private final Setting.Num lookahead = num("Lookahead", 2.0, 1.0, 5.0, 0.5);
    private final Setting.Bool onlyBackwards = bool("OnlyBackwards", false);
    private final Setting.Bool pitchOnly = bool("LookingDown", false);

    private boolean forced;

    public Eagle() {
        super("Eagle", "Sneaks automatically at the edge of blocks so you never walk off.", Category.PLAYER);
    }

    @Override public void onDisable() { release(); }
    @Override public void onWorldChange() { forced = false; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gui.screen() != null) { release(); return; }
        Player p = mc.player;
        boolean edge = p.onGround() && !p.isInWater() && !p.getAbilities().flying && atEdge(p);
        if (edge && onlyBackwards.get() && !mc.options.keyDown.isDown()) edge = false;
        if (edge && pitchOnly.get() && p.getXRot() < 55f) edge = false;
        if (edge) {
            if (!mc.options.keyShift.isDown()) {
                mc.options.keyShift.setDown(true);
                forced = true;
            }
        } else {
            release();
        }
    }

    private boolean atEdge(Player p) {
        Vec3 m = p.getDeltaMovement();
        double k = lookahead.get();
        double dx = m.x * k;
        double dz = m.z * k;
        if (Math.abs(dx) < 1.0E-3 && Math.abs(dz) < 1.0E-3) return false;
        AABB probe = p.getBoundingBox().inflate(-0.2, 0.0, -0.2).move(dx, -0.6, dz);
        return mc.level.noCollision(probe);
    }

    private void release() {
        if (forced) {
            mc.options.keyShift.setDown(false);
            forced = false;
        }
    }
}
