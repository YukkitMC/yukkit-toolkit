# Yukkit Toolkit v0.1.0-alpha.8

**Minecraft Java 26.3 · Fabric · Java 25**

## Team ESP color customization
- **Default:** Yukkit Blue (`#006EFE`).
- Choose any of **8 color presets**: Blue, Cyan, Green, Red, Yellow, Purple, White, Orange.
- Or enter your own hexadecimal color, for example `#FF33AA`, using the vanilla Minecraft input field.
- Changes are saved in the Yukkit Toolkit local JSON config and take effect on the client-side teammate outline.
- Access via **RSHIFT → Team → Glow Color** or **ESC → Yukkit Tools → Team → Glow Color**.
- The existing ESP distance setting is available on the **Team Color** page.

## Important limitations
- Only teammates that the server has already sent to the client can be highlighted.
- Configured ESP distance does not force the server to load faraway players.
- The color applies locally and does not modify server scoreboard teams.
- This build has **compiled successfully in GitHub Actions**, but runtime compatibility with Minecraft, Sodium, Iris or other mods has not been verified.

## Installation
1. Use Minecraft 26.3 with compatible Fabric Loader, Fabric API and Java 25.
2. Remove older Yukkit Toolkit JARs from your Modrinth profile's `mods` directory.
3. Place `yukkit-toolkit-0.1.0-alpha.8.jar` directly in `mods`.
4. Open the game, use RSHIFT or ESC → Yukkit Tools, and enable Team Glow for your chosen teammates.

Source: https://github.com/YukkitMC/yukkit-toolkit
