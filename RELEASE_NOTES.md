# Yukkit Toolkit v0.1.0-alpha.11

**Minecraft Java 26.3 · Fabric · Java 25**

## Chest ESP now displays the actual chest model

The previous blue outline/filled box has been replaced with **Minecraft's textured 3D chest model**, including its actual lid, lock, orientation and vanilla texture.

- No blue box or wireframe around the chest.
- Normal terrain, walls and buildings stay rendered.
- Client-loaded chests can be shown even behind blocks, using an additional vanilla model submission with depth testing disabled.
- Chests, trapped chests and Ender chests are supported, plus variants rendered by the vanilla `ChestRenderer`; the barrel model is not yet a see-through texture.
- Within approximately 64 blocks and up to 96 tracked loaded chests.
- The GPU commands remain managed by Minecraft's normal submission system. No nested render pass is created, avoiding the specific alpha.9 crash code path.

**Caution:** This is an experimental alpha. GitHub Actions passed Java compilation but the new textured rendering has not been tested inside your Sodium/Iris setup. The effect may fail or crash due to rendering-mod incompatibility. It does not discover server-hidden blocks.

### Install
1. Remove all older `yukkit-toolkit-*.jar` files from your Modrinth profile's `mods` folder.
2. Copy `yukkit-toolkit-0.1.0-alpha.11.jar` into `mods`.
3. Use Minecraft 26.3, compatible Fabric Loader + Fabric API, and Java 25.
4. Open **RSHIFT → Mods → Chest Model: ON** or **ESC → Yukkit Tools → Mods**.
5. If it crashes, send the latest `Caused by:` stack trace from `latest.log`.

Source: https://github.com/YukkitMC/yukkit-toolkit
