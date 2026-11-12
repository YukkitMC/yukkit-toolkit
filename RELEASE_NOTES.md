# Yukkit Toolkit v0.1.0-alpha.9

**Minecraft Java 26.3 · Fabric · Java 25**

## Fixed: Chest ESP

The old Chest X-Ray filter made the whole world look invisible. It now behaves as **Chest ESP**:

- **Normal stone, dirt, buildings and terrain stay visible**, unchanged.
- Nearby loaded chests, trapped chests, Ender chests and barrels are indicated by translucent **blue 3D boxes visible through walls**.
- Ore X-Ray remains an independent option. Turning on ore-only X-Ray will still hide other block models intentionally.
- The overlay scans only client-loaded chunks once every 20 ticks and limits rendering to 96 storage blocks within roughly 64 blocks.
- No server-side data, storage contents, or unloaded chunks are requested.

**Important:** The ESP shows a transparent 3D marker around the chest, **not the chest's textured model** through the wall. This uses a new Minecraft 26.3 Renderpearl rendering path.

### Installation

1. Use Minecraft Java **26.3**, compatible Fabric Loader/API and Java 25.
2. Remove older Yukkit Toolkit JARs from your instance's `mods` folder.
3. Place `yukkit-toolkit-0.1.0-alpha.9.jar` directly in `mods`.
4. Press **RSHIFT → Mods → Chest ESP: ON**, or use **ESC → Yukkit Tools → Mods**.

This is an **experimental pre-release**. GitHub Actions validates compilation, but the overlay has not yet been tested in a live Minecraft client or against Sodium/Iris. Follow server rules regarding client-side assistance.

Repository: https://github.com/YukkitMC/yukkit-toolkit
