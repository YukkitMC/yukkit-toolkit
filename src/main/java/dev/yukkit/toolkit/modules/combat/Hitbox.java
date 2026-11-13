package dev.yukkit.toolkit.modules.combat;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Makes other entities easier to aim at by growing the area your crosshair can pick. Needs {@code HitboxMixin}. */
public final class Hitbox extends Module {
    private final Setting.Num expand = num("Expand", 0.1, 0.0, 0.5, 0.01);
    private final Setting.Bool playersOnly = bool("Players Only", true);

    public Hitbox() {
        super("Hitbox", "Expands the targeting box of other entities.", Category.COMBAT);
    }

    @Override public String tag() { return String.format(java.util.Locale.ROOT, "%.2f", expand.get()); }

    /** Extra pick margin for the given entity, 0 when the module is off or the entity is excluded. */
    public static float margin(Entity e) {
        Hitbox m = ModuleManager.get(Hitbox.class);
        if (m == null || !m.isEnabled() || e == mc.player) return 0f;
        if (m.playersOnly.get() && !(e instanceof Player)) return 0f;
        return m.expand.asFloat();
    }
}
