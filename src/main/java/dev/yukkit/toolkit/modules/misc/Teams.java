package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import dev.yukkit.toolkit.team.TeamNames;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Team detection helper. Other modules call {@link #isTeammate(Entity)} to skip allies.
 * Always false while this module is disabled.
 */
public final class Teams extends Module {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final Setting.Bool scoreboard = bool("Scoreboard Team", true);
    private final Setting.Bool nameColor = bool("Name Color", true);
    private final Setting.Bool armorColor = bool("Armor Color", false);
    private final Setting.Bool friends = bool("Friends", true);
    private final Setting.Bool custom = bool("Custom List", true);

    public Teams() {
        super("Teams", "Marks teammates and friends so other modules can ignore them.", Category.MISC);
    }

    public static boolean isTeammate(Entity entity) {
        Teams self = ModuleManager.get(Teams.class);
        if (self == null || !self.isEnabled()) return false;
        return self.check(entity);
    }

    private boolean check(Entity entity) {
        if (entity == null || mc.player == null || entity == mc.player) return false;
        if (friends.get() && UtilFriends.isFriend(entity)) return true;
        if (entity instanceof Player player && custom.get() && TeamNames.matches(player)) return true;
        if (scoreboard.get() && mc.player.getTeam() != null && mc.player.isAlliedTo(entity)) return true;
        if (!(entity instanceof Player other)) return false;

        if (nameColor.get()) {
            TextColor mine = mc.player.getDisplayName().getStyle().getColor();
            TextColor theirs = other.getDisplayName().getStyle().getColor();
            if (mine != null && mine.equals(theirs)) return true;
        }
        if (armorColor.get()) {
            Integer mine = dyeColor(mc.player);
            Integer theirs = dyeColor(other);
            if (mine != null && mine.equals(theirs)) return true;
        }
        return false;
    }

    /** First dyed (leather) armor colour found, or null. */
    private static Integer dyeColor(Player player) {
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            DyedItemColor dye = stack.get(DataComponents.DYED_COLOR);
            if (dye != null) return dye.rgb();
        }
        return null;
    }
}
