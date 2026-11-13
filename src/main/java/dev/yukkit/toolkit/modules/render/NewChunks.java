package dev.yukkit.toolkit.modules.render;

import dev.yukkit.toolkit.core.Category;
import dev.yukkit.toolkit.core.Module;
import dev.yukkit.toolkit.core.Setting;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Marks chunks that finished loading after you joined (a travel / exploration marker). */
public final class NewChunks extends Module {
    private final Setting.Num seconds = num("Seconds", 60, 5, 600, 5);
    private final Setting.Num radius = num("Radius", 12, 4, 24, 1);

    private final Map<Long, Integer> loadedAt = new HashMap<>();
    private boolean primed;
    private int age;

    public NewChunks() { super("NewChunks", "Marks chunks that just loaded around you.", Category.RENDER); }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xFFFFFFFFL); }

    @Override public void onEnable() { loadedAt.clear(); primed = false; age = 0; }
    @Override public void onDisable() { loadedAt.clear(); }
    @Override public void onWorldChange() { loadedAt.clear(); primed = false; age = 0; }

    @Override public void onTick() {
        if (!hasWorld()) return;
        age++;
        int cx = mc.player.blockPosition().getX() >> 4;
        int cz = mc.player.blockPosition().getZ() >> 4;
        int r = radius.asInt();
        Map<Long, Integer> fresh = new HashMap<>();
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                long k = key(x, z);
                if (mc.level.hasChunkAt(x << 4, z << 4)) {
                    // chunks present on the first scan are the initial load; they get a negative stamp (never drawn)
                    fresh.put(k, loadedAt.containsKey(k) ? loadedAt.get(k) : (primed ? age : -1));
                }
            }
        }
        // chunks that unloaded or left the scan window are forgotten, so reloading re-marks them
        loadedAt.clear();
        loadedAt.putAll(fresh);
        primed = true;
    }

    @Override public void onWorld() {
        if (!hasWorld()) return;
        int life = seconds.asInt() * 20;
        double y = mc.player.getY() + 0.02;
        int drawn = 0;
        Iterator<Map.Entry<Long, Integer>> it = loadedAt.entrySet().iterator();
        while (it.hasNext() && drawn < 120) {
            Map.Entry<Long, Integer> e = it.next();
            int t = e.getValue();
            if (t < 0 || age - t > life) continue;
            long k = e.getKey();
            int x = (int) (k >> 32);
            int z = (int) k;
            AABB box = new AABB(x * 16.0, y, z * 16.0, x * 16.0 + 16.0, y + 0.05, z * 16.0 + 16.0);
            UtilDraw.box(box, 0x40C0FF, "Outline", false);
            drawn++;
        }
    }
}
