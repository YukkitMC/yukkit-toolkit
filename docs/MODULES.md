# Yukkit module architecture

This is an original Yukkit Toolkit module system. It adopts the general concept
of categorized, toggleable client features without copying LiquidBounce source
code, assets, or GUI implementation.

## Foundation
- Stable IDs, display names, descriptions, and feature categories.
- Explicit enable/disable lifecycle callbacks.
- Client tick callback for enabled modules.
- Duplicate-ID protection, category filtering, and disabled-by-default behavior.

Register modules during client initialization with
`ModuleRegistry.register(new YourModule())`. The registry is ticked from the
client end-tick callback.

## Implementation principles
- Preserve this repository's MIT license.
- Implement behavior independently against Minecraft 26.3 and Fabric.
- Keep the existing Yukkit screen and controls. Future visual polish may adapt
  broad design tokens such as spacing, typography, restrained accent colors,
  and state indicators, but must not copy LiquidBounce layouts, assets, or UI code.
- Do not advertise a feature as implemented until connected and tested in-game.
- Keep features under explicit user control; this framework does not bypass
  server-side restrictions or anti-cheat protections.

## Planned stages
1. Module lifecycle and registry.
2. Typed settings, keybinds, persistence, and searchable catalogue.
3. Adapters for existing Yukkit utilities to avoid duplicate behavior.
4. Original implementations of compatible combat, movement, player, render,
   world, and quality-of-life features, tested independently.
5. Visual polish of the existing Yukkit interface.
