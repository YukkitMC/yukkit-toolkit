package dev.yukkit.toolkit.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/** Shared drawing / scanning helpers for the render modules. */
final class UtilDraw {
    static final int PLAYER = 0, HOSTILE = 1, PASSIVE = 2, NONE = -1;

    private UtilDraw() {}

    static Minecraft mc() { return Minecraft.getInstance(); }

    static float partial() {
        try {
            return mc().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        } catch (Throwable t) {
            return 1.0f;
        }
    }

    /** Classifies an entity for the ESP / Tracers filters. */
    static int kind(Entity e) {
        if (e == null || e == mc().player || e instanceof ArmorStand) return NONE;
        if (!(e instanceof LivingEntity le) || !le.isAlive()) return NONE;
        if (e instanceof Player) return PLAYER;
        if (e instanceof Monster) return HOSTILE;
        if (e instanceof Mob) return PASSIVE;
        return NONE;
    }

    /** Bounding box shifted to the interpolated render position. */
    static AABB smoothBox(Entity e, float pt) {
        Vec3 render = e.getPosition(pt);
        Vec3 cur = e.position();
        return e.getBoundingBox().move(render.x - cur.x, render.y - cur.y, render.z - cur.z);
    }

    static int rgb(int r, int g, int b) {
        return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }

    static int hue(float degrees) {
        return Mth.hsvToRgb(((degrees % 360f) + 360f) % 360f / 360f, 0.75f, 1.0f) & 0xFFFFFF;
    }

    /** Close = red, far = green. */
    static int distanceColor(double dist, double max) {
        float f = (float) Mth.clamp(dist / Math.max(1.0, max), 0.0, 1.0);
        return hue(f * 120f);
    }

    /** Low health = red, full = green. */
    static int healthColor(float health, float maxHealth) {
        float f = maxHealth <= 0f ? 1f : Mth.clamp(health / maxHealth, 0f, 1f);
        return hue(f * 120f);
    }

    /** Team colour of the entity, or -1 if it has none. */
    static int teamColor(Entity e) {
        try {
            int c = e.getTeamColor() & 0xFFFFFF;
            return c == 0xFFFFFF ? -1 : c;
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Colour for an entity given a colour-mode name. */
    static int entityColor(Entity e, String mode, double dist, double maxDist, float fixedHue) {
        switch (mode) {
            case "Team": {
                int t = teamColor(e);
                if (t >= 0) return t;
                return distanceColor(dist, maxDist);
            }
            case "Health":
                if (e instanceof LivingEntity le) return healthColor(le.getHealth(), le.getMaxHealth());
                return 0xFFFFFF;
            case "Fixed":
                return hue(fixedHue);
            default:
                return distanceColor(dist, maxDist);
        }
    }

    /** Draws a box. {@code style}: "Outline", "Filled" or "Both". */
    static void box(AABB box, int rgb, String style, boolean throughWalls) {
        rgb &= 0xFFFFFF;
        int strokeA = style.equals("Filled") ? 0x00 : 0xFF;
        int fillA = style.equals("Outline") ? 0x00 : (style.equals("Filled") ? 0x66 : 0x30);
        GizmoStyle s = GizmoStyle.strokeAndFill((strokeA << 24) | rgb, 1.5f, (fillA << 24) | rgb);
        var g = Gizmos.cuboid(box, s);
        if (throughWalls) g.setAlwaysOnTop();
    }

    static void line(Vec3 a, Vec3 b, int rgb, float width, boolean throughWalls) {
        var g = Gizmos.line(a, b, 0xFF000000 | (rgb & 0xFFFFFF), width);
        if (throughWalls) g.setAlwaysOnTop();
    }

    /** Camera position, nudged forward so lines starting at the eye stay visible. */
    static Vec3 lineOrigin() {
        Minecraft mc = mc();
        Vec3 cam = mc.gameRenderer.mainCamera().position();
        if (mc.player == null) return cam;
        return cam.add(mc.player.getLookAngle().scale(0.4));
    }

    /**
     * Scans a cube around the player and collects matching block positions
     * (nearest-first is not guaranteed; the cap simply stops the scan).
     */
    static List<BlockPos> scan(int radius, int vRadius, int cap, BiPredicate<BlockPos, BlockState> test) {
        List<BlockPos> out = new ArrayList<>();
        Minecraft mc = mc();
        Level level = mc.level;
        if (level == null || mc.player == null) return out;
        BlockPos c = mc.player.blockPosition();
        int minY = level.getMinY();
        int y0 = Math.max(minY, c.getY() - vRadius);
        int y1 = c.getY() + vRadius;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = c.getX() - radius; x <= c.getX() + radius; x++) {
            for (int z = c.getZ() - radius; z <= c.getZ() + radius; z++) {
                if (!level.hasChunkAt(x, z)) continue;
                for (int y = y0; y <= y1; y++) {
                    p.set(x, y, z);
                    BlockState st = level.getBlockState(p);
                    if (st.isAir()) continue;
                    if (test.test(p, st)) {
                        out.add(p.immutable());
                        if (out.size() >= cap) return out;
                    }
                }
            }
        }
        return out;
    }
}
