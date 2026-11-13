package dev.yukkit.toolkit.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukkit.toolkit.ToolkitConfig;
import dev.yukkit.toolkit.YukkitToolkitClient;
import dev.yukkit.toolkit.camera.FreecamController;
import dev.yukkit.toolkit.camera.PerspectiveController;
import dev.yukkit.toolkit.cosmetics.CapeCosmetics;
import dev.yukkit.toolkit.map.ToolkitMap;
import dev.yukkit.toolkit.performance.PerformanceController;
import dev.yukkit.toolkit.modules.XrayController;
import dev.yukkit.toolkit.render.ChestCandidates;
import dev.yukkit.toolkit.team.TeamEspColor;
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
import java.util.List;
import java.util.Locale;

/**
 * Vanilla GUI presentation. All interactive controls use Minecraft's native
 * Button/EditBox widgets and the default Screen backdrop. The map is the
 * only custom-rendered section; there are no hand-drawn panels or button skins.
 */
public final class ToolkitScreen extends Screen {
    public enum Page { HOME, VISUAL, CAMERA, MODS, STORAGE, AUTOMATION, TEAM, TEAM_COLOR, STYLE, WORLD, MAP }

    private final Screen parent;
    private final Page page;
    private EditBox nameBox;
    private EditBox customColorBox;
    private String feedback = "";
    private int teamPage = 0;

    public ToolkitScreen() {
        this(null, Page.HOME);
    }

    public ToolkitScreen(Screen parent) {
        this(parent, Page.HOME);
    }

    private ToolkitScreen(Screen parent, Page page) {
        super(Component.literal(page == Page.HOME ? "Yukkit Tools" : "Yukkit Tools - " + pretty(page)));
        this.parent = parent;
        this.page = page;
    }

    private static String pretty(Page page) {
        return switch (page) {
            case HOME -> "Home";
            case VISUAL -> "Visual";
            case CAMERA -> "Camera";
            case MODS -> "Mods";
            case STORAGE -> "Storage ESP";
            case AUTOMATION -> "Automation";
            case TEAM -> "Team";
            case TEAM_COLOR -> "Team Color";
            case STYLE -> "Style";
            case WORLD -> "World";
            case MAP -> "Map";
        };
    }

    @Override
    protected void init() {
        super.init();
        int left = width / 2 - 154;
        int right = width / 2 + 4;
        int top = Math.max(46, height / 2 - 108);
        int pairWidth = 150;
        ToolkitConfig config = ToolkitConfig.INSTANCE;

        if (page == Page.HOME) {
            nativeButton(left, top, pairWidth, "Visual", () -> open(Page.VISUAL));
            nativeButton(right, top, pairWidth, "Camera", () -> open(Page.CAMERA));
            nativeButton(left, top + 25, pairWidth, "Mods", () -> open(Page.MODS));
            nativeButton(right, top + 25, pairWidth, "Team", () -> open(Page.TEAM));
            nativeButton(left, top + 50, pairWidth, "Cosmetics", () -> open(Page.STYLE));
            nativeButton(right, top + 50, pairWidth, "World", () -> open(Page.WORLD));
            nativeButton(left, top + 75, pairWidth, "Map", () -> open(Page.MAP));
            nativeButton(right, top + 75, pairWidth, "Automation", () -> open(Page.AUTOMATION));
        } else if (page == Page.VISUAL) {
            nativeButton(left, top, pairWidth, "Time: " + config.timeLabel(), () -> {
                config.localTime = (config.localTime + 1) % 5;
                change();
            });
            nativeButton(right, top, pairWidth, "Weather: " + config.weatherLabel(), () -> {
                config.localWeather = (config.localWeather + 1) % 3;
                change();
            });
            nativeButton(left, top + 25, pairWidth, "Fullbright: " + label(config.fullbright), () -> {
                config.fullbright = !config.fullbright;
                change();
            });
        } else if (page == Page.CAMERA) {
            nativeButton(left, top, pairWidth, "Freecam: " + label(FreecamController.isEnabled()), () -> {
                FreecamController.toggle(Minecraft.getInstance());
                change();
            });
            nativeButton(right, top, pairWidth, "Perspective: " + label(PerspectiveController.isEnabled()), () -> {
                PerspectiveController.toggle(Minecraft.getInstance());
                change();
            });
            nativeButton(left, top + 25, 308, "Reset Camera", () -> {
                FreecamController.disable();
                PerspectiveController.disable();
                change();
            });
        } else if (page == Page.MODS) {
            nativeButton(left, top, pairWidth, "Keystrokes: " + label(config.keystrokes), () -> {
                config.keystrokes = !config.keystrokes;
                change();
            });
            nativeButton(right, top, pairWidth, "TNT Timer: " + label(config.tntTimer), () -> {
                config.tntTimer = !config.tntTimer;
                change();
            });
            nativeButton(left, top + 25, pairWidth, "Team HUD: " + label(config.teamHud), () -> {
                config.teamHud = !config.teamHud;
                change();
            });
            nativeButton(right, top + 25, pairWidth, "Light Graphics: " + label(PerformanceController.isEnabled()), () -> {
                PerformanceController.toggle(Minecraft.getInstance());
                change();
            });
            nativeButton(left, top + 50, pairWidth, "Auto Totem: " + label(config.autoTotem), () -> {
                config.autoTotem = !config.autoTotem;
                change();
            });
            nativeButton(right, top + 50, pairWidth, "X-Ray Ores: " + label(config.xray), () -> {
                dev.yukkit.toolkit.modules.XrayController.toggle(Minecraft.getInstance());
                rebuildWidgets();
            });
            nativeButton(left, top + 75, pairWidth, "Chest Model: " + label(config.chestXray), () -> {
                XrayController.toggleChest(Minecraft.getInstance());
                rebuildWidgets();
            });
            nativeButton(right, top + 75, pairWidth, "Storage ESP Options...", () -> open(Page.STORAGE));
            nativeButton(left, top + 100, 308, "Item ESP: " + label(config.itemEsp), () -> {
                config.itemEsp = !config.itemEsp;
                change();
            });
        } else if (page == Page.STORAGE) {
            nativeButton(left, top, pairWidth, "Chest: " + label(config.storageChest), () -> {
                config.storageChest = !config.storageChest; change();
            });
            nativeButton(right, top, pairWidth, "Trapped Chest: " + label(config.storageTrapped), () -> {
                config.storageTrapped = !config.storageTrapped; change();
            });
            nativeButton(left, top + 25, pairWidth, "Ender Chest: " + label(config.storageEnder), () -> {
                config.storageEnder = !config.storageEnder; change();
            });
            nativeButton(right, top + 25, pairWidth, "Radius: " + config.storageRangeChunks + " chunks", () -> {
                config.storageRangeChunks = switch(config.storageRangeChunks) {
                    case 4 -> 8;
                    case 8 -> 16;
                    case 16 -> 32;
                    default -> 4;
                };
                change();
            });
            nativeButton(left, top + 50, pairWidth, "Tracers: " + label(config.storageTracers), () -> {
                config.storageTracers = !config.storageTracers; change();
            });
            nativeButton(right, top + 50, pairWidth, "Tracer Color", () -> {
                config.storageTracerColor = switch(config.storageTracerColor) {
                    case 0x006EFE -> 0xFF9A33;
                    case 0xFF9A33 -> 0xB05CFF;
                    case 0xB05CFF -> 0x27DD7D;
                    default -> 0x006EFE;
                };
                change();
            });
            nativeButton(left, top + 75, pairWidth, "Shulker Markers: " + label(config.storageShulkers), () -> {
                config.storageShulkers = !config.storageShulkers; change();
            });
            nativeButton(right, top + 75, pairWidth, "Hopper Markers: " + label(config.storageHoppers), () -> {
                config.storageHoppers = !config.storageHoppers; change();
            });
        } else if (page == Page.AUTOMATION) {
            nativeButton(left, top, pairWidth, "Auto Clicker: " + label(config.autoClicker), () -> {
                config.autoClicker = !config.autoClicker;
                change();
            });
            nativeButton(right, top, pairWidth, "CPS: " + config.clickCps, () -> {
                config.clickCps = config.clickCps >= 12 ? 4 : config.clickCps + 2;
                change();
            });
            nativeButton(left, top + 25, pairWidth, "Auto Miner: " + label(config.autoMiner), () -> {
                config.autoMiner = !config.autoMiner;
                change();
            });
            nativeButton(right, top + 25, pairWidth, "Mace Swap: " + label(config.maceSwap), () -> {
                config.maceSwap = !config.maceSwap;
                change();
            });
            nativeButton(left, top + 50, pairWidth, "Auto Eat: " + label(config.autoEat), () -> {
                config.autoEat = !config.autoEat;
                change();
            });
            nativeButton(right, top + 50, pairWidth, "Eat hunger <= " + config.eatThreshold, () -> {
                config.eatThreshold = switch (config.eatThreshold) {
                    case 10 -> 14;
                    case 14 -> 16;
                    default -> 10;
                };
                change();
            });
            nativeButton(left, top + 75, 308, "Auto Respawn: " + label(config.autoRespawn), () -> {
                config.autoRespawn = !config.autoRespawn;
                change();
            });
        } else if (page == Page.TEAM) {
            nameBox = new EditBox(font, left, top, 232, 20, Component.literal("Minecraft player name"));
            nameBox.setMaxLength(32);
            nameBox.setHint(Component.literal("Player name (offline names allowed)"));
            addRenderableWidget(nameBox);
            nativeButton(left + 238, top, 70, "Add", this::addTeammate);
            nativeButton(left, top + 25, pairWidth, "Glow: " + label(config.teamGlow), () -> {
                config.teamGlow = !config.teamGlow;
                change();
            });
            nativeButton(right, top + 25, pairWidth, "Glow Color: " + TeamEspColor.name(config.teamGlowColor),
                    () -> open(Page.TEAM_COLOR));
            List<String> names = config.teamMates == null ? List.of() : config.teamMates;
            int visible = Math.min(3, Math.max(0, names.size() - teamPage * 3));
            for (int i = 0; i < visible; i++) {
                final int index = teamPage * 3 + i;
                nativeButton(right + 100, top + 54 + i * 25, 50, "X", () -> {
                    config.teamMates.remove(index);
                    change();
                });
            }
            nativeButton(left, top + 137, pairWidth, "Previous", () -> {
                teamPage = Math.max(0, teamPage - 1);
                rebuildWidgets();
            });
            nativeButton(right, top + 137, pairWidth, "Next", () -> {
                teamPage = Math.min(Math.max(0, (names.size() - 1) / 3), teamPage + 1);
                rebuildWidgets();
            });
        } else if (page == Page.TEAM_COLOR) {
            for (int i = 0; i < TeamEspColor.VALUES.length; i++) {
                final int selected = i;
                nativeButton(i % 2 == 0 ? left : right, top + (i / 2) * 25, pairWidth,
                        TeamEspColor.NAMES[i], () -> {
                            TeamEspColor.setPreset(selected);
                            rebuildWidgets();
                        });
            }
            customColorBox = new EditBox(font, left, top + 104, 228, 20, Component.literal("Custom ESP color"));
            customColorBox.setMaxLength(7);
            customColorBox.setValue(TeamEspColor.hex());
            addRenderableWidget(customColorBox);
            nativeButton(left + 234, top + 104, 74, "Apply HEX", () -> {
                if (TeamEspColor.setHex(customColorBox.getValue())) {
                    feedback = "";
                    rebuildWidgets();
                } else {
                    feedback = "Invalid color. Use #RRGGBB";
                }
            });
            nativeButton(left, top + 130, 308, "ESP range: " + config.teamGlowRange, () -> {
                config.teamGlowRange = switch (config.teamGlowRange) {
                    case 64 -> 256;
                    case 256 -> 512;
                    case 512 -> 2048;
                    case 2048 -> 10000;
                    default -> 64;
                };
                change();
            });
        } else if (page == Page.STYLE) {
            nativeButton(left, top, pairWidth, "Cape: " + CapeCosmetics.label(), () -> {
                config.capeStyle = (config.capeStyle + 1) % 3;
                change();
            });
            nativeButton(right, top, pairWidth, "Wave Emote", () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                onClose();
            });
        } else if (page == Page.WORLD) {
            nativeButton(left, top, 308,
                    YukkitToolkitClient.WORLD_CAPTURE.isCapturing() ? "Stop World Capture" : "Start World Capture",
                    () -> {
                        if (YukkitToolkitClient.WORLD_CAPTURE.isCapturing()) {
                            YukkitToolkitClient.WORLD_CAPTURE.stop();
                            YukkitToolkitClient.notifyPlayer("World capture stopped.");
                        } else {
                            YukkitToolkitClient.WORLD_CAPTURE.start(Minecraft.getInstance());
                        }
                        rebuildWidgets();
                    });
        } else if (page == Page.MAP) {
            nativeButton(left, top, pairWidth, "Minimap: " + label(config.minimap), () -> {
                config.minimap = !config.minimap;
                change();
            });
            nativeButton(right, top, pairWidth, "Map preview: " + label(config.mapPreview), () -> {
                config.mapPreview = !config.mapPreview;
                change();
            });
        }

        // Exactly Minecraft's button rendering, placement and text style.
        int bottom = height - 32;
        if (page == Page.HOME) {
            nativeButton(width / 2 - 100, bottom, 200, "Done", this::onClose);
        } else {
            nativeButton(width / 2 - 154, bottom, 150, "Apply Changes", () -> {
                ToolkitConfig.INSTANCE.save();
                back();
            });
            nativeButton(width / 2 + 4, bottom, 150, "Back", this::back);
        }
    }

    private void nativeButton(int x, int y, int width, String name, Runnable callback) {
        addRenderableWidget(Button.builder(Component.literal(name), button -> callback.run())
                .bounds(x, y, width, 20).build());
    }

    private static String label(boolean enabled) { return enabled ? "ON" : "OFF"; }

    private void change() {
        ToolkitConfig.INSTANCE.save();
        rebuildWidgets();
    }

    private void addTeammate() {
        if (nameBox == null) return;
        String name = nameBox.getValue().trim();
        if (!name.matches("[A-Za-z0-9_.-]{1,32}")) {
            feedback = "Use 1-32 characters: letters, numbers, . _ -";
            return;
        }
        ToolkitConfig cfg = ToolkitConfig.INSTANCE;
        if (cfg.teamMates == null) cfg.teamMates = new ArrayList<>();
        if (cfg.teamMates.stream().anyMatch(s -> s.equalsIgnoreCase(name))) {
            feedback = "Already in your team";
            return;
        }
        if (cfg.teamMates.size() >= 64) {
            feedback = "Team list is full (64)";
            return;
        }
        cfg.teamMates.add(name);
        feedback = "";
        change();
    }

    private void open(Page page) {
        if (minecraft != null) minecraft.gui.setScreen(new ToolkitScreen(parent, page));
    }

    private void back() {
        if (minecraft != null)
            minecraft.gui.setScreen(new ToolkitScreen(parent, page == Page.TEAM_COLOR ? Page.TEAM : page == Page.STORAGE ? Page.MODS : Page.HOME));
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        // Default Screen background and native Minecraft button widgets.
        super.extractRenderState(graphics, mx, my, delta);
        graphics.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);
        if (page == Page.CAMERA) {
            graphics.centeredText(font, Component.literal("Freecam: WASD / Space / Shift / Sprint"), width / 2, height / 2 + 25, 0xFFAAAAAA);
            graphics.centeredText(font, Component.literal("Freecam shortcut: X  |  Perspective: V"), width / 2, height / 2 + 39, 0xFFAAAAAA);
        } else if (page == Page.STORAGE) {
            int baseline=Math.max(46,height / 2 - 108) + 114;
            graphics.centeredText(font, Component.literal("Cached chests: " + ChestCandidates.positions().size()
                    + "    Forced render states: " + ChestCandidates.lastForcedExtracted()),
                    width / 2,baseline,0xFFFFFFFF);
            graphics.centeredText(font, Component.literal("The texture is rendered separately; tracers are optional."),
                    width / 2,baseline + 13,0xFFAAAAAA);
            graphics.centeredText(font, Component.literal("Only storage in chunks loaded by the client."),
                    width / 2,baseline + 25,0xFFAAAAAA);
        } else if (page == Page.AUTOMATION) {
            graphics.centeredText(font, Component.literal("Clicker: hold attack on an entity (no auto aim)"), width / 2, height / 2 + 12, 0xFFAAAAAA);
            graphics.centeredText(font, Component.literal("Miner: only the block under your crosshair"), width / 2, height / 2 + 28, 0xFFAAAAAA);
            graphics.centeredText(font, Component.literal("Mace: preselect while falling | Eat: hotbar carrot"), width / 2, height / 2 + 44, 0xFFAAAAAA);
        } else if (page == Page.TEAM) {
            int left = width / 2 - 154;
            int top = Math.max(46, height / 2 - 108);
            var names = ToolkitConfig.INSTANCE.teamMates;
            if (names != null) {
                for (int i = teamPage * 3; i < Math.min(names.size(), teamPage * 3 + 3); i++) {
                    graphics.text(font, names.get(i), left + 8, top + 60 + (i - teamPage * 3) * 25, 0xFFFFFFFF, true);
                }
            }
            graphics.centeredText(font, Component.literal("ESP range filters loaded players only; no distant tracking"), width / 2, height - 57, 0xFFAAAAAA);
        } else if (page == Page.TEAM_COLOR) {
            int y = Math.min(Math.max(46, height / 2 - 108) + 166, height - 76);
            int color = 0xFF000000 | TeamEspColor.rgb();
            graphics.fill(width / 2 - 154, y, width / 2 - 122, y + 14, color);
            graphics.text(font, "ESP preview: " + TeamEspColor.hex(), width / 2 - 115, y + 3,
                    0xFFFFFFFF, true);
        } else if (page == Page.MAP) {
            if (ToolkitConfig.INSTANCE.mapPreview) {
                ToolkitMap.render(graphics, width / 2 - 88, Math.max(91, height / 2 - 88), 2);
            }
        } else if (page == Page.WORLD) {
            graphics.centeredText(font, Component.literal("Experimental: saves loaded chunks only"), width / 2, height / 2, 0xFFAAAAAA);
            graphics.centeredText(font, Component.literal("Captured chunks: " + YukkitToolkitClient.WORLD_CAPTURE.getSavedChunks()), width / 2, height / 2 + 16, 0xFFFFFFFF);
        } else if (page == Page.STYLE) {
            graphics.centeredText(font, Component.literal("Capes are local only. Wave is a vanilla animation."), width / 2, height / 2 + 20, 0xFFAAAAAA);
        }
        if (!feedback.isEmpty()) {
            graphics.centeredText(font, Component.literal(feedback), width / 2, height - 58, 0xFFFFAA55);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RSHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
