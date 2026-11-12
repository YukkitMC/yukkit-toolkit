# Changelog

## 0.1.0-alpha.7 — 2026-11-12

### Added
- Chest X-Ray toggle: keep chest, trapped chest, Ender chest and barrel rendering when other block models are hidden.
- Team ESP/Glow range choices: 64, 256, 512, 2048 and 10000 blocks, within client-loaded entity visibility.
- Auto Clicker with adjustable CPS cap; attacks the entity under the crosshair while holding attack, following the local attack cooldown.
- Auto Miner for the block currently pointed at, using normal client game mode mining calls.
- Mace Swap to temporarily select a hotbar mace when falling toward an entity.
- Auto Eat for hotbar golden carrots, with adjustable hunger threshold and slot restoration.
- Native Minecraft-style Automation menu page and new controls on Mods and Team pages.

### Known limitations
- **Not yet run inside Minecraft** with other mods. Compilation success is not a runtime compatibility test.
- Chest rendering may vary with other block-entity renderer and optimization mods.
- Long-range ESP cannot discover players not already sent by the game server.
- Mace Swap only preselects the mace; success depends on player timing and server rules.
- Auto Clicker and Auto Miner do not add aim assistance, reach, pathfinding or bypass server rules.
- Automatic gameplay tools are disabled by default.



## 0.1.0-alpha.6 — 2026-11-12

### Added
- Auto Totem for refilling an empty offhand from standard player inventory using normal inventory interactions.
- Ore X-Ray rendering mode with ore whitelist and a chunk-rebuild action on toggling.
- Full vanilla-style menu pages with default Minecraft backgrounds, native gray buttons, centered titles and footer controls.

### Improved
- Map resolution from 25 coarse cells (4 blocks per sample) to 88 cells (1 block per sample).
- Map data is refreshed over multiple ticks instead of scanning the whole image in one tick.
- The minmap renders cached colors and combines identical neighboring pixels into horizontal draw runs.

### Known limitations
- No actual in-game runtime or modpack compatibility test is available yet.
- Auto Totem and X-Ray are disabled by default. Follow individual server rules.
- X-Ray relies on vanilla rendering hooks and may conflict with Sodium, shaders or culling mods.
- 88x88 maps are still stylized surface previews, not fully textured world maps.


## 0.1.0-alpha.5 — 2026-11-12

### Added
- Offline-mode nickname matching with dots, underscores and hyphens.
- Wall-visible local teammate glow outlines through the client renderer.
- HUD minimap and native Yukkit Tools Map tab, with sampled surfaces from loaded chunks.
- Yukkit Tools button in Minecraft's ESC pause menu.

### Improved
- Team management no longer requires official Java account-name syntax.
- Team Glow now evaluates the rendered player rather than overwriting server-synchronized flags.
- The Yukkit Tools main panel uses native Minecraft buttons and understated vanilla-style backgrounds.

### Caution
- This is a pre-release built by CI and **not yet tested in the running game**.
- Minimap and team markers operate on client-loaded data only.
- Users should follow server rules about spectator-camera tools or teammate outlines.


## 0.1.0-alpha.4 — 2026-11-12

### Added
- Perspective mode using an independent client-side view rotation, with a configurable V key.
- Keystrokes HUD displaying WASD, LMB, RMB and SPACE.
- TNT Timer HUD for nearby, loaded primed TNT entities.
- Team Time management with up to 64 saved Minecraft nicknames, nearby distance HUD and optional local glow.
- Reversible lightweight graphics preset affecting render distance, particles and clouds.
- Style tab with two bundled local cape designs, Azure and Ember.
- Basic vanilla arm-swing Wave gesture.
- Mods, Team Time and Style pages in the Right Shift panel.

### Improved
- Freecam movement uses damped velocity and partial-tick interpolation for smoother camera transitions.
- HUD entity queries are rate-limited and cached rather than repeated every rendered frame.
- Team Glow only highlights loaded nearby players in the current client world and restores the local flag when no longer eligible.

### Known limitations
- Individual cape selection and custom emotes are not synchronized between players.
- No verified Minecraft in-game test has been performed on this build.
- World Downloader is experimental and does not produce a complete server backup.
- The lightweight graphics preset is not a guaranteed FPS increase and may conflict with user-changed options while enabled.

## 0.1.0-alpha.3 — 2026-11-12

- Fixed the Minecraft 26.3 startup crash caused by a removed `Level#getDayTime` Mixin target.
- Moved local visual time handling to the client clock.
