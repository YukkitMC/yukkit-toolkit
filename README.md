<div align="center">

# Yukkit Toolkit

### Your Minecraft. Your experience.

**A modular client-side toolkit built to make Minecraft Java Edition more comfortable, customizable, and practical.**

[![Status](https://img.shields.io/badge/status-early%20development-006EFE?style=for-the-badge&labelColor=121722)](https://github.com/YukkitMC/yukkit-toolkit)
[![Minecraft](https://img.shields.io/badge/platform-Java%20Edition-222936?style=for-the-badge&labelColor=121722)](https://www.minecraft.net/)
[![License: MIT](https://img.shields.io/badge/license-MIT-006EFE?style=for-the-badge&labelColor=121722)](LICENSE)

[**Overview**](#overview) · [**Features**](#planned-features) · [**Roadmap**](#roadmap) · [**Contribute**](CONTRIBUTING.md) · [**Security**](SECURITY.md)

</div>

---

## Overview

**Yukkit Toolkit** is a project by [YukkitMC](https://github.com/YukkitMC), an independent organization dedicated to useful Minecraft modifications.

The idea is simple: **one mod, one interface, the tools you actually need.** Instead of installing a separate client for every convenience, Yukkit Toolkit aims to provide a coherent set of configurable, client-side utilities for exploration, building, recording, and multiplayer quality of life.

Our development principles:

- **Player-first:** prioritize features with a clear everyday benefit.
- **Modular by design:** enable only the tools you want.
- **Clean experience:** an organized interface, predictable controls, sensible defaults.
- **Performance-conscious:** keep overhead low and behavior understandable.
- **Transparent development:** document capabilities, limitations, and compatibility honestly.

> [!IMPORTANT]
> **Project status: early development.** Features below are **planned**, not claims that they already work. There is currently no stable public build or confirmed supported Minecraft version.

## Planned features

| Category | Module | Description |
| :--- | :--- | :--- |
| **World** | World Downloader | Save world data received by your client for permitted local use. |
| **World** | Waypoints | Organize and revisit useful locations. |
| **World** | Chunk Borders | Display chunk boundaries for building and navigation. |
| **Visual** | Time Changer | Adjust the time of day rendered on your screen. |
| **Visual** | Weather Changer | Customize locally rendered weather effects. |
| **Visual** | Fullbright | Improve visibility through client-side rendering settings. |
| **Visual** | Zoom | Use smooth, configurable camera zoom. |
| **Camera** | Freecam | Move your camera independently from your character, where permitted. |
| **Camera** | Freelook | Look around without rotating your character. |
| **Interface** | HUD Customization | Adjust useful in-game information and layout. |
| **Settings** | Server Profiles | Save different preferences for different servers. |

> [!NOTE]
> Client-side features cannot override server-authoritative game rules. A World Downloader can only record data actually sent to your client; it cannot obtain a complete server backup or hidden, unloaded chunks.

## One panel. Every module.

The planned **ClickGUI** will bring all modules into a single interface.

- **Right Shift (RSHIFT)** to open the panel by default.
- Clear categories: World, Visual, Camera, Interface, and Settings.
- Individual module toggles, options, and configurable keybindings.
- Local persistence of preferences.
- A layout designed for quick access without covering gameplay unnecessarily.

The interface and default bindings are subject to change before release.

## Roadmap

| Milestone | Scope | State |
| :--- | :--- | :--- |
| **Foundation** | Project setup, module architecture, configuration | Planned |
| **Interface** | ClickGUI, navigation, toggles, keybindings | Planned |
| **Core utilities** | Time Changer, Weather Changer, Fullbright, Freecam | Planned |
| **World utilities** | World Downloader, waypoints, chunk tools | Planned |
| **First public build** | Compatibility checks, documentation, downloadable JAR | Planned |

We will publish supported Minecraft versions, loaders, dependencies, and changelogs alongside actual releases rather than advertise untested compatibility.

## Getting started

**There is no official downloadable version yet.**

When builds become available, installation instructions will be included in [Releases](https://github.com/YukkitMC/yukkit-toolkit/releases). Only use artifacts from verified project channels, and never run an unknown JAR simply because someone labels it “Yukkit.”

## Multiplayer & fair use

We build for player convenience, but **server rules always matter**:

- Local time and weather changes affect your view, not the server's simulation.
- Freecam does not grant new permissions or access to unloaded areas.
- Save or reuse world data only where you are authorized to do so.
- Some servers may restrict or prohibit client modifications.

Yukkit Toolkit is not intended to bypass access controls or evade anti-cheat enforcement. You are responsible for using modules in accordance with the communities you join.

## Contributing

We welcome clear bug reports, thoughtful suggestions, documentation improvements, and focused pull requests. We prefer contributions to the **official YukkitMC project** instead of independent distributions, but MIT licensing permits forks and redistribution under its terms.

Please review the following before participating:

| Document | Purpose |
| :--- | :--- |
| [**Contributing**](CONTRIBUTING.md) | How to report, propose, and submit changes. |
| [**Code of Conduct**](CODE_OF_CONDUCT.md) | Expectations for respectful collaboration. |
| [**Security**](SECURITY.md) | How to report vulnerabilities privately. |
| [**MIT License**](LICENSE) | Rights and obligations for use and redistribution. |

[Open an issue](https://github.com/YukkitMC/yukkit-toolkit/issues) · [View pull requests](https://github.com/YukkitMC/yukkit-toolkit/pulls)

## License

This project is licensed under the **[MIT License](LICENSE)**. It allows use, modification, redistribution, and commercial use subject to retaining the required copyright and license notice. Contributions are submitted under the same license unless otherwise agreed.

---

<div align="center">

**Built by [YukkitMC](https://github.com/YukkitMC). Built for Minecraft players.**

<sub>Yukkit Toolkit is independent and is not affiliated with, endorsed by, or sponsored by Mojang Studios or Microsoft. Minecraft and associated names belong to their respective owners.</sub>

</div>
