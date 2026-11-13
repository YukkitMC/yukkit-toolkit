# Changelog

## 0.1.0-alpha.12 — 2026-11-12

### Fixed
- Textured Chest ESP now submits its duplicate chest model in the opaque solid phase with depth ALWAYS_PASS rather than through the translucent/OIT path. This targets hidden chests disappearing behind terrain, particularly with Iris.
- Hidden chest render state extraction no longer inherits the vanilla 64-block view-distance restriction.

### Extended
- Range from 64 blocks to a search of up to 32 already-loaded chunks in each horizontal direction (approximately 512 blocks, subject to chunk availability).
- Incremental nearest-first scan at 64 chunk lookups per game tick, with a maximum of 1024 cached chest candidates.
- Frustum culling prevents model submissions behind the camera while still including chests behind walls.

### Limitations
- No live in-game test in the user's Iris/Sodium modpack has been performed. Compilation alone does not prove that the through-wall effect works.
- Chunks or block entity data not received from the server cannot be discovered.
- Barrels are still excluded from the textured ChestRenderer-specific overlay.



## 0.1.0-alpha.11 — 2026-11-12

### New: True-textured Chest ESP
- Replaces the translucent blue box overlay with a second submission of Minecraft's original textured `ChestModel`.
- Uses the real chest sprite, facing, double-chest half selection and interpolated lid animation from the vanilla chest renderer.
- Draws the model through blocks with a depth-independent `RenderType`, leaving terrain visible and unchanged.
- Caches client-loaded chest positions once per 20 ticks (maximum 96 within approximately 64 blocks).
- Extracts otherwise-culled nearby chest render states to improve through-wall visibility, without loading new chunks.
- Removes the previous `Gizmos.cuboid` overlay completely; the previous nested render-pass crash path remains removed.

### Caveats
- Applies to Minecraft's block-entity chests, trapped chests, Ender chests and other variants that share `ChestRenderer`. Barrels are ordinary block models and are not rendered through walls by this path.
- Uses Minecraft's model/texture rendering APIs, not a separate GPU render pass.
- The build compiles with Minecraft 26.3, but runtime compatibility with Iris/Sodium is **not yet verified**.
- Only containers already sent to the client can be shown.



## 0.1.0-alpha.10 — 2026-11-12

### Fixed
- Fixed a verified Chest ESP crash: `IllegalStateException: Close the existing render pass before performing additional commands`.
- Removed manual `StagedVertexBuffer.upload()` / nested `RenderPass` calls during translucent terrain rendering.
- Replaced the custom GPU overlay with Minecraft 26.3's built-in `Gizmos.cuboid` rendering on Fabric's `BEFORE_GIZMOS` phase.
- Removed the obsolete GameRenderer cleanup Mixin registration.

### Behavior
- Blue translucent highlights remain visible through walls, with normal terrain unaffected.
- Checks only block entities already loaded by the client, approximately every 20 game ticks and up to 96 nearby containers.
- Compilation validated in GitHub Actions. Runtime with Iris/Sodium should be retested; no in-game confirmation has yet been performed for this fix.



## 0.1.0-alpha.9 — 2026-11-12

### Fixed
- Chest X-Ray no longer makes nearby terrain, buildings, and other block models invisible.
- Chest highlighting has been separated from the existing ore-only X-Ray toggle.

### Changed
- Replaced the Chest X-Ray filter with **Chest ESP**: translucent blue 3D markers rendered through walls without touching normal world geometry.
- Detects nearby client-loaded chests, trapped chests, Ender chests and barrels. Scans every 20 game ticks, limits the search to loaded chunks within roughly 64 blocks, and caps rendered boxes at 96.
- Updated the GPU drawing code for Minecraft 26.3's Renderpearl API; compilation verified in CI.

### Caveats
- Highlights are transparent box overlays, not textured chest models drawn through walls.
- Some shader/optimization mods may interfere; in-game behavior remains unverified.
- The server must have already sent the chunk and block entity to the client.
- Ore X-Ray remains a separate feature that deliberately hides non-ore block models.



## 0.1.0-alpha.8 — 2026-11-12

### Added
- Customizable teammate glow outline colors (blue by default).
- Eight preset colors: blue, cyan, green, red, yellow, purple, white and orange.
- Custom `#RRGGBB` color entry with input validation and preview.
- Dedicated Team Color page using Minecraft's native button and input widgets.

### Technical
- Client-side `Entity#getTeamColor` injection applies only to loaded players matched in the saved team list while Team Glow is enabled.
- Color is stored as a 24-bit RGB value in `config/yukkit-toolkit.json`.
- Original server scoreboard/team memberships are not modified.
- ESP range remains a display filter, not a server-tracking extension.

### Known limitations
- This build has passed Java compilation, but not in-game runtime testing.
- Actual visibility depends on the server transmitting the player entity and on other rendering mods.
- Colored outlines may be unavailable if a modded shader pipeline replaces vanilla glowing.



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
