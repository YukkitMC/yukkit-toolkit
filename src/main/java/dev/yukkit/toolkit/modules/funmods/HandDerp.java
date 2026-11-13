package dev.yukkit.toolkit.modules.funmods;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.HumanoidArm;

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

    /** Bit set of the skin layers the player currently has enabled. */
    private static int skinLayerBits() {
        return mc.options.modelParts().stream().mapToInt(part -> 1 << part.ordinal()).reduce(0, (x, y) -> x | y);
    }

    /** Re-announces the client settings to the server with the given main hand. */
    private void send(HumanoidArm arm) {
        var o = mc.options;
        ClientInformation announce = new ClientInformation(
                o.languageCode, o.renderDistance().get(), o.chatVisibility().get(), o.chatColors().get(),
                skinLayerBits(), arm, mc.isTextFilteringEnabled(), o.allowServerListing().get(), o.particles().get());
        ModuleManager.sendDirect(new ServerboundClientInformationPacket(announce));
    }
}
