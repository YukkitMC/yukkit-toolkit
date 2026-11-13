package dev.yukkit.toolkit.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Registry and dispatcher for all modules. */
public final class ModuleManager {
    public static final Logger LOGGER = LoggerFactory.getLogger("YukkitToolkit/Modules");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("yukkit-toolkit-modules.json");
    private static final List<Module> MODULES = new ArrayList<>();
    /** Packets we sent ourselves via {@link #sendDirect}; they bypass module cancellation. */
    private static final Set<Packet<?>> OWN_PACKETS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean loaded;
    private static Object lastLevel;

    private ModuleManager() {}

    public static void register(Module... modules) {
        for (Module m : modules) MODULES.add(m);
    }

    public static List<Module> all() { return MODULES; }

    public static List<Module> inCategory(Category category) {
        return MODULES.stream().filter(m -> m.category == category).collect(Collectors.toList());
    }

    public static Module byName(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> type) {
        for (Module m : MODULES) if (type.isInstance(m)) return (T) m;
        return null;
    }

    public static boolean isOn(Class<? extends Module> type) {
        Module m = get(type);
        return m != null && m.isEnabled();
    }

    // ------------------------------------------------------------- dispatch
    public static void tick(Minecraft mc) {
        if (!loaded) loadConfig();
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            for (Module m : MODULES) safe(m, Module::onWorldChange);
        }
        for (Module m : MODULES) if (m.isEnabled()) safe(m, Module::onTick);
    }

    public static void hud(GuiGraphicsExtractor graphics) {
        for (Module m : MODULES) if (m.isEnabled()) safe(m, mod -> mod.onHud(graphics));
    }

    public static void world() {
        for (Module m : MODULES) if (m.isEnabled()) safe(m, Module::onWorld);
    }

    /** @return true when any module wants the outgoing packet cancelled. */
    public static boolean fireSend(Packet<?> packet) {
        if (OWN_PACKETS.remove(packet)) return false;
        boolean cancel = false;
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try { cancel |= m.onSendPacket(packet); } catch (Throwable t) { fault(m, t); }
        }
        return cancel;
    }

    /** @return true when any module wants the incoming packet cancelled. */
    public static boolean fireReceive(Packet<?> packet) {
        boolean cancel = false;
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try { cancel |= m.onReceivePacket(packet); } catch (Throwable t) { fault(m, t); }
        }
        return cancel;
    }

    /** Sends a packet without letting modules inspect or cancel it. */
    public static void sendDirect(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        OWN_PACKETS.add(packet);
        mc.getConnection().send(packet);
    }

    private static void safe(Module m, java.util.function.Consumer<Module> action) {
        try { action.accept(m); } catch (Throwable t) { fault(m, t); }
    }

    private static void fault(Module m, Throwable t) {
        LOGGER.warn("Module {} threw and was disabled", m.name, t);
        m.setEnabled(false);
    }

    // ---------------------------------------------------------- persistence
    private static void loadConfig() {
        loaded = true;
        try {
            if (!Files.isRegularFile(FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Module m : MODULES) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                for (Setting<?> s : m.settings()) {
                    if (o.has(s.name) && o.get(s.name).isJsonPrimitive()) {
                        var p = o.getAsJsonPrimitive(s.name);
                        s.fromJson(p.isBoolean() ? (Object) p.getAsBoolean() : p.isNumber() ? (Object) p.getAsDouble() : p.getAsString());
                    }
                }
                if (o.has("enabled") && o.get("enabled").getAsBoolean()) m.restore(true);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not read module settings, using defaults", e);
        }
    }

    public static void save() {
        if (!loaded) return;
        try {
            JsonObject root = new JsonObject();
            for (Module m : MODULES) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.isEnabled());
                for (Setting<?> s : m.settings()) {
                    Object v = s.toJson();
                    if (v instanceof Boolean b) o.addProperty(s.name, b);
                    else if (v instanceof Number n) o.addProperty(s.name, n);
                    else o.addProperty(s.name, String.valueOf(v));
                }
                root.add(m.name, o);
            }
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("Could not save module settings", e);
        }
    }

    public static String slug(String s) { return s.toLowerCase(Locale.ROOT).replace(" ", ""); }
}
