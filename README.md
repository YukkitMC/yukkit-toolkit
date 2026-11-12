# Yukkit Toolkit

**Modular, client-side Minecraft utilities.** Developed by [YukkitMC](https://github.com/YukkitMC).

[![Build](https://github.com/YukkitMC/yukkit-toolkit/actions/workflows/build.yml/badge.svg)](https://github.com/YukkitMC/yukkit-toolkit/actions/workflows/build.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-006EFE)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-222222)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

> **Status: v0.1.0-alpha.3**. Source implementation, build validation, and in-game compatibility testing are separate milestones. This is an experimental build, not a stable release.

## Modules

| Feature | Status | Description |
|---|---|---|
| Right Shift ClickGUI | Initial implementation | Open the configurable in-game control panel. |
| Time Changer | Initial implementation | Day / noon / sunset / night rendered client-side, with server fallback. |
| Weather Changer | Initial implementation | Local clear / rain / server weather. |
| Fullbright | Initial implementation | Brightness override in client lightmap calculations. |
| Freecam | Initial implementation | Independent camera movement using WASD, Space / Shift, and mouse. |
| World Downloader | Experimental | Captures *loaded overworld chunks* into a new local Anvil-format save. |

The 26.3 Time Changer now hooks the client clock instead of the removed Level#getDayTime method. This fixes a confirmed pre-launch Mixin crash seen in 0.1.0-alpha.2.\n\nThe **World Downloader is deliberately limited**: this alpha copies block states of loaded chunks. It does **not** preserve exact biomes, block entities, inventories, entities, structures, server seed, or unloaded areas. The generated save is **not an exact server backup**, and opening it in singleplayer has not yet been verified. New areas will use an unrelated world seed, causing mismatched terrain. **Back up saves before experimenting.** It never modifies an existing save.

### Controls

- **Right Shift**: open/close Toolkit panel (customizable through Minecraft Controls).
- **Visual**: cycle time and weather, toggle Fullbright.
- **Camera**: Freecam on/off. Use **WASD**, **Space**, **Sneak**, **mouse**, and **Sprint** for movement.
- **World**: start/stop experimental overworld capture, record chunks while exploring.

## Requirements for players

- Minecraft Java Edition **26.3**
- **Java 25** to run the game
- **Fabric Loader**
- **Fabric API** for Minecraft 26.3

## Building

The 26.3 build requires **JDK 25** to run Gradle and compile the mod. Install both JDKs and Gradle 9.7.1, with Gradle itself running on JDK 25:

```bash
gradle clean build
```

The Fabric-remapped runtime JAR is built under `build/libs/`. The GitHub Actions workflow also publishes downloadable build artifacts on successful pushes. Do not confuse the `-sources.jar` or `-dev.jar` with the runnable mod.

### Installation

After a successful build, place the **remapped mod JAR** and the appropriate **Fabric API** in your Minecraft 26.3 `mods` folder. Install and select a Fabric 26.3 profile in your launcher. Do not install a build for a different Minecraft version.

### World capture

1. Join an Overworld where capturing world data is permitted.
2. Press **Right Shift**, open **World**, and enable **World Download**.
3. Explore normally; the tool scans already loaded chunks around your real player.
4. Stop the capture before leaving the server.
5. Find a new directory under `.minecraft/saves/YukkitCapture-YYYYMMDD-HHmmss`.

No server-side files or privileged access are used. The mod does not retrieve hidden chunks. Do not use this on servers that forbid map copying.

## Repository

- [Contributing](CONTRIBUTING.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Security policy](SECURITY.md)
- [MIT License](LICENSE)

This is an independent project and not affiliated with Mojang or Microsoft.
