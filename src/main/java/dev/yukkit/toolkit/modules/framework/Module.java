package dev.yukkit.toolkit.modules.framework;

import java.util.Objects;

/**
 * Lifecycle base for independently toggleable client features.
 * Implementations should use the Minecraft client APIs and remain disabled by default.
 */
public abstract class Module {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;

    protected Module(String id, String name, String description, ModuleCategory category) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.description = Objects.requireNonNullElse(description, "");
        this.category = Objects.requireNonNull(category, "category");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }

    public final String id() { return id; }
    public final String name() { return name; }
    public final String description() { return description; }
    public final ModuleCategory category() { return category; }
    public final boolean isEnabled() { return enabled; }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public final void toggle() { setEnabled(!enabled); }
    protected void onEnable() { }
    protected void onDisable() { }
    public void onTick(net.minecraft.client.Minecraft client) { }
}
