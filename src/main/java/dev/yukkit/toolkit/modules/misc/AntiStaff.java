package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Warns when a player that looks like server staff is in the player list.
 * Staff are recognised by the names in {@code config/yukkit-toolkit-staff.txt} and,
 * optionally, by rank words inside the name.
 */
public final class AntiStaff extends Module {
    private static final String FILE = "yukkit-toolkit-staff.txt";
    private static final String[] TOKENS = {"admin", "staff", "moderator", "mod_", "_mod", "helper", "owner", "support", "_gm", "gm_"};

    private final Setting.Bool heuristic = bool("Name Heuristic", true);
    private final Setting.Bool sound = bool("Sound", true);

    private final Set<String> staffNames = new HashSet<>();
    private final Set<String> alerted = new HashSet<>();
    private int timer;

    public AntiStaff() {
        super("AntiStaff", "Alerts when a staff-like player is online.", Category.MISC);
    }

    @Override
    public void onEnable() {
        staffNames.clear();
        for (String s : UtilFiles.readLines(FILE, List.of("# Known staff names, one per line"))) {
            staffNames.add(s.toLowerCase(Locale.ROOT));
        }
        alerted.clear();
        timer = 0;
    }

    @Override
    public void onWorldChange() {
        alerted.clear();
        timer = 40;
    }

    @Override
    public void onTick() {
        if (timer-- > 0) return;
        timer = 20;
        if (!hasWorld() || mc.getConnection() == null) return;

        Set<String> online = new HashSet<>();
        for (var info : mc.getConnection().getListedOnlinePlayers()) {
            String name = info.getProfile().name();
            if (name == null) continue;
            String lower = name.toLowerCase(Locale.ROOT);
            online.add(lower);
            if (name.equals(mc.player.getGameProfile().name())) continue;
            if (isStaff(lower) && alerted.add(lower)) {
                UtilChat.alert("Possible staff online: " + name);
                if (sound.get()) UtilChat.ping();
            }
        }
        alerted.retainAll(online); // alert again when someone leaves and rejoins
    }

    private boolean isStaff(String lowerName) {
        if (staffNames.contains(lowerName)) return true;
        if (!heuristic.get()) return false;
        for (String token : TOKENS) if (lowerName.contains(token)) return true;
        return false;
    }

    @Override
    public String tag() { return Integer.toString(alerted.size()); }
}
