package dev.yukkit.toolkit.modules.misc;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Tiny line-based text files in the config folder (one entry per line, '#' starts a comment). */
final class UtilFiles {
    private UtilFiles() {}

    static Path file(String name) {
        return FabricLoader.getInstance().getConfigDir().resolve(name);
    }

    /** Reads the file, creating it with {@code defaults} when missing. Never throws. */
    static List<String> readLines(String name, List<String> defaults) {
        List<String> out = new ArrayList<>();
        try {
            Path p = file(name);
            if (!Files.isRegularFile(p)) {
                Files.createDirectories(p.getParent());
                Files.write(p, defaults, StandardCharsets.UTF_8);
                out.addAll(defaults);
            } else {
                for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                    String t = line.strip();
                    if (!t.isEmpty() && !t.startsWith("#")) out.add(t);
                }
            }
        } catch (Exception e) {
            out.clear();
            for (String d : defaults) if (!d.startsWith("#")) out.add(d);
        }
        return out;
    }

    static void writeLines(String name, List<String> lines) {
        try {
            Path p = file(name);
            Files.createDirectories(p.getParent());
            Files.write(p, lines, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // best effort
        }
    }
}
