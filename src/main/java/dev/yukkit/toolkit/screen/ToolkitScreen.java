package dev.yukkit.toolkit.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.YukkitToolkitClient;
import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.camera.PerspectiveController;
import dev.yukkit.toolkit.performance.PerformanceController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;

import java.util.ArrayList;
import java.util.Locale;

public final class ToolkitScreen extends Screen {
    private static final int BG = 0xFF0C1019;
    private static final int SIDEBAR = 0xFF141C2A;
    private static final int BLUE = 0xFF006EFE;
    private static final int FG = 0xFFF5F7FC;
    private static final int MUTED = 0xFF9AA6BE;
    private static final String[] TABS = {"Visual", "Camera", "Mods", "Team", "Style", "World"};
    private static int tab = 0;
    private static int teamOffset = 0;

    private int x, y, panelWidth, panelHeight;
    private EditBox nameBox;
    private String message = "";

    public ToolkitScreen() { super(Component.literal("Yukkit Toolkit")); }

    @Override
    protected void init() {
        panelWidth = Math.min(580, width - 16);
        panelHeight = Math.min(380, height - 16);
        x = (width - panelWidth) / 2;
        y = (height - panelHeight) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int target = i;
            addButton(x + 11, y + 60 + i * 33, 105, 26,
                    (tab == i ? " > " : "   ") + TABS[i], () -> changeTab(target));
        }
        int right = x + 145;
        int bw = Math.max(90, panelWidth - 164);
        int top = y + 67;
        ToolkitConfig cfg = ToolkitConfig.INSTANCE;
        Minecraft mc = Minecraft.getInstance();

        switch (tab) {
            case 0 -> {
                addButton(right, top, bw, 25, "Time: " + cfg.timeLabel(), () -> {
                    cfg.localTime = (cfg.localTime + 1) % 5; cfg.save(); refresh();
                });
                addButton(right, top + 36, bw, 25, "Weather: " + cfg.weatherLabel(), () -> {
                    cfg.localWeather = (cfg.localWeather + 1) % 3; cfg.save(); refresh();
                });
                addButton(right, top + 72, bw, 25, "Fullbright: " + onOff(cfg.fullbright), () -> {
                    cfg.fullbright = !cfg.fullbright; cfg.save(); refresh();
                });
            }
            case 1 -> {
                addButton(right, top, bw, 25, "Freecam: " + onOff(FreecamController.isEnabled()), () -> {
                    FreecamController.toggle(mc); refresh();
                });
                addButton(right, top + 36, bw, 25, "Perspective: " + onOff(PerspectiveController.isEnabled()), () -> {
                    PerspectiveController.toggle(mc); refresh();
                });
                addButton(right, top + 72, bw, 25, "Reset Camera", () -> {
                    FreecamController.disable(); PerspectiveController.disable(); refresh();
                });
            }
            case 2 -> {
                addButton(right, top, bw, 25, "Keystrokes: " + onOff(cfg.keystrokes), () -> {
                    cfg.keystrokes = !cfg.keystrokes; cfg.save(); refresh();
                });
                addButton(right, top + 36, bw, 25, "TNT Timer: " + onOff(cfg.tntTimer), () -> {
                    cfg.tntTimer = !cfg.tntTimer; cfg.save(); refresh();
                });
                addButton(right, top + 72, bw, 25, "Lightweight Graphics: " + onOff(PerformanceController.isEnabled()), () -> {
                    PerformanceController.toggle(mc); refresh();
                });
                addButton(right, top + 108, bw, 25, "Team HUD: " + onOff(cfg.teamHud), () -> {
                    cfg.teamHud = !cfg.teamHud; cfg.save(); refresh();
                });
            }
            case 3 -> {
                int fieldW = Math.max(65, bw - 78);
                nameBox = new EditBox(font, right, top, fieldW, 24, Component.literal("Minecraft nickname"));
                nameBox.setMaxLength(16);
                nameBox.setHint(Component.literal("Player nickname"));
                addRenderableWidget(nameBox);
                addButton(right + fieldW + 6, top, 66, 24, "Add", this::addFriend);
                addButton(right, top + 38, bw, 24, "Glow nearby: " + onOff(cfg.teamGlow), () -> {
                    cfg.teamGlow = !cfg.teamGlow; cfg.save(); refresh();
                });
                if (cfg.teamMates == null) cfg.teamMates = new ArrayList<>();
                teamOffset = Math.min(teamOffset, Math.max(0, cfg.teamMates.size() - 4));
                for (int i = teamOffset; i < Math.min(teamOffset + 4, cfg.teamMates.size()); i++) {
                    final int index = i;
                    addButton(right, top + 72 + (i - teamOffset) * 30, bw - 58, 24, cfg.teamMates.get(i), () -> {});
                    addButton(right + bw - 53, top + 72 + (i - teamOffset) * 30, 53, 24, "Remove", () -> {
                        cfg.teamMates.remove(index); cfg.save(); refresh();
                    });
                }
                addButton(right, top + 201, bw / 2 - 4, 22, "Previous", () -> {
                    teamOffset = Math.max(0, teamOffset - 4); refresh();
                });
                addButton(right + bw / 2 + 4, top + 201, bw / 2 - 4, 22, "Next", () -> {
                    teamOffset = Math.min(Math.max(0, cfg.teamMates.size() - 4), teamOffset + 4); refresh();
                });
            }
            case 4 -> {
                addButton(right, top, bw, 25, "Wave (vanilla arm animation)", () -> {
                    if (mc.player != null) mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                    closePanel();
                });
            }
            case 5 -> addButton(right, top, bw, 26,
                    YukkitToolkitClient.WORLD_CAPTURE.isCapturing() ? "Stop World Download" : "Start World Download",
                    () -> {
                        if (YukkitToolkitClient.WORLD_CAPTURE.isCapturing()) {
                            YukkitToolkitClient.WORLD_CAPTURE.stop();
                            YukkitToolkitClient.notifyPlayer("World capture stopped.");
                        } else YukkitToolkitClient.WORLD_CAPTURE.start(mc);
                        refresh();
                    });
            default -> {}
        }
        addButton(x + panelWidth - 34, y + 10, 22, 20, "X", this::closePanel);
    }

    private void addFriend() {
        if (nameBox == null) return;
        String name = nameBox.getValue().trim();
        if (!name.matches("[A-Za-z0-9_]{3,16}")) {
            message = "Use a valid Minecraft Java nickname.";
            return;
        }
        ToolkitConfig cfg = ToolkitConfig.INSTANCE;
        if (cfg.teamMates == null) cfg.teamMates = new ArrayList<>();
        if (cfg.teamMates.stream().anyMatch(v -> v.equalsIgnoreCase(name))) {
            message = "Already on the list.";
            return;
        }
        if (cfg.teamMates.size() >= 64) {
            message = "Team list limit is 64 players.";
            return;
        }
        cfg.teamMates.add(name);
        cfg.save();
        refresh();
    }

    private static String onOff(boolean value) { return value ? "ON" : "OFF"; }

    private void addButton(int bx, int by, int w, int h, String title, Runnable action) {
        if (w < 10 || by + h > y + panelHeight - 29) return;
        addRenderableWidget(Button.builder(Component.literal(title), ignored -> action.run())
                .bounds(bx, by, w, h).build());
    }

    private void changeTab(int next) { tab = next; refresh(); }
    private void refresh() { if (minecraft != null) minecraft.gui.setScreen(new ToolkitScreen()); }
    private void closePanel() { if (minecraft != null) minecraft.gui.setScreen(null); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float dt) {
        graphics.fill(0, 0, width, height, 0xB5000000);
        graphics.fill(x, y, x + panelWidth, y + panelHeight, BG);
        graphics.fill(x, y, x + 4, y + panelHeight, BLUE);
        graphics.fill(x + 4, y + 45, x + 128, y + panelHeight, SIDEBAR);
        graphics.text(font, "YUKKIT", x + 15, y + 15, FG, true);
        graphics.text(font, "TOOLKIT", x + 62, y + 15, 0xFF71AEFF, true);
        graphics.text(font, "MODULAR CLIENT UTILITIES", x + 145, y + 25, MUTED, true);

        int px = x + 145, py = y + 48;
        graphics.text(font, TABS[tab].toUpperCase(Locale.ROOT), px, py, FG, true);
        switch (tab) {
            case 1 -> {
                graphics.text(font, "Freecam: WASD / Space / Shift / Sprint", px, y + 202, MUTED, true);
                graphics.text(font, "Perspective: press V in-game", px, y + 216, MUTED, true);
            }
            case 2 -> {
                graphics.text(font, "Low FX changes vanilla graphics settings.", px, y + 252, MUTED, true);
                graphics.text(font, "Toggle again to restore your settings.", px, y + 266, MUTED, true);
            }
            case 3 -> {
                graphics.text(font, "Only nearby loaded players (up to 32m).", px, y + 313, MUTED, true);
                graphics.text(font, "No remote tracking or server data access.", px, y + 327, MUTED, true);
            }
            case 4 -> {
                graphics.text(font, "Vanilla wave is a single arm swing.", px, y + 123, MUTED, true);
                graphics.text(font, "Custom cape sync & advanced emotes: planned.", px, y + 147, MUTED, true);
                graphics.text(font, "Other players need a compatible sync service.", px, y + 161, MUTED, true);
            }
            case 5 -> {
                graphics.text(font, "Experimental: captures loaded blocks only.", px, y + 118, MUTED, true);
                graphics.text(font, "Saved chunks: " + YukkitToolkitClient.WORLD_CAPTURE.getSavedChunks(),
                        px, y + 136, FG, true);
                graphics.text(font, "No inventories, entities or hidden chunks.", px, y + 156, MUTED, true);
            }
            default -> {}
        }
        if (!message.isEmpty()) graphics.text(font, message, px, y + panelHeight - 44, 0xFFFFBE63, true);
        graphics.fill(x + 8, y + panelHeight - 25, x + panelWidth - 8, y + panelHeight - 24, 0xFF273248);
        graphics.text(font, "YukkitMC / 26.3  /  alpha", x + 14, y + panelHeight - 16, MUTED, true);
        super.extractRenderState(graphics, mx, my, dt);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RSHIFT) {
            closePanel();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
