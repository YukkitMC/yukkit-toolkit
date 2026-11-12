<div align="center">

# Yukkit Toolkit

**A modular client-side utility mod for Minecraft Java Edition.**

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-006EFE)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-151b29)](https://fabricmc.net/)
[![Build](https://github.com/YukkitMC/yukkit-toolkit/actions/workflows/build.yml/badge.svg)](https://github.com/YukkitMC/yukkit-toolkit/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-006EFE)](LICENSE)

**[Releases](https://github.com/YukkitMC/yukkit-toolkit/releases) • [Features](#features) • [Contributing](CONTRIBUTING.md) • [Security](SECURITY.md)**

</div>

---

## What is Yukkit Toolkit?

Yukkit Toolkit combines practical Minecraft tools under a vanilla-style in-game options menu, with a focus on straightforward controls, reliable behavior, and low overhead.

> **Early alpha — v0.1.0-alpha.6.** Builds are automatically compiled by GitHub Actions, but Minecraft runtime compatibility, interoperability with other mods and individual gameplay features have **not** been fully validated. Test in a separate instance and back up important worlds.

## Features

| Category | Module | Details |
|---|---|---|
| **Visual** | Time Changer | Local day, noon, sunset or night; does not change the server clock. |
| **Visual** | Weather Changer | Local clear/rain display with an option to follow server weather. |
| **Visual** | Fullbright | Client-side lighting adjustment. |
| **Camera** | Freecam | Independent camera with eased acceleration/deceleration, smooth partial-tick position interpolation, and sprint speed. |
| **Camera** | Perspective | Rotate the camera independently of your character. Default keyboard shortcut: **V**. |
| **Mods** | Auto Totem | Optional inventory totem refill when the offhand is empty; uses ordinary inventory swaps. Disabled by default. |
| **Mods** | Ore X-Ray | Optional ore-only client-side block rendering with chunk rebuild. Disabled by default. |
| **Mods** | Keystrokes | Lightweight WASD, left/right mouse and SPACE HUD. |
| **Mods** | TNT Timer | Shows countdowns for up to three nearby loaded primed TNT entities. |
| **Mods** | Lightweight Graphics | Reversible preset for minimal particles, disabled clouds and render distance capped at 8 chunks. Actual FPS gains depend on your hardware and existing settings. |
| **Team Time** | Nearby teammate list | Add online or offline-mode names (including dots and hyphens); see loaded nearby players and distances. |
| **Team Time** | Local Team Glow | Optional wall-visible glow outlines of configured teammates up to 64 blocks away, if already loaded by the client. |
| **Map** | High-resolution minimap | 88×88 surface map sampled at 1 block per pixel, with row-budgeted updates and nearby player markers. |
| **Menu** | Vanilla options interface | Default Minecraft screen backdrop, centered titles, gray buttons and two-column options; accessible through ESC or RSHIFT. |
| **Style** | Local capes | Two bundled cape designs, **Azure** and **Ember**. Local rendering only. |
| **Style** | Wave | Simple vanilla arm-swing gesture. Not a custom animated emote. |
| **World** | World Downloader | Experimental capture of loaded overworld block data into a local Anvil-format world directory. Not a faithful or validated world backup. |

### Controls

- **Right Shift:** open/close the Yukkit Toolkit panel.
- **V:** toggle Perspective when no menu is open.
- **Freecam:** WASD to move, mouse to rotate, SPACE to ascend, SNEAK to descend, and SPRINT to accelerate.
- **Mods page:** Auto Totem, Ore X-Ray, Keystrokes, TNT Timer, Lightweight Graphics and Team HUD.
- **ESC:** opens the vanilla pause menu with a new **Yukkit Tools** button.
- **Map tab:** turn the minimap HUD/preview on or off.
- **Team Time tab:** add/remove nicknames, enable/disable nearby glow.
- **Style tab:** select cape preset and run the basic arm animation.

Minecraft **26.3** uses SDL input scancodes rather than the older GLFW keycodes.

## Important limitations

**Cosmetic synchronization.** The included capes render **locally**, not on other people's clients. To show a player's individually selected cape or a custom emote to another user who also has Yukkit Toolkit, a secure synchronization service or an opt-in server relay must be developed. This release does **not** transmit cosmetics, nicknames, profiles or emote data over any network.

**World Downloader.** Captures data only from chunks the client has already received. The alpha exporter does not preserve the world seed, exact biomes, inventories, block entities, structures, mobs or unexplored chunks. The resulting world may not load as expected. It must not be treated as an exact backup and should only be used where server rules permit.

**Auto Totem.** Only triggers for an empty offhand in the ordinary player inventory; it does not drop an item already held in the offhand. Some servers forbid automation.

**Ore X-Ray.** Alters client rendering for client-received blocks only, and may conflict with Sodium, shaders or culling mods; some servers disallow it. It cannot recover server-hidden ore data. Toggle off to restore normal block models.

**Map and teammate outlines.** Both are restricted to chunks and players already loaded by the local client. They do not reveal remote players or hidden server data. Team Glow can display client-loaded teammates through blocks and is enabled only when you turn it on.

**Multiplayer rules.** Client-side does not mean every server allows a feature. Verify server policies before using Freecam, World Downloader, Team Glow or other modules. No anti-cheat bypass or server permission escalation is promised.

**Performance.** The lightweight preset changes ordinary Minecraft video settings and stores the previous values for restoration when disabled in the same session. An unexpected crash may leave the newly saved video settings enabled. Performance gains are not measured or guaranteed.

## Requirements

- Minecraft Java Edition **26.3**
- Fabric Loader compatible with 26.3
- Fabric API for 26.3
- Java **25**

Install the JAR generated by the [Build Fabric Mod](https://github.com/YukkitMC/yukkit-toolkit/actions/workflows/build.yml) workflow into your profile's `mods` directory. Install only the runtime JAR, not the sources JAR.

## Build

Run Gradle **9.7.1** using JDK **25**:

```sh
gradle clean build
```

The output is written to `build/libs/`. CI publishes the runnable JAR as a downloadable workflow artifact after a successful build. A successful Java compilation is **not** equivalent to a successful in-game test.

## Development

Yukkit Toolkit welcomes issues and pull requests following the [contribution guide](CONTRIBUTING.md). Please include your Minecraft version, Fabric Loader version, list of installed mods and relevant crash log when reporting a runtime issue.

[Changelog](CHANGELOG.md) • [Code of Conduct](CODE_OF_CONDUCT.md) • [Security Policy](SECURITY.md) • [MIT License](LICENSE)

---

<div align="center">

**YukkitMC** — *Your Minecraft. Your experience.*

<sub>Independent project. Not affiliated with Mojang Studios or Microsoft.</sub>

</div>
