package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.ModuleManager;

public final class CombatModules {
    private CombatModules() {}

    public static void register() {
        ModuleManager.register(
                new KillAura(),
                new Velocity(),
                new Aimbot(),
                new TriggerBot(),
                new Criticals(),
                new SuperKnockback(),
                new KeepSprint(),
                new Hitbox(),
                new AutoWeapon(),
                new AutoArmor(),
                new AutoLeave(),
                new AutoRod(),
                new AutoBow(),
                new AutoShoot(),
                new ProjectileAimbot(),
                new FakeLag(),
                new CrystalAura()
        );
    }
}
