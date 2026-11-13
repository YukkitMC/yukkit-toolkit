package dev.yukkit.toolkit.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.ModuleManager;
import dev.yukkit.toolkit.core.Setting;
import dev.yukkit.toolkit.team.TeamEspColor;
import dev.yukkit.toolkit.team.TeamNames;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Window-style module browser: grey title bar with small icon buttons, a left tab column
 * (selected tab in cyan, stats at the bottom), a dark content pane with a section header,
 * a search box, striped rows and green / blue action buttons.
 * Tabs: the eight module categories plus Teams. Typing filters modules across every category.
 */
public final class ModuleScreen extends Screen {
    // Palette
    private static final int OUTLINE = 0xFF111111;
    private static final int LIGHT_EDGE = 0xFF9A9A9A;
    private static final int TITLE_BG = 0xFF767676;
    private static final int SIDE_BG = 0xFF626262;
    private static final int CONTENT_BG = 0xFF353535;
    private static final int ROW_BG = 0xFF4A4A4A;
    private static final int ROW_HOVER = 0xFF595959;
    private static final int ROW_SELECTED = 0xFF666666;
    private static final int ICON_BG = 0xFF555555;
    private static final int ICON_EDGE = 0xFFC4C4C4;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xFFC8C8C8;
    private static final int FAINT = 0xFF9A9A9A;
    private static final int CYAN = 0xFF4FD8FF;
    private static final int GREEN = 0xFF3AA630;
    private static final int GREEN_EDGE = 0xFF8AF070;
    private static final int BLUE = 0xFF2B7BDB;
    private static final int BLUE_EDGE = 0xFF5FB0FF;
    private static final int RED = 0xFFFF5555;
    private static final int SWITCH_ON = 0xFF4DAC68;
    private static final int SWITCH_OFF = 0xFF2B2B2B;

    private static final int SIDEBAR_W = 120, TITLE_H = 28, ROW_H = 22, BTN_H = 24;
    private static final int TEAMS_VIEW = Category.values().length;
    private static final int MAX_TEAM = 64;

    private enum Kind { MODULE, SETTING, LABEL, SWATCHES, MEMBER, NEARBY, TEAM_TOGGLE }

    private static final class Row {
        final Kind kind;
        final String text;
        Module module;
        Setting<?> setting;
        String name;
        Row(Kind kind, String text) { this.kind = kind; this.text = text; }
    }

    private final Screen parent;
    private int view = 0;
    private String search = "";
    private int scroll = 0;
    private Module selected;
    private Module expanded;
    private String feedback = "";

    private EditBox box;
    private String teamInput = "";

    // layout (recomputed in init)
    private int x0, y0, w, h;

    // hit boxes: {x1, y1, x2, y2}
    private final List<int[]> tabHits = new ArrayList<>();
    private final List<int[]> rowHits = new ArrayList<>();
    private final List<Row> rowTargets = new ArrayList<>();
    private int[] closeHit, classicHit, greenHit, blueHit;
    private final List<int[]> swatchHits = new ArrayList<>();

    public ModuleScreen(Screen parent) {
        super(Component.literal("Yukkit Toolkit"));
        this.parent = parent;
    }

    // ------------------------------------------------------------------ setup
    @Override
    protected void init() {
        super.init();
        w = Math.min(width - 20, Math.max(420, (int) (width * 0.86)));
        h = Math.min(height - 20, Math.max(250, (int) (height * 0.88)));
        x0 = (width - w) / 2;
        y0 = (height - h) / 2;

        boolean teams = view == TEAMS_VIEW;
        box = new EditBox(font, contentX() + 8, y0 + 6, contentW() - 8 - 56, 16,
                Component.literal(teams ? "Player name" : "Search modules"));
        box.setMaxLength(32);
        box.setHint(Component.literal(teams ? "Player name (offline names allowed)" : "Search all modules..."));
        box.setValue(teams ? teamInput : search);
        box.setResponder(text -> {
            if (view == TEAMS_VIEW) teamInput = text;
            else {
                search = text;
                scroll = 0;
            }
        });
        addRenderableWidget(box);
        setInitialFocus(box);
    }

    private int contentX() { return x0 + SIDEBAR_W; }
    private int contentW() { return w - SIDEBAR_W; }
    private int listTop() { return y0 + TITLE_H + 26; }
    private int bottomBarY() { return y0 + h - 34; }
    private int visibleRows() { return Math.max(1, (bottomBarY() - 4 - listTop()) / ROW_H); }

    @Override
    public void onClose() {
        ModuleManager.save();
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private void switchView(int next) {
        if (next == view) return;
        view = next;
        scroll = 0;
        feedback = "";
        rebuildWidgets();
    }

    // ----------------------------------------------------------------- rows
    private List<Row> buildRows() {
        List<Row> rows = new ArrayList<>();
        if (view == TEAMS_VIEW) {
            rows.add(new Row(Kind.TEAM_TOGGLE, "Glow teammates through walls"));
            rows.add(new Row(Kind.SWATCHES, "Team color"));
            ToolkitConfig cfg = ToolkitConfig.INSTANCE;
            int members = cfg.teamMates == null ? 0 : cfg.teamMates.size();
            rows.add(new Row(Kind.LABEL, "Members [" + members + "/" + MAX_TEAM + "]"));
            if (members == 0) rows.add(new Row(Kind.LABEL, "  nobody yet - type a name and press Add"));
            for (int i = 0; members > 0 && i < members; i++) {
                Row r = new Row(Kind.MEMBER, cfg.teamMates.get(i));
                r.name = cfg.teamMates.get(i);
                rows.add(r);
            }
            rows.add(new Row(Kind.LABEL, "Nearby players"));
            boolean any = false;
            if (minecraft != null && minecraft.level != null && minecraft.player != null) {
                for (Entity e : minecraft.level.entitiesForRendering()) {
                    if (!(e instanceof Player p) || p == minecraft.player) continue;
                    String n = p.getName().getString();
                    if (inTeam(n)) continue;
                    Row r = new Row(Kind.NEARBY, n);
                    r.name = n;
                    rows.add(r);
                    any = true;
                    if (rows.size() > 400) break;
                }
            }
            if (!any) rows.add(new Row(Kind.LABEL, "  nobody nearby"));
            return rows;
        }

        String q = search.trim().toLowerCase(Locale.ROOT);
        List<Module> source = new ArrayList<>();
        if (q.isEmpty()) {
            source.addAll(ModuleManager.inCategory(Category.values()[view]));
        } else {
            for (Module m : ModuleManager.all()) {
                if (m.name.toLowerCase(Locale.ROOT).contains(q) || m.description.toLowerCase(Locale.ROOT).contains(q)) source.add(m);
            }
        }
        for (Module m : source) {
            Row r = new Row(Kind.MODULE, m.name);
            r.module = m;
            rows.add(r);
            if (m == expanded) {
                for (Setting<?> s : m.settings()) {
                    Row sr = new Row(Kind.SETTING, s.name);
                    sr.module = m;
                    sr.setting = s;
                    rows.add(sr);
                }
                if (m.settings().isEmpty()) rows.add(new Row(Kind.LABEL, "    no settings"));
            }
        }
        return rows;
    }

    private static boolean inTeam(String name) {
        List<String> list = ToolkitConfig.INSTANCE.teamMates;
        if (list == null) return false;
        for (String s : list) if (s.equalsIgnoreCase(name)) return true;
        return false;
    }

    // --------------------------------------------------------------- drawing
    private static void outline(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void button(GuiGraphicsExtractor g, int[] r, String label, int fill, int edge, boolean enabled) {
        g.fill(r[0], r[1], r[2], r[3], enabled ? fill : 0xFF4A4A4A);
        outline(g, r[0], r[1], r[2], r[3], enabled ? edge : 0xFF6A6A6A);
        if (enabled) {
            g.fill(r[0] + 1, r[1] + 1, r[2] - 1, r[1] + 2, 0x55FFFFFF);
            g.fill(r[0] + 1, r[3] - 2, r[2] - 1, r[3] - 1, 0x44000000);
        }
        g.text(font, label, r[0] + (r[2] - r[0] - font.width(label)) / 2, r[1] + (r[3] - r[1] - 8) / 2, enabled ? WHITE : FAINT, true);
    }

    private void drawSwitch(GuiGraphicsExtractor g, int x, int y, boolean on) {
        g.fill(x, y, x + 22, y + 10, on ? SWITCH_ON : SWITCH_OFF);
        outline(g, x, y, x + 22, y + 10, OUTLINE);
        int knob = on ? x + 13 : x + 2;
        g.fill(knob, y + 2, knob + 7, y + 8, WHITE);
    }

    private static boolean inside(int[] r, double x, double y) {
        return r != null && x >= r[0] && x < r[2] && y >= r[1] && y < r[3];
    }

    private int enabledCount() {
        int n = 0;
        for (Module m : ModuleManager.all()) if (m.isEnabled()) n++;
        return n;
    }

    private String version() {
        try {
            return FabricLoader.getInstance().getModContainer("yukkit_toolkit")
                    .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
        } catch (Throwable t) {
            return "?";
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        // Vanilla background first; it also draws the EditBox, so panels below leave a hole for it.
        super.extractRenderState(g, mx, my, delta);
        tabHits.clear();
        rowHits.clear();
        rowTargets.clear();
        swatchHits.clear();

        int x1 = x0, y1 = y0, x2 = x0 + w, y2 = y0 + h;
        int cx = contentX();

        // ---- window + title bar
        g.fill(x1, y1, x2, y2, SIDE_BG);
        int tbx1 = box.getX(), tby1 = box.getY(), tbx2 = tbx1 + box.getWidth(), tby2 = tby1 + box.getHeight();
        g.fill(x1, y1, x2, tby1, TITLE_BG);
        g.fill(x1, tby2, x2, y1 + TITLE_H, TITLE_BG);
        g.fill(x1, tby1, tbx1, tby2, TITLE_BG);
        g.fill(tbx2, tby1, x2, tby2, TITLE_BG);
        g.fill(x1, y1 + TITLE_H - 1, x2, y1 + TITLE_H, OUTLINE);
        g.fill(x1 + 1, y1 + 1, x2 - 1, y1 + 2, 0x33FFFFFF);
        g.text(font, "Yukkit Toolkit", x1 + 10, y1 + 10, WHITE, true);

        closeHit = new int[]{x2 - 24, y1 + 5, x2 - 6, y1 + 23};
        classicHit = new int[]{x2 - 48, y1 + 5, x2 - 30, y1 + 23};
        for (int[] r : new int[][]{closeHit, classicHit}) {
            g.fill(r[0], r[1], r[2], r[3], ICON_BG);
            outline(g, r[0], r[1], r[2], r[3], inside(r, mx, my) ? WHITE : ICON_EDGE);
        }
        outline(g, classicHit[0] + 5, classicHit[1] + 5, classicHit[2] - 5, classicHit[3] - 5, WHITE);
        g.text(font, "X", closeHit[0] + 6, closeHit[1] + 5, WHITE, true);

        // ---- sidebar
        Category[] cats = Category.values();
        int ty = y1 + TITLE_H + 14;
        for (int i = 0; i <= cats.length; i++) {
            String label = i == TEAMS_VIEW ? "Teams" : cats[i].label;
            boolean active = i == view && search.isBlank();
            boolean hover = mx >= x1 + 4 && mx < cx - 4 && my >= ty - 4 && my < ty + 12;
            g.text(font, label, x1 + 12, ty, active ? CYAN : hover ? WHITE : DIM, true);
            tabHits.add(new int[]{x1 + 4, ty - 4, cx - 4, ty + 12});
            ty += 18;
        }
        int fy = y2 - 60;
        g.text(font, "Status", x1 + 12, fy, WHITE, true);
        g.text(font, enabledCount() + " active", x1 + 12, fy + 11, enabledCount() > 0 ? 0xFF55FF55 : RED, true);
        g.text(font, "Version", x1 + 12, fy + 27, WHITE, true);
        g.text(font, version(), x1 + 12, fy + 38, DIM, true);
        g.text(font, "Fabric", x1 + 12, fy + 49, DIM, true);

        // ---- content pane (leaves a hole for the search box)
        int px1 = cx, py1 = y1 + TITLE_H, px2 = x2, py2 = y2;
        g.fill(px1, py1, px2, py2, CONTENT_BG);
        g.fill(px1, py1, px1 + 1, py2, OUTLINE);
        g.fill(px1 + 1, py1, px2, py1 + 2, 0x55000000);
        outline(g, x1, y1, x2, y2, OUTLINE);
        outline(g, x1 + 1, y1 + 1, x2 - 1, y2 - 1, LIGHT_EDGE);
        outline(g, x1, y1, x2, y2, OUTLINE);

        // ---- header with trailing line
        List<Row> rows = buildRows();
        String title;
        if (view == TEAMS_VIEW) title = "Teams";
        else if (!search.isBlank()) title = "Search";
        else title = cats[view].label;
        int count = view == TEAMS_VIEW ? 0 : (int) rows.stream().filter(r -> r.kind == Kind.MODULE).count();
        String head = view == TEAMS_VIEW ? title : title + " [" + count + "]";
        g.text(font, head, cx + 10, py1 + 9, WHITE, true);
        g.fill(cx + 16 + font.width(head), py1 + 13, px2 - 10, py1 + 14, 0xFFE0E0E0);

        // ---- rows
        int visible = visibleRows();
        int maxScroll = Math.max(0, rows.size() - visible);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        int y = listTop();
        int rx1 = cx + 10, rx2 = px2 - 10;
        for (int i = scroll; i < rows.size() && i < scroll + visible; i++) {
            Row r = rows.get(i);
            int[] hit = {rx1, y, rx2, y + ROW_H - 2};
            boolean hover = inside(hit, mx, my);
            switch (r.kind) {
                case MODULE -> {
                    Module m = r.module;
                    g.fill(hit[0], hit[1], hit[2], hit[3], m == selected ? ROW_SELECTED : hover ? ROW_HOVER : ROW_BG);
                    g.fill(hit[0] + 4, hit[1] + 4, hit[0] + 14, hit[1] + 14, m.isEnabled() ? SWITCH_ON : 0xFF2B2B2B);
                    outline(g, hit[0] + 4, hit[1] + 4, hit[0] + 14, hit[1] + 14, OUTLINE);
                    g.text(font, m.name, hit[0] + 20, hit[1] + 6, m.isEnabled() ? WHITE : DIM, true);
                    String extra = !search.isBlank() ? m.category.label : m.tag();
                    if (!extra.isEmpty()) g.text(font, extra, hit[0] + 26 + font.width(m.name), hit[1] + 6, FAINT, true);
                    drawSwitch(g, hit[2] - 30, hit[1] + 4, m.isEnabled());
                }
                case SETTING -> {
                    g.fill(hit[0] + 10, hit[1], hit[2], hit[3], hover ? ROW_HOVER : 0xFF464646);
                    g.text(font, r.setting.name, hit[0] + 16, hit[1] + 6, DIM, true);
                    if (r.setting instanceof Setting.Bool b) drawSwitch(g, hit[2] - 30, hit[1] + 5, b.get());
                    else {
                        String v = r.setting.display();
                        g.text(font, v, hit[2] - 6 - font.width(v), hit[1] + 6, CYAN, true);
                    }
                }
                case LABEL -> g.text(font, r.text, hit[0] + 2, hit[1] + 6, DIM, true);
                case SWATCHES -> {
                    g.fill(hit[0], hit[1], hit[2], hit[3], ROW_BG);
                    g.text(font, r.text, hit[0] + 6, hit[1] + 6, WHITE, true);
                    int sx = hit[2] - 8 * 14 - 2;
                    for (int c = 0; c < TeamEspColor.VALUES.length; c++) {
                        int[] s = {sx + c * 14, hit[1] + 3, sx + c * 14 + 12, hit[1] + 15};
                        g.fill(s[0], s[1], s[2], s[3], 0xFF000000 | TeamEspColor.VALUES[c]);
                        boolean cur = TeamEspColor.VALUES[c] == TeamEspColor.rgb();
                        outline(g, s[0], s[1], s[2], s[3], cur ? WHITE : OUTLINE);
                        swatchHits.add(s);
                    }
                }
                case TEAM_TOGGLE -> {
                    g.fill(hit[0], hit[1], hit[2], hit[3], hover ? ROW_HOVER : ROW_BG);
                    g.text(font, r.text, hit[0] + 6, hit[1] + 6, WHITE, true);
                    drawSwitch(g, hit[2] - 30, hit[1] + 5, ToolkitConfig.INSTANCE.teamGlow);
                }
                case MEMBER -> {
                    g.fill(hit[0], hit[1], hit[2], hit[3], hover ? ROW_HOVER : ROW_BG);
                    g.text(font, r.text, hit[0] + 6, hit[1] + 6, WHITE, true);
                    g.text(font, "Remove", hit[2] - 6 - font.width("Remove"), hit[1] + 6, RED, true);
                }
                case NEARBY -> {
                    g.fill(hit[0], hit[1], hit[2], hit[3], hover ? ROW_HOVER : ROW_BG);
                    g.text(font, r.text, hit[0] + 6, hit[1] + 6, DIM, true);
                    g.text(font, "Add", hit[2] - 6 - font.width("Add"), hit[1] + 6, SWITCH_ON, true);
                }
            }
            rowHits.add(hit);
            rowTargets.add(r);
            y += ROW_H;
        }
        if (maxScroll > 0) {
            String pos = (scroll + 1) + "-" + Math.min(rows.size(), scroll + visible) + " / " + rows.size();
            g.text(font, pos, px2 - 10 - font.width(pos), py1 + 9, FAINT, true);
        }

        // ---- bottom bar
        int by = bottomBarY();
        int bw = Math.min(150, (contentW() - 30) / 2);
        greenHit = new int[]{px2 - 10 - bw * 2 - 8, by + 4, px2 - 10 - bw - 8, by + 4 + BTN_H};
        blueHit = new int[]{px2 - 10 - bw, by + 4, px2 - 10, by + 4 + BTN_H};
        if (view == TEAMS_VIEW) {
            button(g, greenHit, "Add", GREEN, GREEN_EDGE, true);
            button(g, blueHit, "Clear All", BLUE, BLUE_EDGE, true);
        } else {
            boolean has = selected != null;
            button(g, greenHit, has ? (selected.isEnabled() ? "Disable" : "Enable") : "Enable", GREEN, GREEN_EDGE, has);
            button(g, blueHit, has && expanded == selected ? "Hide Settings" : "Settings", BLUE, BLUE_EDGE, has);
        }
        String hint = !feedback.isEmpty() ? feedback
                : view == TEAMS_VIEW ? "Click a nearby player to add; click a member to remove."
                : "Left: toggle   Right: settings   Wheel: scroll   RShift: close";
        g.text(font, hint, cx + 10, by + 10, !feedback.isEmpty() ? 0xFFFFAA55 : FAINT, true);
    }

    // ----------------------------------------------------------------- input
    private void addTeammate(String name) {
        name = name.trim();
        if (!name.matches("[A-Za-z0-9_.-]{1,32}")) {
            feedback = "Use 1-32 characters: letters, numbers, . _ -";
            return;
        }
        ToolkitConfig cfg = ToolkitConfig.INSTANCE;
        if (cfg.teamMates == null) cfg.teamMates = new ArrayList<>();
        if (inTeam(name)) {
            feedback = "Already in your team";
            return;
        }
        if (cfg.teamMates.size() >= MAX_TEAM) {
            feedback = "Team list is full (" + MAX_TEAM + ")";
            return;
        }
        cfg.teamMates.add(name);
        feedback = "";
        cfg.save();
        TeamNames.refresh();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double x = event.x(), y = event.y();
        int button = event.button();

        if (inside(closeHit, x, y)) { onClose(); return true; }
        if (inside(classicHit, x, y)) {
            if (minecraft != null) minecraft.gui.setScreen(new ToolkitScreen(this));
            return true;
        }
        for (int i = 0; i < tabHits.size(); i++) {
            if (inside(tabHits.get(i), x, y)) {
                search = "";
                if (i == view) rebuildWidgets(); else switchView(i);
                return true;
            }
        }
        if (inside(greenHit, x, y)) {
            if (view == TEAMS_VIEW) {
                addTeammate(teamInput);
                if (feedback.isEmpty()) { teamInput = ""; rebuildWidgets(); }
            } else if (selected != null) selected.toggle();
            return true;
        }
        if (inside(blueHit, x, y)) {
            if (view == TEAMS_VIEW) {
                ToolkitConfig cfg = ToolkitConfig.INSTANCE;
                if (cfg.teamMates != null) cfg.teamMates.clear();
                cfg.save();
                TeamNames.refresh();
            } else if (selected != null) expanded = expanded == selected ? null : selected;
            return true;
        }
        for (int i = 0; i < swatchHits.size(); i++) {
            if (inside(swatchHits.get(i), x, y)) {
                TeamEspColor.setPreset(i % TeamEspColor.VALUES.length);
                return true;
            }
        }
        for (int i = 0; i < rowHits.size(); i++) {
            if (!inside(rowHits.get(i), x, y)) continue;
            Row r = rowTargets.get(i);
            switch (r.kind) {
                case MODULE -> {
                    selected = r.module;
                    if (button == 0) r.module.toggle();
                    else if (button == 1) expanded = expanded == r.module ? null : r.module;
                }
                case SETTING -> {
                    r.setting.step(button != 1);
                    ModuleManager.save();
                }
                case TEAM_TOGGLE -> {
                    ToolkitConfig.INSTANCE.teamGlow = !ToolkitConfig.INSTANCE.teamGlow;
                    ToolkitConfig.INSTANCE.save();
                }
                case MEMBER -> {
                    ToolkitConfig.INSTANCE.teamMates.removeIf(s -> s.equalsIgnoreCase(r.name));
                    ToolkitConfig.INSTANCE.save();
                    TeamNames.refresh();
                }
                case NEARBY -> addTeammate(r.name);
                default -> { }
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll += scrollY > 0 ? -2 : 2;
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
