package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class ElytraFly extends Module {
    private final Setting.Num speed = num("Speed", 1.2, 0.2, 4.0, 0.05);
    private final Setting.Num vertical = num("Vertical", 0.5, 0.1, 2.0, 0.05);
    private final Setting.Bool hover = bool("Hover", true);
    private final Setting.Bool autoStart = bool("Auto start", true);
    private int cooldown;

    public ElytraFly() { super("ElytraFly", "Take control of elytra flight.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        LocalPlayer p = mc.player;
        if (cooldown > 0) cooldown--;
        if (p.isSpectator() || p.isPassenger()) return;

        if (!p.isFallFlying()) {
            if (autoStart.get() && cooldown == 0 && !p.onGround() && !p.isInWater() && UtilMove.jumpKey()
                    && p.getDeltaMovement().y < 0.0 && p.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                p.startFallFlying();
                ModuleManager.sendDirect(new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                cooldown = 10;
            }
            return;
        }

        Vec3 look = p.getLookAngle();
        double s = speed.get();
        if (UtilMove.up()) {
            p.setDeltaMovement(look.x * s, look.y * s, look.z * s);
        } else if (hover.get()) {
            double vy = UtilMove.jumpKey() ? vertical.get() : UtilMove.sneakKey() ? -vertical.get() : 0.0;
            p.setDeltaMovement(0.0, vy, 0.0);
        }
        p.fallDistance = 0;
    }
}
