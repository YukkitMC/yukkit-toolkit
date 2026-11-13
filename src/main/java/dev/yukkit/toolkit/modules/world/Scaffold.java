package dev.yukkit.toolkit.modules.world;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Places blocks under your feet (and optionally ahead of you) while you walk. */
public final class Scaffold extends Module {
    private final Setting.Num delay = num("Delay", 0, 0, 6, 1);
    private final Setting.Num expand = num("Expand", 0, 0, 5, 1);
    private final Setting.Num perTick = num("Blocks per tick", 1, 1, 3, 1);
    private final Setting.Bool airPlace = bool("Air place", false);
    private final Setting.Bool noGliding = bool("Pause when gliding", true);

    private int cooldown;

    public Scaffold() {
        super("Scaffold", "Automatically places blocks beneath your feet allowing you to walk across gaps.", Category.WORLD);
    }

    @Override public void onEnable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!inGame() || !UtilBuild.ready()) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (noGliding.get() && mc.player.isFallFlying()) return;
        int slot = UtilBuild.findSlot(UtilBuild::isBuildBlock);
        if (slot == UtilBuild.NONE) return;

        double x = mc.player.getX();
        double z = mc.player.getZ();
        double y = mc.player.getBoundingBox().minY - 0.5;
        BlockPos base = BlockPos.containing(x, y, z);

        int budget = perTick.asInt();
        if (UtilBuild.isReplaceable(base) && UtilBuild.placeAt(base, slot, airPlace.get())) budget--;

        int ahead = expand.asInt();
        if (ahead > 0 && budget > 0) {
            Vec3 motion = mc.player.getDeltaMovement();
            double len = Math.hypot(motion.x, motion.z);
            if (len > 0.03) {
                double dx = motion.x / len;
                double dz = motion.z / len;
                BlockPos last = base;
                for (int i = 1; i <= ahead && budget > 0; i++) {
                    BlockPos next = BlockPos.containing(x + dx * i, y, z + dz * i);
                    if (next.equals(last)) continue;
                    last = next;
                    if (UtilBuild.isReplaceable(next) && UtilBuild.placeAt(next, slot, false)) budget--;
                }
            }
        }
        if (budget < perTick.asInt()) cooldown = delay.asInt();
    }

    @Override
    public String tag() {
        return hasWorld() ? Integer.toString(UtilBuild.countItems(UtilBuild::isBuildBlock)) : "";
    }
}
