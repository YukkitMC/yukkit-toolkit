package dev.yukkit.toolkit.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every toggleable module. Subclasses declare settings in the
 * constructor with {@link #bool}, {@link #num}, {@link #choice} and override only
 * the hooks they need. Hooks are only called while the module is enabled and a
 * world is loaded (except {@link #onDisable()}, which always runs on toggle off).
 */
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();

    public final String name;
    public final String description;
    public final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // ------------------------------------------------------------ settings
    protected Setting.Bool bool(String name, boolean def) {
        Setting.Bool s = new Setting.Bool(name, def);
        settings.add(s);
        return s;
    }

    protected Setting.Num num(String name, double def, double min, double max, double step) {
        Setting.Num s = new Setting.Num(name, def, min, max, step);
        settings.add(s);
        return s;
    }

    protected Setting.Choice choice(String name, String def, String... options) {
        Setting.Choice s = new Setting.Choice(name, def, options);
        settings.add(s);
        return s;
    }

    public List<Setting<?>> settings() { return settings; }

    // --------------------------------------------------------------- state
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        try {
            if (value) onEnable(); else onDisable();
        } catch (Throwable t) {
            ModuleManager.LOGGER.warn("Module {} failed during {}", name, value ? "enable" : "disable", t);
        }
        ModuleManager.save();
    }

    public void toggle() { setEnabled(!enabled); }

    /** Restores the persisted state without firing hooks twice. */
    void restore(boolean value) {
        enabled = value;
        if (value) {
            try { onEnable(); } catch (Throwable t) { enabled = false; }
        }
    }

    /** Short text drawn next to the name in the array list (e.g. the current mode). */
    public String tag() { return ""; }

    // --------------------------------------------------------------- hooks
    public void onEnable() {}
    public void onDisable() {}
    /** Called once per client tick (END_CLIENT_TICK) while in a world. */
    public void onTick() {}
    /** Called every HUD frame. */
    public void onHud(GuiGraphicsExtractor graphics) {}
    /** Called every frame inside the level render pass (use {@code Gizmos}). */
    public void onWorld() {}
    /** Outgoing packet. Return true to cancel it. Packets this mod sends itself are not re-offered. */
    public boolean onSendPacket(Packet<?> packet) { return false; }
    /** Incoming packet. Return true to cancel it. */
    public boolean onReceivePacket(Packet<?> packet) { return false; }
    /** Called when the player leaves/joins a world. */
    public void onWorldChange() {}

    // ------------------------------------------------------------- helpers
    /** True when a player and level exist and no GUI screen is open. */
    protected static boolean inGame() {
        return mc.player != null && mc.level != null && mc.gui.screen() == null;
    }

    protected static boolean hasWorld() {
        return mc.player != null && mc.level != null;
    }
}
