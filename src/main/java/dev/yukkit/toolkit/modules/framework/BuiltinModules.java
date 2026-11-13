package dev.yukkit.toolkit.modules.framework;

import dev.yukkit.toolkit.ToolkitConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Adapts existing Yukkit features to the common module browser.
 * Each adapter controls an existing implementation; it does not duplicate its tick logic.
 */
public final class BuiltinModules {
    private BuiltinModules() { }

    public static void register() {
        register("keystrokes", "Keystrokes", "Shows the movement and mouse key HUD.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.keystrokes, v -> ToolkitConfig.INSTANCE.keystrokes = v);
        register("tnt-timer", "TNT Timer", "Displays nearby primed TNT fuse timers.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.tntTimer, v -> ToolkitConfig.INSTANCE.tntTimer = v);
        register("team-hud", "Team HUD", "Displays your configured teammate list.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.teamHud, v -> ToolkitConfig.INSTANCE.teamHud = v);
        register("fullbright", "Fullbright", "Raises local world brightness.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.fullbright, v -> ToolkitConfig.INSTANCE.fullbright = v);
        register("item-esp", "Item ESP", "Highlights dropped items already loaded by the client.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.itemEsp, v -> ToolkitConfig.INSTANCE.itemEsp = v);
        register("team-glow", "Team Glow", "Outlines configured teammates already tracked by the client.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.teamGlow, v -> ToolkitConfig.INSTANCE.teamGlow = v);
        register("minimap", "Minimap", "Shows the local surface map and nearby loaded players.", ModuleCategory.RENDER,
                () -> ToolkitConfig.INSTANCE.minimap, v -> ToolkitConfig.INSTANCE.minimap = v);
        register("auto-totem", "Auto Totem", "Refills an empty offhand from the ordinary inventory.", ModuleCategory.PLAYER,
                () -> ToolkitConfig.INSTANCE.autoTotem, v -> ToolkitConfig.INSTANCE.autoTotem = v);
        register("auto-respawn", "Auto Respawn", "Uses the normal respawn action on the death screen.", ModuleCategory.PLAYER,
                () -> ToolkitConfig.INSTANCE.autoRespawn, v -> ToolkitConfig.INSTANCE.autoRespawn = v);
        register("auto-eat", "Auto Eat", "Selects a hotbar golden carrot at the configured hunger level.", ModuleCategory.PLAYER,
                () -> ToolkitConfig.INSTANCE.autoEat, v -> ToolkitConfig.INSTANCE.autoEat = v);
        register("auto-clicker", "Auto Clicker", "Repeats ordinary attacks while attack is held on an entity.", ModuleCategory.COMBAT,
                () -> ToolkitConfig.INSTANCE.autoClicker, v -> ToolkitConfig.INSTANCE.autoClicker = v);
        register("auto-miner", "Auto Miner", "Mines only the block currently under the crosshair.", ModuleCategory.WORLD,
                () -> ToolkitConfig.INSTANCE.autoMiner, v -> ToolkitConfig.INSTANCE.autoMiner = v);
        register("mace-swap", "Mace Swap", "Selects a hotbar mace while falling toward a target.", ModuleCategory.COMBAT,
                () -> ToolkitConfig.INSTANCE.maceSwap, v -> ToolkitConfig.INSTANCE.maceSwap = v);
    }

    private static void register(String id, String name, String description, ModuleCategory category,
                                 Supplier<Boolean> getter, Consumer<Boolean> setter) {
        ConfigModule module = ModuleRegistry.register(new ConfigModule(id, name, description, category, getter, setter));
        module.setEnabled(getter.get());
    }

    private static final class ConfigModule extends Module {
        private final Supplier<Boolean> getter;
        private final Consumer<Boolean> setter;

        private ConfigModule(String id, String name, String description, ModuleCategory category,
                             Supplier<Boolean> getter, Consumer<Boolean> setter) {
            super(id, name, description, category);
            this.getter = getter;
            this.setter = setter;
        }

        @Override protected void onEnable() { setter.accept(true); ToolkitConfig.INSTANCE.save(); }
        @Override protected void onDisable() { setter.accept(false); ToolkitConfig.INSTANCE.save(); }
        @Override public void onTick(net.minecraft.client.Minecraft client) {
            if (isEnabled() != getter.get()) setEnabled(getter.get());
        }
    }
}
