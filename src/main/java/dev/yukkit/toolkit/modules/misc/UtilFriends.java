package dev.yukkit.toolkit.modules.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/** Persistent friend list shared by {@link MiddleClickAction} and {@link Teams}. */
public final class UtilFriends {
    private static final String FILE = "yukkit-toolkit-friends.txt";
    private static final Set<String> NAMES = Collections.synchronizedSet(new TreeSet<>());
    private static boolean loaded;

    private UtilFriends() {}

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        for (String s : UtilFiles.readLines(FILE, List.of("# One friend name per line"))) {
            NAMES.add(s.toLowerCase(Locale.ROOT));
        }
    }

    public static boolean isFriend(String name) {
        if (name == null) return false;
        ensureLoaded();
        return NAMES.contains(name.toLowerCase(Locale.ROOT));
    }

    public static boolean isFriend(Entity entity) {
        return entity instanceof Player p && isFriend(p.getName().getString());
    }

    /** @return true when the name is a friend afterwards. */
    public static boolean toggle(String name) {
        ensureLoaded();
        String key = name.toLowerCase(Locale.ROOT);
        boolean now;
        if (!NAMES.remove(key)) {
            NAMES.add(key);
            now = true;
        } else {
            now = false;
        }
        List<String> lines = new ArrayList<>();
        lines.add("# One friend name per line");
        synchronized (NAMES) { lines.addAll(NAMES); }
        UtilFiles.writeLines(FILE, lines);
        return now;
    }
}
