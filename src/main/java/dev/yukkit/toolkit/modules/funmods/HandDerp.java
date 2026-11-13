package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

/**
 * Flips your main hand over and over as seen by the server (other players see the model
 * switch hands). Your own settings are not touched and are re-announced when the module stops.
 */
public final class HandDerp extends Module {
    private final Setting.Num interval = num("Interval (ticks)", 10, 2, 100, 1);

    private HumanoidArm current;
    private int timer;

    public HandDerp() {
        super("HandDerp", "Rapidly switches your main hand for other players.", Category.FUN);
    }

    @Override
    public void onEnable() {
        current = mc.options.mainHand().get();
        timer = 0;
    }

    @Override
    public void onDisable() {
        if (mc.getConnection() == null) return;
        send(mc.options.mainHand().get());
    }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.getConnection() == null) return;
        if (timer-- > 0) return;
        timer = interval.asInt();
        current = current.getOpposite();
        send(current);
    }

    private void send(HumanoidArm arm) {
        int mask = 0;
        for (PlayerModelPart part : mc.options.getModelParts()) mask |= 1 << part.ordinal();
        ClientInformation info = new ClientInformation(
                mc.options.languageCode,
                mc.options.renderDistance().get(),
                mc.options.chatVisibility().get(),
                mc.options.chatColors().get(),
                mask,
                arm,
                mc.isTextFilteringEnabled(),
                mc.options.allowServerListing().get(),
                mc.options.particles().get());
        ModuleManager.sendDirect(new ServerboundClientInformationPacket(info));
    }
}
