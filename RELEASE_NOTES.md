# Yukkit Toolkit v0.1.0-alpha.10

**Minecraft Java 26.3 · Fabric Loader 0.20.1+ · Java 25**

## Hotfix: crash while turning on Chest ESP

This release addresses the actual exception reported with **Iris and Sodium**:

```text
IllegalStateException: Close the existing render pass before performing additional commands
at StagedVertexBuffer.uploadDrawsToBuffers(...)
at dev.yukkit.toolkit.render.ChestEspRenderer.render(...)
```

The alpha.9 implementation attempted to upload a GPU vertex buffer inside Minecraft's existing active rendering pass. In alpha.10, **Chest ESP uses the vanilla 26.3 Gizmos API** at Fabric's `BEFORE_GIZMOS` event instead. It no longer manually opens render passes or uploads GPU buffers.

### Expected behavior
- Normal blocks and terrain remain visible.
- Already-loaded chests, trapped chests, Ender chests and barrels are marked with translucent blue boxes visible through walls.
- Only client-loaded storage blocks within approximately 64 blocks are considered (maximum 96 markers).
- Ore X-Ray remains separate.

### Install
1. Remove `yukkit-toolkit-0.1.0-alpha.9.jar` or any older Yukkit Toolkit from the `mods` directory.
2. Install only `yukkit-toolkit-0.1.0-alpha.10.jar` with Minecraft **26.3**, Fabric and Java 25.
3. Enter a test world and enable **RSHIFT → Mods → Chest ESP**.
4. If it still crashes, share the **new full crash report**, especially the `Caused by:` section.

**Validation:** the Java/Fabric build has passed CI, but this hotfix has not been executed in-game with the user's exact Iris/Sodium modpack. Pre-release status is intentional.

Source: https://github.com/YukkitMC/yukkit-toolkit
