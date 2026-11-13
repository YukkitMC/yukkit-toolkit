package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public final class AntiLevitation extends Module {
    public AntiLevitation() { super("AntiLevitation", "Ignores the levitation effect and falls normally.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (p.isSpectator() || !p.hasEffect(MobEffects.LEVITATION)) return;
        Vec3 m = p.getDeltaMovement();
        p.setDeltaMovement(m.x, Math.min(m.y, 0.0) - 0.0784, m.z);
    }
}
