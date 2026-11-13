package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One-shot module: fills the book-and-quill in your main hand with generated pages
 * (and optionally signs it), then turns itself off.
 */
public final class BookBot extends Module {
    private static final String[] WORDS = {
            "alpha", "bridge", "castle", "dragon", "ember", "forest", "golem", "harbor", "iron", "jungle",
            "kelp", "lantern", "mine", "nether", "obsidian", "portal", "quartz", "redstone", "slime", "tower",
            "village", "warden", "yield", "zombie"
    };

    private final Setting.Num pages = num("Pages", 20, 1, 100, 1);
    private final Setting.Num pageLength = num("Page Length", 256, 16, 1000, 16);
    private final Setting.Choice content = choice("Content", "Words", "Words", "Random", "Counter");
    private final Setting.Bool sign = bool("Sign", false);

    private boolean pending;

    public BookBot() {
        super("BookBot", "Fills the held book and quill with generated text.", Category.MISC);
    }

    @Override
    public void onEnable() { pending = true; }

    @Override
    public void onDisable() { pending = false; }

    @Override
    public void onTick() {
        if (!pending) return;
        pending = false;
        try {
            write();
        } finally {
            setEnabled(false);
        }
    }

    private void write() {
        if (!hasWorld() || mc.getConnection() == null) return;
        Player player = mc.player;
        if (!player.getMainHandItem().is(Items.WRITABLE_BOOK)) {
            UtilChat.alert("BookBot: hold a book and quill in your main hand.");
            return;
        }
        int pageCount = pages.asInt();
        int length = pageLength.asInt();
        List<String> out = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) out.add(page(i, length));
        Optional<String> title = sign.get() ? Optional.of("Yukkit " + ThreadLocalRandom.current().nextInt(1000, 10000)) : Optional.empty();
        ModuleManager.sendDirect(new ServerboundEditBookPacket(player.getInventory().getSelectedSlot(), out, title));
        UtilChat.info("BookBot: wrote " + pageCount + " pages" + (sign.get() ? " and signed the book." : "."));
    }

    private String page(int index, int length) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        if (content.is("Counter")) {
            for (int n = index * 1000; sb.length() < length; n++) sb.append(n).append(' ');
        } else if (content.is("Random")) {
            while (sb.length() < length) sb.append((char) ('a' + rng.nextInt(26)));
        } else {
            while (sb.length() < length) sb.append(WORDS[rng.nextInt(WORDS.length)]).append(' ');
        }
        sb.setLength(length);
        return sb.toString();
    }
}
