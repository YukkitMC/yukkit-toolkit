package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public final class ElytraRecast extends Module {
    private final Setting.Bool needJump = bool("Hold jump", true);
    private final Setting.Num delay = num("Delay", 8, 2, 40, 1);
    private int cooldown;

    public ElytraRecast() { super("ElytraRecast", "Re-opens your elytra whenever it closes while falling.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (cooldown > 0) { cooldown--; return; }
        LocalPlayer p = mc.player;
        if (p.isSpectator() || p.isPassenger() || p.onGround() || p.isFallFlying() || p.isInWater()) return;
        if (p.hasEffect(MobEffects.LEVITATION) || p.getDeltaMovement().y > -0.1) return;
        if (needJump.get() && !UtilMove.jumpKey()) return;
        if (!p.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) return;
        p.startFallFlying();
        ModuleManager.sendDirect(new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        cooldown = delay.asInt();
    }
}
