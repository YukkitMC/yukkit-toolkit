package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends the lines of {@code config/yukkit-toolkit-spammer.txt} to chat on an interval.
 * Lines starting with '/' are sent as commands.
 */
public final class Spammer extends Module {
    private static final String FILE = "yukkit-toolkit-spammer.txt";
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final Setting.Num delay = num("Delay (s)", 8.0, 1.0, 120.0, 0.5);
    private final Setting.Choice order = choice("Order", "Sequential", "Sequential", "Random");
    private final Setting.Bool suffix = bool("Random Suffix", true);

    private List<String> lines = new ArrayList<>();
    private int index;
    private int timer;

    public Spammer() {
        super("Spammer", "Repeats the lines of the spammer text file in chat.", Category.MISC);
    }

    @Override
    public void onEnable() {
        lines = UtilFiles.readLines(FILE, List.of("# One message per line. Lines starting with / are commands.", "Hello from Yukkit Toolkit!"));
        index = 0;
        timer = 20;
        if (hasWorld() && lines.isEmpty()) UtilChat.alert("Spammer: " + FILE + " has no messages.");
    }

    @Override
    public void onTick() {
        if (mc.getConnection() == null || lines.isEmpty()) return;
        if (timer-- > 0) return;
        timer = Math.max(1, (int) Math.round(delay.get() * 20.0));

        String line = order.is("Random")
                ? lines.get(ThreadLocalRandom.current().nextInt(lines.size()))
                : lines.get(index++ % lines.size());

        if (line.startsWith("/")) {
            String command = line.substring(1);
            if (!command.isBlank()) mc.getConnection().sendCommand(command);
            return;
        }
        if (suffix.get()) {
            StringBuilder sb = new StringBuilder(line).append(" [");
            for (int i = 0; i < 4; i++) sb.append(ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length())));
            line = sb.append(']').toString();
        }
        if (line.length() > 256) line = line.substring(0, 256);
        mc.getConnection().sendChat(line);
    }

    @Override
    public String tag() { return order.get(); }
}
