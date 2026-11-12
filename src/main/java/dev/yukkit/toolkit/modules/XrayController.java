package dev.yukkit.toolkit.modules;

import dev.yukkit.toolkit.ToolkitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Selective client-side block rendering in Minecraft 26.3.
 *
 * Only blocks already sent to the client can be shown. This does not
 * request hidden blocks from a server or bypass access controls.
 */
public final class XrayController {
    private XrayController() {}

    public static boolean enabled() { return ToolkitConfig.INSTANCE.xray || ToolkitConfig.INSTANCE.chestXray; }

    public static boolean keep(BlockState state) {
        if (state == null) return false;
        return (ToolkitConfig.INSTANCE.xray && isOre(state))
                || (ToolkitConfig.INSTANCE.chestXray && isChest(state.getBlock()));
    }

    public static boolean isChest(Block b) {
        return b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.ENDER_CHEST
                || b == Blocks.BARREL;
    }

    public static boolean isOre(BlockState state) { return state != null && isOre(state.getBlock()); }

    public static boolean isOre(Block b) {
        return b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE
            || b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE
            || b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE
            || b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE
            || b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE
            || b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE
            || b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE
            || b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE
            || b == Blocks.ANCIENT_DEBRIS || b == Blocks.NETHER_QUARTZ_ORE;
    }

    public static void toggle(Minecraft client) {
        ToolkitConfig cfg = ToolkitConfig.INSTANCE;
        cfg.xray = !cfg.xray;
        cfg.save();
        refreshChunks(client);
    }

    public static void toggleChest(Minecraft client) {
        ToolkitConfig cfg = ToolkitConfig.INSTANCE;
        cfg.chestXray = !cfg.chestXray;
        cfg.save();
        refreshChunks(client);
    }

    public static void refreshChunks(Minecraft client) {
        if (client.level != null) client.levelExtractor.allChanged();
    }
}
