# Cosmetic and Emote Synchronization: Design Notes

This document describes a **future feature**, not a deployed service.

## Current alpha behavior

- Two cape textures are bundled as local resources.
- Cape selection is saved in the local `config/yukkit-toolkit.json`.
- The selected cape is rendered by the user's own Yukkit Toolkit instance.
- Wave triggers the vanilla arm-swing animation; it is not a custom synchronized emote.
- No cosmetic profile details are uploaded, downloaded or shared by Yukkit Toolkit.

## What real cross-client synchronization needs

A client-only mod cannot assume an arbitrary Minecraft server forwards custom payloads to other modded clients. To share a player's selected cape and custom animation, YukkitMC would need an opt-in relay server or another purpose-built service with an identity-verification scheme, consent controls, a limited data model, abuse protections and a published privacy policy.

A responsible future protocol might include `player_id`, `cape_id`, `emote_id`, `timestamp` and a signed version identifier. It must not publish account tokens or treat a nickname as proof of ownership.

## Out of scope for this alpha

- Uploading personal skins or capes.
- Real-time multiplayer emote playback.
- Automatic player tracking outside loaded Minecraft chunks.
- Impersonation or using another player's authentication tokens.

No future feature in this document should be described as implemented without a verified release.
