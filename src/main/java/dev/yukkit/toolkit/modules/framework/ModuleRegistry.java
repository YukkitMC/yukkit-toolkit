package dev.yukkit.toolkit.modules.framework;

import net.minecraft.client.Minecraft;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Central registry for Yukkit's toggleable feature modules. */
public final class ModuleRegistry {
    private static final Map<String, Module> MODULES = new LinkedHashMap<>();
    private ModuleRegistry() { }

    public static synchronized <T extends Module> T register(T module) {
        if (MODULES.putIfAbsent(module.id(), module) != null)
            throw new IllegalArgumentException("Duplicate module id: " + module.id());
        return module;
    }

    public static synchronized Optional<Module> get(String id) {
        return Optional.ofNullable(MODULES.get(id));
    }

    public static synchronized Collection<Module> all() {
        return Collections.unmodifiableList(List.copyOf(MODULES.values()));
    }

    public static synchronized Collection<Module> byCategory(ModuleCategory category) {
        return MODULES.values().stream().filter(m -> m.category() == category).toList();
    }

    public static synchronized boolean unregister(String id) {
        Module module = MODULES.remove(id);
        if (module == null) return false;
        module.setEnabled(false);
        return true;
    }

    public static void tick(Minecraft client) {
        for (Module module : all()) if (module.isEnabled()) module.onTick(client);
    }
}
