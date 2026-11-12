package dev.yukkit.toolkit.screen;

import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.YukkitToolkitClient;
import dev.yukkit.toolkit.camera.FreecamController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ToolkitScreen extends Screen {
    private static final int BG = 0xFF0C1019;
    private static final int SIDEBAR = 0xFF141C2A;
    private static final int BLUE = 0xFF006EFE;
    private static final int FG = 0xFFF5F7FC;
    private static final int MUTED = 0xFF9AA6BE;
    private static int currentCategory = 0;

    private int x;
    private int y;
    private int panelWidth;
    private int panelHeight;

    public ToolkitScreen() {
        super(Text.literal("Yukkit Toolkit"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(510, width - 24);
        panelHeight = Math.min(345, height - 24);
        x = (width - panelWidth) / 2;
        y = (height - panelHeight) / 2;

        String[] tabs = {"Visual", "Camera", "World"};
        int sidebar = 125;
        for (int i = 0; i < tabs.length; i++) {
            final int tab = i;
            addButton(x + 12, y + 63 + i * 31, sidebar - 24, 24,
                    (currentCategory == i ? "[ " : "") + tabs[i] + (currentCategory == i ? " ]" : ""),
                    () -> switchCategory(tab));
        }

        int right = x + sidebar + 18;
        int buttonWidth = Math.max(130, panelWidth - sidebar - 36);
        int line = y + 66;
        ToolkitConfig config = ToolkitConfig.INSTANCE;
        MinecraftClient client = MinecraftClient.getInstance();

        if (currentCategory == 0) {
            addButton(right, line, buttonWidth, 25, "Time: " + config.timeLabel(), () -> {
                config.localTime = (config.localTime + 1) % 5;
                config.save();
                refresh();
            });
            addButton(right, line + 35, buttonWidth, 25, "Weather: " + config.weatherLabel(), () -> {
                config.localWeather = (config.localWeather + 1) % 3;
                config.save();
                refresh();
            });
            addButton(right, line + 70, buttonWidth, 25,
                    "Fullbright: " + (config.fullbright ? "ON" : "OFF"), () -> {
                config.fullbright = !config.fullbright;
                config.save();
                refresh();
            });
        } else if (currentCategory == 1) {
            addButton(right, line, buttonWidth, 25,
                    "Freecam: " + (FreecamController.isEnabled() ? "ON" : "OFF"), () -> {
                FreecamController.toggle(client);
                refresh();
            });
            addButton(right, line + 41, buttonWidth, 25, "Exit Freecam", () -> {
                FreecamController.disable();
                refresh();
            });
        } else {
            addButton(right, line, buttonWidth, 25,
                    YukkitToolkitClient.WORLD_CAPTURE.isCapturing() ? "Stop World Download" : "Start World Download",
                    () -> {
                if (YukkitToolkitClient.WORLD_CAPTURE.isCapturing()) {
                    YukkitToolkitClient.WORLD_CAPTURE.stop();
                    YukkitToolkitClient.notifyPlayer("World capture stopped.");
                } else {
                    YukkitToolkitClient.WORLD_CAPTURE.start(client);
                }
                refresh();
            });
        }
        addButton(x + panelWidth - 35, y + 9, 23, 20, "X", this::close);
    }

    private void addButton(int bx, int by, int w, int h, String label, Runnable click) {
        if (by + h > y + panelHeight - 28) return;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> click.run())
                .dimensions(bx, by, w, h).build());
    }

    private void switchCategory(int tab) {
        currentCategory = tab;
        refresh();
    }

    private void refresh() {
        if (client != null) client.setScreen(new ToolkitScreen());
    }

    private void close() {
        if (client != null) client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xBA000000);
        context.fill(x, y, x + panelWidth, y + panelHeight, BG);
        context.fill(x, y, x + 4, y + panelHeight, BLUE);
        context.fill(x + 4, y + 45, x + 125, y + panelHeight, SIDEBAR);

        context.drawTextWithShadow(textRenderer, "YUKKIT", x + 17, y + 14, FG);
        context.drawTextWithShadow(textRenderer, "TOOLKIT", x + 62, y + 14, 0xFF67AAFF);
        context.drawTextWithShadow(textRenderer, "Client-side utilities", x + 17, y + 31, MUTED);

        int sx = x + 145;
        String[] titles = {"Visual settings", "Camera tools", "World Downloader"};
        context.drawTextWithShadow(textRenderer, titles[currentCategory], sx, y + 48, FG);

        if (currentCategory == 0) {
            context.drawTextWithShadow(textRenderer, "Changes affect your client only.", sx, y + 185, MUTED);
            context.drawTextWithShadow(textRenderer, "Server time and weather remain unchanged.", sx, y + 199, MUTED);
        } else if (currentCategory == 1) {
            context.drawTextWithShadow(textRenderer, "WASD: fly  |  Space/Shift: height", sx, y + 158, MUTED);
            context.drawTextWithShadow(textRenderer, "Mouse: look  |  Sprint: faster", sx, y + 172, MUTED);
            context.drawTextWithShadow(textRenderer, "Your player stays in place.", sx, y + 186, MUTED);
        } else {
            context.drawTextWithShadow(textRenderer, "Experimental overworld capture.", sx, y + 118, MUTED);
            context.drawTextWithShadow(textRenderer,
                    "Chunks saved: " + YukkitToolkitClient.WORLD_CAPTURE.getSavedChunks(), sx, y + 136, FG);
            context.drawTextWithShadow(textRenderer, "No inventories or unseen chunks.", sx, y + 154, MUTED);
            context.drawTextWithShadow(textRenderer, "Follow server rules before recording.", sx, y + 172, MUTED);
        }

        context.fill(x + 8, y + panelHeight - 26, x + panelWidth - 8, y + panelHeight - 25, 0xFF273248);
        context.drawTextWithShadow(textRenderer, "YukkitMC  /  v0.1.0-alpha.1", x + 17,
                y + panelHeight - 17, MUTED);
        context.drawTextWithShadow(textRenderer, "RSHIFT TO TOGGLE", x + panelWidth - 130,
                y + panelHeight - 17, MUTED);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
