package dev.yukkit.toolkit.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.modules.framework.Module;
import dev.yukkit.toolkit.modules.framework.ModuleCategory;
import dev.yukkit.toolkit.modules.framework.ModuleRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Original Yukkit ClickGUI implementation.
 *
 * Visual direction is credited to LiquidBounce's ClickGUI:
 * https://github.com/CCBlueX/LiquidBounce
 *
 * This class is an independent Java implementation and does not copy
 * LiquidBounce source code or assets.
 */
public final class ClickGuiScreen extends Screen {
    private static final int[] ACCENTS = {0xFF8B5CF6, 0xFF3B82F6, 0xFF14B8A6};
    private final Screen parent;
    private final List<HitBox> hitBoxes = new ArrayList<>();
    private ModuleCategory selectedCategory;
    private EditBox searchBox;
    private int page;
    private int theme;
    private int accentIndex;

    private record HitBox(int x, int y, int width, int height, Runnable action) {
        boolean contains(double px, double py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("Yukkit ClickGUI"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int panelX = Math.max(12, (width - 820) / 2);
        int panelY = Math.max(12, (height - 500) / 2);
        searchBox = new EditBox(font, panelX + 190, panelY + 48, 280, 20, Component.literal("Search modules"));
        searchBox.setHint(Component.literal("Search modules..."));
        searchBox.setMaxLength(48);
        addRenderableWidget(searchBox);
        rebuildHitBoxes();
    }

    private int panelX() { return Math.max(12, (width - 820) / 2); }
    private int panelY() { return Math.max(12, (height - 500) / 2); }
    private int panelWidth() { return Math.min(820, width - 24); }
    private int panelHeight() { return Math.min(500, height - 24); }

    private List<Module> visibleModules() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        return ModuleRegistry.all().stream()
                .filter(m -> selectedCategory == null || m.category() == selectedCategory)
                .filter(m -> query.isEmpty()
                        || m.name().toLowerCase(Locale.ROOT).contains(query)
                        || m.description().toLowerCase(Locale.ROOT).contains(query)
                        || m.id().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private void rebuildHitBoxes() {
        hitBoxes.clear();
        int x = panelX(), y = panelY();
        int sideX = x + 16;
        int sideY = y + 105;
        int index = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            final ModuleCategory target = category;
            int rowY = sideY + index * 29;
            hitBoxes.add(new HitBox(sideX, rowY, 154, 25, () -> {
                selectedCategory = selectedCategory == target ? null : target;
                page = 0;
                rebuildHitBoxes();
            }));
            index++;
        }
        hitBoxes.add(new HitBox(x + panelWidth() - 104, y + 15, 88, 25, () -> {
            theme = (theme + 1) % 2;
            rebuildHitBoxes();
        }));
        hitBoxes.add(new HitBox(x + panelWidth() - 206, y + 15, 88, 25, () -> {
            accentIndex = (accentIndex + 1) % ACCENTS.length;
            rebuildHitBoxes();
        }));

        List<Module> modules = visibleModules();
        int pageSize = 8;
        int pages = Math.max(1, (modules.size() + pageSize - 1) / pageSize);
        page = Math.min(page, pages - 1);
        int start = page * pageSize;
        for (int i = start; i < Math.min(modules.size(), start + pageSize); i++) {
            Module module = modules.get(i);
            int slot = i - start;
            int col = slot % 2;
            int row = slot / 2;
            int cardX = x + 190 + col * 300;
            int cardY = y + 91 + row * 78;
            hitBoxes.add(new HitBox(cardX + 238, cardY + 14, 42, 22, () -> {
                module.toggle();
                rebuildHitBoxes();
            }));
        }
        hitBoxes.add(new HitBox(x + panelWidth() - 210, y + panelHeight() - 43, 92, 25, () -> {
            page = Math.max(0, page - 1);
            rebuildHitBoxes();
        }));
        hitBoxes.add(new HitBox(x + panelWidth() - 108, y + panelHeight() - 43, 92, 25, () -> {
            page = Math.min(pages - 1, page + 1);
            rebuildHitBoxes();
        }));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = panelX(), y = panelY(), w = panelWidth(), h = panelHeight();
        int bg = theme == 0 ? 0xF012141B : 0xF0F4F5F8;
        int surface = theme == 0 ? 0xFF1C202B : 0xFFFFFFFF;
        int surfaceAlt = theme == 0 ? 0xFF252B38 : 0xFFE9ECF2;
        int border = theme == 0 ? 0xFF343B4B : 0xFFD4D9E3;
        int primary = theme == 0 ? 0xFFF4F5FA : 0xFF171923;
        int muted = theme == 0 ? 0xFF9BA4B5 : 0xFF697386;
        int accent = ACCENTS[accentIndex];

        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + 176, y + h, surface);
        g.fill(x, y, x + w, y + 3, accent);
        g.fill(x + 175, y + 1, x + 176, y + h - 1, border);

        g.text(font, "YUKKIT", x + 18, y + 18, accent, true);
        g.text(font, "CLIENT TOOLKIT", x + 18, y + 33, muted, false);
        g.text(font, "MODULES", x + 18, y + 83, muted, true);

        int sideY = y + 105;
        int idx = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            int rowY = sideY + idx * 29;
            boolean active = selectedCategory == category;
            if (active) g.fill(x + 16, rowY, x + 170, rowY + 25, surfaceAlt);
            g.text(font, categoryLabel(category), x + 27, rowY + 8,
                    active ? accent : primary, active);
            idx++;
        }
        g.text(font, "All categories", x + 27, y + h - 28,
                selectedCategory == null ? accent : muted, selectedCategory == null);

        g.text(font, "ClickGUI", x + 190, y + 19, primary, true);
        g.text(font, "Independent Yukkit implementation", x + 190, y + 32, muted, false);
        g.fill(x + w - 206, y + 15, x + w - 118, y + 40, surfaceAlt);
        g.fill(x + w - 104, y + 15, x + w - 16, y + 40, surfaceAlt);
        g.text(font, "ACCENT", x + w - 187, y + 24, primary, true);
        g.text(font, theme == 0 ? "DARK" : "LIGHT", x + w - 85, y + 24, primary, true);

        g.text(font, visibleModules().size() + " modules", x + 190, y + 76, muted, false);
        List<Module> modules = visibleModules();
        int pageSize = 8;
        int start = page * pageSize;
        for (int i = start; i < Math.min(modules.size(), start + pageSize); i++) {
            Module module = modules.get(i);
            int slot = i - start;
            int col = slot % 2, row = slot / 2;
            int cx = x + 190 + col * 300;
            int cy = y + 91 + row * 78;
            g.fill(cx, cy, cx + 286, cy + 66, surface);
            g.fill(cx, cy, cx + 3, cy + 66, module.isEnabled() ? accent : border);
            g.text(font, module.name(), cx + 13, cy + 13, primary, true);
            String description = module.description().isBlank() ? module.category().name() : module.description();
            g.text(font, trim(description, 35), cx + 13, cy + 31, muted, false);
            g.fill(cx + 238, cy + 14, cx + 280, cy + 36,
                    module.isEnabled() ? accent : surfaceAlt);
            g.text(font, module.isEnabled() ? "ON" : "OFF", cx + 249, cy + 21,
                    module.isEnabled() || theme == 0 ? 0xFFFFFFFF : muted, true);
        }
        int pages = Math.max(1, (modules.size() + pageSize - 1) / pageSize);
        g.text(font, "LiquidBounce ClickGUI visual inspiration — credits: CCBlueX / LiquidBounce",
                x + 190, y + h - 24, muted, false);
        g.fill(x + w - 210, y + h - 43, x + w - 118, y + h - 18, surfaceAlt);
        g.fill(x + w - 108, y + h - 43, x + w - 16, y + h - 18, surfaceAlt);
        g.text(font, "PREVIOUS", x + w - 197, y + h - 34, primary, true);
        g.text(font, "NEXT  " + (page + 1) + "/" + pages, x + w - 96, y + h - 34, primary, true);
        if (searchBox != null) searchBox.extractRenderState(g, mouseX, mouseY, delta);
        rebuildHitBoxes();
    }

    private static String trim(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    private static String categoryLabel(ModuleCategory category) {
        return switch (category) {
            case COMBAT -> "Combat";
            case PLAYER -> "Player";
            case MOVEMENT -> "Movement";
            case RENDER -> "Render";
            case WORLD -> "World";
            case MISC -> "Misc";
            case EXPLOIT -> "Exploit";
            case FUN -> "Fun";
            case AUTOMATION -> "Automation";
        };
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (HitBox hitBox : List.copyOf(hitBoxes)) {
                if (hitBox.contains(event.x(), event.y())) {
                    hitBox.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RSHIFT) {
            onClose();
            return true;
        }
        boolean handled = super.keyPressed(event);
        rebuildHitBoxes();
        return handled;
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
