# Changelog

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
