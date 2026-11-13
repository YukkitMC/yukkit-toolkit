# Yukkit Toolkit v0.1.0-alpha.13

**Minecraft Java 26.3 · Fabric · Java 25**

## Chest Model: occlusion troubleshooting hotfix

Some users reported that the real textured chest model still disappeared behind solid walls, including with Iris/Sodium. This version changes the depthless model render pipeline and removes an extra camera-frustum discard from hidden-chest extraction.

- Keeps the real vanilla chest texture and lid model, with **no colored bounding box around chests**.
- The duplicate rendering pipeline now has **no depth/stencil attachment**, avoiding ordinary wall depth testing.
- Forces up to **192 cached hidden chest render states per frame**, independent of vanilla visible-section occlusion.
- Scans client-loaded chunks incrementally, adjustable from **4 to 32 chunks per direction** (up to ~512 blocks).
- The new Storage ESP settings page displays **Cached chests** and **Forced render states**. These counters help diagnose whether a hidden chest reaches the renderer.

**Important:** Compilation alone does not prove that the textured model will always display behind walls. Iris/Sodium's rendering passes may still hide it. If so, you can enable optional tracers (which use Minecraft's standard always-on-top Gizmos API) while preserving the texture mode. A screenshot of the settings counters when the chest disappears will allow a more specific fix.

## Other additions
- **Storage ESP:** chest/trapped/Ender filters, range options, optional tracers and tracer colors.
- **Shulker & hopper markers:** optional, off by default.
- **Item ESP:** optional markers around dropped items, off by default.
- **Auto Respawn:** optional automatic normal respawn, off by default.
- Regular Minecraft vanilla GUI buttons remain unchanged.

## Install
1. Use Minecraft 26.3, Java 25, Fabric Loader and a compatible Fabric API.
2. Delete all older Yukkit Toolkit JAR files from the Modrinth instance `mods` folder.
3. Copy only `yukkit-toolkit-0.1.0-alpha.13.jar` to `mods`.
4. Open **RSHIFT → Mods → Chest Model** and **Storage ESP Options**.
5. If the chest model still disappears behind stone, screenshot the counters in Storage ESP Options and share a fresh `latest.log`.

Release is experimental and **has not been runtime-tested with the user's modpack**.

GitHub: https://github.com/YukkitMC/yukkit-toolkit
