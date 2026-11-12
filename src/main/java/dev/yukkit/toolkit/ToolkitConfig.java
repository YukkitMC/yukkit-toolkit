package dev.yukkit.toolkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ToolkitConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("yukkit-toolkit.json");

    public static ToolkitConfig INSTANCE = load();

    // 0 = server, 1 = day, 2 = noon, 3 = sunset, 4 = night
    public int localTime = 0;
    // 0 = server, 1 = clear, 2 = rain
    public int localWeather = 0;
    public boolean fullbright = false;

    private static ToolkitConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                ToolkitConfig loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), ToolkitConfig.class);
                if (loaded != null) {
                    loaded.localTime = Math.floorMod(loaded.localTime, 5);
                    loaded.localWeather = Math.floorMod(loaded.localWeather, 3);
                    return loaded;
                }
            }
        } catch (Exception ignored) {
            // Invalid or unreadable configuration: safely use defaults.
        }
        return new ToolkitConfig();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception e) {
            YukkitToolkitClient.LOGGER.warn("Could not save Yukkit Toolkit settings", e);
        }
    }

    public String timeLabel() {
        return new String[]{"Server", "Day (1000)", "Noon (6000)", "Sunset (12000)", "Night (18000)"}[localTime];
    }

    public long timeValue() {
        return new long[]{0L, 1000L, 6000L, 12000L, 18000L}[localTime];
    }

    public String weatherLabel() {
        return new String[]{"Server", "Clear", "Rain"}[localWeather];
    }
}
