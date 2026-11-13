package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.ModuleManager;

public final class MovementModules {
    private MovementModules() {}

    public static void register() {
        ModuleManager.register(
                new Sprint(), new Fly(), new Speed(), new Step(), new NoSlow(), new LongJump(), new HighJump(),
                new Spider(), new SafeWalk(), new Parkour(), new NoWeb(), new LiquidWalk(), new InventoryMove(),
                new ElytraFly(), new Strafe(), new AirJump(), new Anchor(), new AntiBounce(), new AntiLevitation(),
                new AvoidHazards(), new BlockBounce(), new ElytraRecast(), new Freeze(), new NoClip(),
                new NoJumpDelay(), new NoPush(), new SnapTap(), new Sneak(), new VehicleBoost(),
                new VehicleControl(), new TerrainSpeed());
    }
}
