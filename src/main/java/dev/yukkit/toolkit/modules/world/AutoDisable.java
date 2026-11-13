package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;

/** Switches off a whole category of modules when you die or change world. */
public final class AutoDisable extends Module {
    private final Setting.Choice target = choice("Disable", "World",
            "World", "Combat", "Movement", "Player", "Render", "Misc", "Fun", "Exploit", "All");
    private final Setting.Bool onDeath = bool("On death", true);
    private final Setting.Bool onWorldChange = bool("On world change", true);

    private boolean wasDead;

    public AutoDisable() { super("AutoDisable", "Automatically disables certain modules.", Category.WORLD); }

    @Override public void onEnable() { wasDead = false; }

    @Override
    public void onWorldChange() {
        wasDead = false;
        if (isEnabled() && onWorldChange.get()) disableTargets();
    }

    @Override
    public void onTick() {
        if (!hasWorld()) return;
        boolean dead = mc.player.isDeadOrDying();
        if (dead && !wasDead && onDeath.get()) disableTargets();
        wasDead = dead;
    }

    private void disableTargets() {
        for (Module m : ModuleManager.all()) {
            if (m == this || !m.isEnabled()) continue;
            if (target.is("All") || m.category.label.equalsIgnoreCase(target.get())) m.setEnabled(false);
        }
    }

    @Override public String tag() { return target.get(); }
}
