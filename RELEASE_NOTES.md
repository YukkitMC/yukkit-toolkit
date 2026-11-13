# Yukkit Toolkit v0.2.0-alpha.4 · Minecraft 26.3

**Alpha build, not fully tested in-game.** Back up your worlds and test in a separate instance.

## What's new
- Black and blue theme for the module window.
- Fix: typed search text was invisible; the field is now drawn by the mod.
- Module window restyled to match the reference layout and enlarged; search box is now in the title bar.
- Fix: legacy toggles (Keystrokes, Fullbright, Auto Eat...) could be enabled but not disabled from the new screen.
- 139 modules across Combat, Movement, Player, World, Exploit, Render, Misc and Fun (written from scratch).
- New **Right Shift** window: category tabs, search across all modules, per-module settings, and a **Teams** tab (add by name, add nearby players, remove, glow toggle, team colour).
- **PlayerESP** module; teammates use your team colour.
- **Download World** button under the disconnect button in the pause menu.
- Fix: startup crash caused by the mixin config owning the entrypoint package.

## Notes
- Mixins live in `yukkit-toolkit.modules.mixins.json` (not required): a mixin that does not apply is skipped and logged, and its module then has no effect.
- Using combat/movement modules on servers that forbid them can get you banned.
