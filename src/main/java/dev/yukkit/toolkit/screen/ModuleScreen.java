package dev.yukkit.toolkit.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Module browser with a translucent-black surface, a single blue accent, white/dimmed-grey text and
 * switch-style toggles. Category tabs on top, module grid on the left, settings of the
 * selected module on the right. Left click toggles / steps, right click selects / steps back.
 */
public final class ModuleScreen extends Screen {
    // Palette: black surface at several opacities, one accent, success green, dimmed text.
    private static final int SURFACE_68 = 0xAD000000;
    private static final int SURFACE_50 = 0x80000000;
    private static final int SURFACE_36 = 0x5C000000;
    private static final int SURFACE_90 = 0xE6000000;
    private static final int ACCENT = 0xFF4677FF;
    private static final int SUCCESS = 0xFF4DAC68;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFD3D3D3;

    private static final int TAB_H = 22, ROW_H = 16, CELL_W = 112, SET_W = 168, PAD = 6;

    private final Screen parent;
    private Category category = Category.COMBAT;
    private Module selected;
    private int scroll;
    private int settingScroll;

    // Hit boxes recomputed each frame: {x1, y1, x2, y2}
    private final List<int[]> tabHits = new ArrayList<>();
    private final List<int[]> moduleHits = new ArrayList<>();
    private final List<Module> moduleHitTargets = new ArrayList<>();
    private final List<int[]> settingHits = new ArrayList<>();
    private final List<Setting<?>> settingHitTargets = new ArrayList<>();

    public ModuleScreen(Screen parent) {
        super(Component.literal("Yukkit Modules"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private int settingsX;
    private int panelTop() { return 28; }
    private int gridCols() { return Math.max(1, Math.min(4, (width - SET_W - PAD * 4 - 16) / CELL_W)); }
    private int panelBottom() { return height - 12; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x66000000);
        tabHits.clear();
        moduleHits.clear();
        moduleHitTargets.clear();
        settingHits.clear();
        settingHitTargets.clear();

        int cols = gridCols();
        int left = Math.max(8, (width - (cols * CELL_W + SET_W + PAD * 3)) / 2);
        int top = panelTop();
        int gridW = cols * CELL_W;
        int total = gridW + SET_W + PAD * 3;

        // ---- tab bar
        g.fill(left, top - TAB_H, left + total, top, SURFACE_68);
        Category[] cats = Category.values();
        int tabW = total / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int x1 = left + i * tabW, x2 = i == cats.length - 1 ? left + total : x1 + tabW;
            boolean active = cats[i] == category;
            if (active) g.fill(x1, top - 2, x2, top, ACCENT);
            g.text(font, cats[i].label, x1 + (x2 - x1 - font.width(cats[i].label)) / 2, top - TAB_H + 7,
                    active ? TEXT : TEXT_DIM, true);
            tabHits.add(new int[]{x1, top - TAB_H, x2, top});
        }

        // ---- module grid
        int gx = left + PAD;
        int gTop = top + PAD;
        int gBottom = panelBottom();
        g.fill(left, top, left + gridW + PAD * 2, gBottom, SURFACE_50);
        List<Module> mods = ModuleManager.inCategory(category);
        int rows = Math.max(1, (gBottom - gTop - PAD) / ROW_H);
        int perPage = rows * cols;
        int maxScroll = Math.max(0, (mods.size() - perPage + cols - 1) / cols);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        int first = scroll * cols;
        for (int i = first; i < mods.size() && i < first + perPage; i++) {
            Module m = mods.get(i);
            int idx = i - first;
            int x1 = gx + (idx % cols) * CELL_W, y1 = gTop + (idx / cols) * ROW_H;
            int x2 = x1 + CELL_W - 3, y2 = y1 + ROW_H - 2;
            boolean hover = mx >= x1 && mx < x2 && my >= y1 && my < y2;
            g.fill(x1, y1, x2, y2, m == selected ? SURFACE_90 : hover ? SURFACE_68 : SURFACE_36);
            if (m.isEnabled()) g.fill(x1, y1, x1 + 2, y2, ACCENT);
            g.text(font, m.name, x1 + 6, y1 + 4, m.isEnabled() ? TEXT : TEXT_DIM, true);
            moduleHits.add(new int[]{x1, y1, x2, y2});
            moduleHitTargets.add(m);
        }
        if (maxScroll > 0) {
            String hint = (scroll + 1) + "/" + (maxScroll + 1);
            g.text(font, hint, left + gridW + PAD * 2 - font.width(hint) - 4, top + 2, TEXT_DIM, true);
        }

        // ---- settings panel
        int sx = left + gridW + PAD * 2 + PAD;
        settingsX = sx;
        g.fill(sx, top, sx + SET_W, gBottom, SURFACE_68);
        if (selected == null) {
            g.text(font, "Right-click a module", sx + 8, top + 8, TEXT_DIM, true);
            g.text(font, "to see its settings.", sx + 8, top + 20, TEXT_DIM, true);
        } else {
            g.text(font, selected.name, sx + 8, top + 8, TEXT, true);
            drawSwitch(g, sx + SET_W - 30, top + 6, selected.isEnabled());
            settingHits.add(new int[]{sx + SET_W - 30, top + 6, sx + SET_W - 8, top + 16});
            settingHitTargets.add(null);
            g.text(font, trim(selected.description, SET_W - 16), sx + 8, top + 22, TEXT_DIM, true);
            int y = top + 40;
            List<Setting<?>> list = selected.settings();
            int visible = Math.max(1, (gBottom - y - 4) / ROW_H);
            settingScroll = Math.max(0, Math.min(Math.max(0, list.size() - visible), settingScroll));
            for (int i = settingScroll; i < list.size() && i < settingScroll + visible; i++) {
                Setting<?> s = list.get(i);
                int x1 = sx + 6, x2 = sx + SET_W - 6;
                boolean hover = mx >= x1 && mx < x2 && my >= y && my < y + ROW_H - 2;
                g.fill(x1, y, x2, y + ROW_H - 2, hover ? SURFACE_90 : SURFACE_36);
                g.text(font, s.name, x1 + 5, y + 4, TEXT_DIM, true);
                if (s instanceof Setting.Bool b) {
                    drawSwitch(g, x2 - 26, y + 3, b.get());
                } else {
                    String v = s.display();
                    g.text(font, v, x2 - 5 - font.width(v), y + 4, ACCENT, true);
                }
                settingHits.add(new int[]{x1, y, x2, y + ROW_H - 2});
                settingHitTargets.add(s);
                y += ROW_H;
            }
            if (list.isEmpty()) g.text(font, "No settings.", sx + 8, top + 42, TEXT_DIM, true);
        }
        g.text(font, "Left: toggle / next    Right: select / previous    Wheel: scroll    RShift: close",
                left, gBottom + 2, TEXT_DIM, true);
    }

    private void drawSwitch(GuiGraphicsExtractor g, int x, int y, boolean on) {
        g.fill(x, y, x + 22, y + 10, on ? SUCCESS : 0xFF3A3A3A);
        int knob = on ? x + 12 : x + 2;
        g.fill(knob, y + 2, knob + 8, y + 8, TEXT);
    }

    private String trim(String s, int maxWidth) {
        if (font.width(s) <= maxWidth) return s;
        while (s.length() > 1 && font.width(s + "...") > maxWidth) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    private static boolean inside(int[] r, double x, double y) {
        return x >= r[0] && x < r[2] && y >= r[1] && y < r[3];
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double x = event.x(), y = event.y();
        int button = event.button();
        for (int i = 0; i < tabHits.size(); i++) {
            if (inside(tabHits.get(i), x, y)) {
                category = Category.values()[i];
                selected = null;
                scroll = 0;
                settingScroll = 0;
                return true;
            }
        }
        for (int i = 0; i < moduleHits.size(); i++) {
            if (!inside(moduleHits.get(i), x, y)) continue;
            Module m = moduleHitTargets.get(i);
            if (button == 1) {
                selected = m;
                settingScroll = 0;
            } else if (button == 0) {
                m.toggle();
            }
            return true;
        }
        for (int i = 0; i < settingHits.size(); i++) {
            if (!inside(settingHits.get(i), x, y)) continue;
            Setting<?> s = settingHitTargets.get(i);
            if (s == null) {
                if (selected != null) selected.toggle();
            } else {
                s.step(button != 1);
                ModuleManager.save();
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int dir = scrollY > 0 ? -1 : 1;
        if (mouseX >= settingsX) settingScroll += dir; else scroll += dir;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RSHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}
