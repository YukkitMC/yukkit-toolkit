# Yukkit Toolkit v0.1.0-alpha.5

**Minecraft Java 26.3 · Fabric Loader 0.20.1 · Java 25**

This is an **experimental pre-release**, not a stable or fully tested Minecraft build. Back up your worlds and test it in a separate Modrinth instance.

## What's new
- **Offline-mode teammate names:** Support usernames containing dots (`.`), underscores (`_`) and hyphens (`-`) up to 32 characters. Names are matched locally against loaded players.
- **Team outlines:** Render local glowing outlines of configured teammates (up to 64 blocks away) through solid blocks, without modifying the player's server-side flags.
- **Minimap:** 25×25 surface preview from already-loaded terrain, with colored markers for nearby players and teammates.
- **Map page:** A separate Map tab inside Yukkit Tools, with HUD/preview toggles.
- **Vanilla pause menu:** The ESC menu now includes a native **Yukkit Tools** button and returns to the pause menu when closed using RSHIFT or X.
- **Interface refinements:** Native Minecraft buttons with more restrained dark panels.

## Existing features
Freecam, Perspective (V), Time Changer, Weather Changer, Keystrokes, TNT Timer, team list, basic local capes, and experimental World Downloader.

## Limitations
- Not tested in-game yet. A successful Java compilation does not guarantee all Mixins and mods work together at runtime.
- Map and outlines only use **client-loaded** chunks and entities. No remote coordinates, world seed, inventories, or invisible server data are retrieved.
- Custom cosmetic sharing between users is **not yet implemented**.
- World Downloader remains experimental; exported saves may be incomplete or invalid.
- Client-side outlines or Freecam may be prohibited by individual server rules.

## Installation
1. Use Minecraft **26.3**, **Fabric Loader 0.20.1+**, and a matching **Fabric API**.
2. Remove older `yukkit-toolkit` versions from your Modrinth profile's `mods` directory.
3. Place **`yukkit-toolkit-0.1.0-alpha.5.jar`** (not a ZIP) into `mods`.
4. Launch Minecraft and press **RSHIFT**. ESC also exposes a **Yukkit Tools** button.

See the [source code](https://github.com/YukkitMC/yukkit-toolkit) and [changelog](CHANGELOG.md).
