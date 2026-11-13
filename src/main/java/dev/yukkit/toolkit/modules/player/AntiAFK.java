package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.util.Mth;

import java.util.Random;

public final class AntiAFK extends Module {
    private final Setting.Choice mode = choice("Mode", "Mixed", "Mixed", "Rotate", "Jump", "Walk");
    private final Setting.Num interval = num("Interval", 20, 5, 120, 5);

    private final Random random = new Random();
    private int timer;
    private int holdTicks;
    private boolean holdJump;
    private boolean holdForward;

    public AntiAFK() {
        super("AntiAFK", "Performs small harmless actions so idle-kick timers keep resetting.", Category.PLAYER);
    }

    @Override public void onEnable() { timer = 0; }
    @Override public void onDisable() { release(); }
    @Override public void onWorldChange() { holdJump = false; holdForward = false; holdTicks = 0; timer = 0; }

    @Override
    public void onTick() {
        if (!hasWorld() || mc.gui.screen() != null) { release(); return; }

        if (holdTicks > 0) {
            if (--holdTicks == 0) release();
            return;
        }
        boolean userActive = mc.options.keyUp.isDown() || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown()
                || mc.options.keyRight.isDown() || mc.options.keyJump.isDown() || mc.options.keyAttack.isDown()
                || mc.options.keyUse.isDown();
        if (userActive) { timer = 0; return; }
        if (++timer < interval.asInt() * 20) return;
        timer = 0;

        String m = mode.get();
        if (mode.is("Mixed")) {
            String[] picks = {"Rotate", "Jump", "Walk"};
            m = picks[random.nextInt(picks.length)];
        }
        switch (m) {
            case "Rotate" -> {
                mc.player.setYRot(mc.player.getYRot() + (random.nextFloat() * 60f - 30f));
                mc.player.setXRot(Mth.clamp(mc.player.getXRot() + (random.nextFloat() * 10f - 5f), -45f, 45f));
            }
            case "Jump" -> {
                mc.options.keyJump.setDown(true);
                holdJump = true;
                holdTicks = 2;
            }
            default -> {
                mc.options.keyUp.setDown(true);
                holdForward = true;
                holdTicks = 8 + random.nextInt(8);
            }
        }
    }

    private void release() {
        if (holdJump) { mc.options.keyJump.setDown(false); holdJump = false; }
        if (holdForward) { mc.options.keyUp.setDown(false); holdForward = false; }
        holdTicks = 0;
    }

    @Override
    public String tag() { return mode.get(); }
}
