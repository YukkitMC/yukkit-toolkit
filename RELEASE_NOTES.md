# Yukkit Toolkit v0.1.0-alpha.6

**Minecraft Java 26.3 · Fabric Loader 0.20.1+ · Java 25**

> **Experimental pre-release:** Source compiled through GitHub Actions; runtime integration with Minecraft and other client mods has not been tested in this environment. Use a separate test profile and back up important worlds.

## Vanilla UI rebuild

The dark custom ClickGUI has been replaced by a multi-page menu made entirely with **Minecraft's native `Screen`, `Button` and `EditBox` widgets**. Expect Minecraft's default menu background and gray interactive buttons, a centered title, two-column option layout and bottom action buttons.

Access Yukkit Tools with **Right Shift** in-game or with **ESC → Yukkit Tools**. Existing features are grouped into Visual, Camera, Mods, Team, Cosmetics, World and Map.

## New features

- **Auto Totem:** optional automatic inventory-to-offhand swap when your offhand is empty, via ordinary client inventory interactions. Disabled by default; does not discard an occupied offhand item.
- **Ore X-Ray:** optional client-side block-model filtering, preserving standard ore models and ancient debris while hiding non-ore block models. Enabling/disabling triggers chunk geometry rebuilding. Disabled by default.
- **Higher-resolution terrain map:** one surface-block sample per pixel instead of one every four blocks. 88×88 cached image, bounded to six raster rows per tick, plus merged same-color draw runs. This reduces the old large pixel blocks but is not a fully textured topographic map.

## Existing modules

Freecam, Perspective, local Time/Weather, Fullbright, Keystrokes, TNT Timer, optional Team Glow, local capes, vanilla Wave animation and experimental World Downloader.

## Warnings and limits

- **X-Ray** is limited to blocks that the client has actually received. Anti-X-Ray plugins may hide/substitute ores. Other rendering mods and shaders may have incompatibilities. Chunk rebuilds can briefly affect performance.
- **Auto Totem** only interacts with the player's own inventory when no container/menu is open. Server rules may prohibit automated item swaps.
- The map is sampled from client-loaded terrain and is *not* Xaero's World Map or a complete downloaded map.
- The World Downloader is experimental and cannot create a faithful server backup.
- A green CI build proves compilation only, not successful in-game operation.

## Installation

1. Use Minecraft Java **26.3** with **Java 25**, Fabric Loader and compatible Fabric API.
2. Remove all older `yukkit-toolkit-*.jar` files from the instance's `mods` folder.
3. Place **`yukkit-toolkit-0.1.0-alpha.6.jar`** directly in `mods`; do not import the workflow ZIP as a Modrinth project.
4. Launch a test world, press **RSHIFT**, and test features individually.

Full source: https://github.com/YukkitMC/yukkit-toolkit
