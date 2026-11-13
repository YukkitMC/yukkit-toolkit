package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.ModuleManager;

/** Registers every module of the World category. */
public final class WorldModules {
    private WorldModules() {}

    public static void register() {
        ModuleManager.register(
                new Scaffold(),
                new Nuker(),
                new AutoTool(),
                new FastBreak(),
                new FastPlace(),
                new Timer(),
                new AutoFarm(),
                new Fucker(),
                new Surround(),
                new BlockIn(),
                new BlockTrap(),
                new BedDefender(),
                new HoleFiller(),
                new LiquidFiller(),
                new LiquidPlace(),
                new AirPlace(),
                new Extinguish(),
                new Clutch(),
                new NoSlowBreak(),
                new ProjectilePuncher(),
                new StrongholdFinder(),
                new AutoDisable());
    }
}
