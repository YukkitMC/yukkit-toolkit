# Yukkit Toolkit v0.1.0-alpha.7

**Minecraft Java 26.3 · Fabric Loader 0.20.1+ · Java 25**

> Experimental pre-release: CI compilation passed, but this build has not been tested in Minecraft. Install it in a separate profile and report crashes with `latest.log`.

## New in alpha.7
- **Chest X-Ray**: optional visibility filter for chests, trapped chests, Ender chests and barrels using loaded client data.
- **Auto Clicker**: hold attack while pointing at an entity; cycle the configured maximum CPS (4–12). Normal combat cooldown and reach still apply.
- **Auto Miner**: toggle automatic mining of the block pointed at by the crosshair. No pathfinding or distant mining.
- **Mace Swap**: preselect a mace from your hotbar when falling toward an entity; restore the prior slot when done.
- **Auto Eat**: select and consume golden carrots from your hotbar below a chosen hunger threshold; restore the previous slot.
- **Team ESP range**: 64 / 256 / 512 / 2048 / 10000 blocks. **Only entities actually loaded by your client can be highlighted.**
- A new **Automation** menu page using native Minecraft controls.

## Important
All new automation features and Chest X-Ray default to **OFF**. Chest X-Ray shares the selective-rendering system with the ore view, and the two filters can be enabled together.

The 10,000-block slider **does not** reveal players the server has not transmitted. Server rules can prohibit automatic combat, mining, block highlighting and through-wall teammate outlines. Rendering mods may interfere with X-Ray.

## Installation
1. Use Minecraft **26.3**, **Java 25**, compatible Fabric Loader and Fabric API.
2. Remove older Yukkit Toolkit JARs from the Modrinth instance `mods` folder.
3. Place `yukkit-toolkit-0.1.0-alpha.7.jar` in `mods` (do **not** import a ZIP as a Modrinth project).
4. Press **RSHIFT** or open **ESC → Yukkit Tools**. Use the **Automation**, **Mods** and **Team** pages.

Report crash logs at https://github.com/YukkitMC/yukkit-toolkit/issues
