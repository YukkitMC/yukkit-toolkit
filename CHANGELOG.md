# Changelog

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
