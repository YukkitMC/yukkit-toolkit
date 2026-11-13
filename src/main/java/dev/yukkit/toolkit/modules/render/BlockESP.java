package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class BlockESP extends Module {
    private final Setting.Choice type = choice("Block", "Diamond", "Diamond", "Emerald", "Gold", "Iron", "Coal",
            "Redstone", "Lapis", "Copper", "Debris", "Spawner", "Chest", "Bed");
    private final Setting.Choice style = choice("Style", "Both", "Outline", "Filled", "Both");
    private final Setting.Num radius = num("Radius", 16, 4, 32, 2);
    private final Setting.Num limit = num("MaxBlocks", 150, 10, 500, 10);
    private final Setting.Bool tracers = bool("Tracers", false);

    private List<BlockPos> found = new ArrayList<>();
    private int timer;

    public BlockESP() { super("BlockESP", "Highlights a chosen block type nearby.", Category.RENDER); }

    @Override public void onEnable() { found = new ArrayList<>(); timer = 0; }
    @Override public void onDisable() { found = new ArrayList<>(); }
    @Override public void onWorldChange() { found = new ArrayList<>(); timer = 0; }

    private boolean matches(Block b) {
        switch (type.get()) {
            case "Diamond": return b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE;
            case "Emerald": return b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE;
            case "Gold": return b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE;
            case "Iron": return b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE;
            case "Coal": return b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE;
            case "Redstone": return b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE;
            case "Lapis": return b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE;
            case "Copper": return b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE;
            case "Debris": return b == Blocks.ANCIENT_DEBRIS;
            case "Spawner": return b == Blocks.SPAWNER;
            case "Chest": return b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.ENDER_CHEST;
            case "Bed": return b instanceof net.minecraft.world.level.block.BedBlock;
            default: return false;
        }
    }

    private int color() {
        switch (type.get()) {
            case "Diamond": return 0x40E0FF;
            case "Emerald": return 0x30E060;
            case "Gold": return 0xFFD030;
            case "Iron": return 0xE0B090;
            case "Coal": return 0x606060;
            case "Redstone": return 0xFF3030;
            case "Lapis": return 0x3050FF;
            case "Copper": return 0xE07040;
            case "Debris": return 0x906040;
            case "Spawner": return 0xA040FF;
            case "Chest": return 0xFFA020;
            default: return 0xFF6080;
        }
    }

    @Override public void onTick() {
        if (!hasWorld()) return;
        if (timer-- > 0) return;
        timer = 20;
        int r = radius.asInt();
        found = UtilDraw.scan(r, r, limit.asInt(), (p, s) -> matches(s.getBlock()));
    }

    @Override public void onWorld() {
        if (found.isEmpty() || !hasWorld()) return;
        int rgb = color();
        var origin = UtilDraw.lineOrigin();
        for (BlockPos p : found) {
            UtilDraw.box(new AABB(p), rgb, style.get(), true);
            if (tracers.get()) UtilDraw.line(origin, net.minecraft.world.phys.Vec3.atCenterOf(p), rgb, 1.0f, true);
        }
    }

    @Override public String tag() { return type.get(); }
}
