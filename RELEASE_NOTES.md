# Yukkit Toolkit v0.1.0-alpha.12

**Minecraft Java 26.3 · Fabric · Java 25**

## Textured Chest ESP: occlusion and range update

**This update addresses two limitations reported in alpha.11:**

1. The textured chest copy could disappear once a solid block obstructed it. It now uses an opaque forced-solid model render phase with an **always-pass depth comparison**, instead of the translucent/OIT path that may be occluded during Iris shader composition. The actual vanilla chest model and texture remain in use, with no blue outline or box.
2. The range was hard-coded to 64 blocks. Chest candidate scanning now reaches **up to 32 already-loaded chunks in each horizontal direction**, approximately 512 blocks from the player (up to the edge of the available client-loaded region). It is not a server-wide or unlimited scan.

### Performance and visibility
- Scans 64 candidate chunks per client tick, most-nearby chunks first. One full 32-chunk-radius scan takes roughly 67 ticks, so new or distant chests may take several seconds to appear.
- Limits the collected list to 1024 block entities per scan.
- Only submits hidden chest render states within the camera frustum. Walls do not prevent those states from being submitted.
- Bypasses the vanilla 64-block *visual* culling only for the additional ESP chest states, without changing server settings or loading/requesting missing chunks.
- Normal terrain remains rendered, and Ore X-Ray stays a separate option.

### Limitations
- **Not tested in the user's live game.** Compilation confirms the APIs exist; the actual through-wall effect still needs testing with Iris and Sodium.
- A server may send far fewer than 32 chunks, and block entity data may be absent. Unloaded chest locations are unknowable to this client.
- Barrels still do not get textured through-wall models through this chest renderer.

### Install
1. Use Minecraft **26.3** with Fabric Loader, Fabric API and Java 25.
2. Remove the older Yukkit Toolkit JAR from the instance `mods` folder.
3. Place **`yukkit-toolkit-0.1.0-alpha.12.jar`** directly into `mods`.
4. Press **RSHIFT → Mods → Chest Model: ON**.
5. If a chest still disappears behind stone or a shader, share the new `latest.log` and a screenshot showing the issue.

Repository: https://github.com/YukkitMC/yukkit-toolkit
