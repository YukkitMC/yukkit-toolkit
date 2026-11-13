package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;

import java.util.List;

/** Keeps a ring of blocks around your feet, like a hole that blunts explosions. */
public final class Surround extends Module {
    private final Setting.Choice blocks = choice("Blocks", "Obsidian", "Obsidian", "Any");
    private final Setting.Num delay = num("Delay", 1, 0, 5, 1);
    private final Setting.Num perTick = num("Blocks per tick", 2, 1, 4, 1);
    private final Setting.Bool floor = bool("Fill floor", true);
    private final Setting.Bool disableOnLeave = bool("Disable when leaving", true);

    private BlockPos home;
    private int cooldown;

    public Surround() {
        super("Surround", "Builds holes that protect you from explosion damage.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        home = hasWorld() ? mc.player.blockPosition().immutable() : null;
        cooldown = 0;
    }

    @Override public void onDisable() { home = null; }

    @Override public void onWorldChange() { home = hasWorld() ? mc.player.blockPosition().immutable() : null; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready()) return;
        BlockPos feet = mc.player.blockPosition();
        if (home == null) home = feet.immutable();
        if (disableOnLeave.get() && !home.equals(feet)) {
            setEnabled(false);
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        int slot = blocks.is("Any")
                ? UtilBuild.findSlot(UtilBuild::isBuildBlock)
                : UtilBuild.findSlot(s -> s.is(Items.OBSIDIAN));
        if (slot == UtilBuild.NONE) return;
        List<BlockPos> targets = UtilBuild.cage(feet, floor.get(), 1, false);
        if (UtilBuild.placeAll(targets, slot, perTick.asInt(), false) > 0) cooldown = delay.asInt();
    }

    @Override public String tag() { return blocks.get(); }
}
