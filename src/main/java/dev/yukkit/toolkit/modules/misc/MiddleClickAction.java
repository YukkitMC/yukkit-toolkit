package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/** Middle click (the pick-item key) throws an ender pearl or toggles the targeted player as a friend. */
public final class MiddleClickAction extends Module {
    private final Setting.Choice action = choice("Action", "Both", "Both", "Pearl", "Friend");

    private boolean wasDown;

    public MiddleClickAction() {
        super("MiddleClickAction", "Middle click: friend a player or throw an ender pearl.", Category.MISC);
    }

    @Override
    public void onEnable() { wasDown = false; }

    @Override
    public void onTick() {
        boolean down = mc.options.keyPickItem.isDown();
        boolean pressed = down && !wasDown;
        wasDown = down;
        if (!pressed || !inGame()) return;

        Player target = null;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Player p && p != mc.player) {
            target = p;
        }
        boolean friend = action.is("Friend") || action.is("Both");
        boolean pearl = action.is("Pearl") || action.is("Both");

        if (target != null && friend) {
            String name = target.getName().getString();
            boolean now = UtilFriends.toggle(name);
            UtilChat.info(name + (now ? " added to friends." : " removed from friends."));
        } else if (pearl && !(target != null && action.is("Friend"))) {
            if (!UtilPearl.throwPearl(mc.player.getYRot(), mc.player.getXRot())) {
                UtilChat.alert("No ender pearl in the hotbar or offhand.");
            }
        }
    }

    @Override
    public String tag() { return action.get(); }
}
