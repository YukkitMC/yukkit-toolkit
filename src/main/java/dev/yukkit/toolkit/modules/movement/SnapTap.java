package dev.yukkit.toolkit.modules.movement;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import net.minecraft.world.entity.player.Input;

/** When two opposite keys are held, the one pressed last wins. Applied by an input mixin. */
public final class SnapTap extends Module {
    private boolean pl, pr, pf, pb;
    private int lastH, lastV; // -1 / 0 / +1: left or forward wins at -1/+1 respectively

    public SnapTap() { super("SnapTap", "Last pressed of two opposite movement keys wins.", Category.MOVEMENT); }

    @Override public void onEnable() { pl = pr = pf = pb = false; lastH = lastV = 0; }

    public Input filter(Input in) {
        boolean l = in.left(), r = in.right(), f = in.forward(), b = in.backward();
        if (l && !pl) lastH = -1;
        if (r && !pr) lastH = 1;
        if (f && !pf) lastV = 1;
        if (b && !pb) lastV = -1;
        pl = l; pr = r; pf = f; pb = b;
        boolean outL = l, outR = r, outF = f, outB = b;
        if (l && r && lastH != 0) { outL = lastH == -1; outR = lastH == 1; }
        if (f && b && lastV != 0) { outF = lastV == 1; outB = lastV == -1; }
        if (outL == l && outR == r && outF == f && outB == b) return in;
        return new Input(outF, outB, outL, outR, in.jump(), in.shift(), in.sprint());
    }
}
