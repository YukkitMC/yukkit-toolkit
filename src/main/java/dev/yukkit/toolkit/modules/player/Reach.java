package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class Reach extends Module {
    private static final Identifier ENTITY_ID = Identifier.parse("yukkit_toolkit:reach_entity");
    private static final Identifier BLOCK_ID = Identifier.parse("yukkit_toolkit:reach_block");

    private final Setting.Num entity = num("Entity", 0.8, 0.0, 3.0, 0.1);
    private final Setting.Num block = num("Block", 0.8, 0.0, 3.0, 0.1);

    public Reach() {
        super("Reach", "Adds to your interaction range via attribute modifiers (servers still enforce their own limit).", Category.PLAYER);
    }

    @Override public void onTick() { if (hasWorld()) apply(mc.player, entity.get(), block.get()); }

    @Override
    public void onDisable() {
        if (mc.player != null) apply(mc.player, 0.0, 0.0);
    }

    private static void apply(Player p, double entityExtra, double blockExtra) {
        var e = p.getAttributes().getInstance(Attributes.ENTITY_INTERACTION_RANGE);
        if (e != null) e.addOrUpdateTransientModifier(new AttributeModifier(ENTITY_ID, entityExtra, AttributeModifier.Operation.ADD_VALUE));
        var b = p.getAttributes().getInstance(Attributes.BLOCK_INTERACTION_RANGE);
        if (b != null) b.addOrUpdateTransientModifier(new AttributeModifier(BLOCK_ID, blockExtra, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public String tag() { return String.format(java.util.Locale.ROOT, "+%.1f", entity.get()); }
}
