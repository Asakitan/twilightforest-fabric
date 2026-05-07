# Codex Twilight

[![Minecraft](https://img.shields.io/badge/For%20MC-1.21.1-e34c18?style=flat-square)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/loader-Fabric%20%2F%20Arclight-f5a623?style=flat-square)](https://fabricmc.net/)
[![Project](https://img.shields.io/badge/project-Twilight%20Forest-2f7d32?style=flat-square)](https://github.com/TeamTwilight/twilightforest)
[![Branch](https://img.shields.io/badge/branch-Codex%201.21.1-4c6fff?style=flat-square)](https://github.com/Asakitan/twilightforest-fabric-1.21.1/tree/1.21.1)
[![Status](https://img.shields.io/badge/status-active%20porting-7b1fa2?style=flat-square)](#branch-contributions)

Codex Twilight is our 1.21.1 Fabric/Arclight continuation of Twilight Forest content for the Cobblemon server stack.
It keeps the upstream Twilight Forest identity and data model while adding the server/runtime work needed for our hybrid Arclight environment.

This branch is based on the Twilight Forest project and is maintained at [Asakitan/twilightforest-fabric-1.21.1](https://github.com/Asakitan/twilightforest-fabric-1.21.1/tree/1.21.1).
Client assets are embedded in the mod jar; the old `codex-twilight` CodexResourcePack module is no longer the distribution path.

## Branch Contributions

This 1.21.1 branch adds Codex-side compatibility work on top of Twilight Forest rather than replacing the upstream project:

- Fabric/Arclight 1.21.1 build wiring with official Mojang mappings, Java 21, split server/client source sets, and bundled Fabric API modules needed by the port.
- Server-loadable Twilight registry/data migration for biomes, configured features, structures, loot compatibility, tags, particles, sounds, damage types, and gameplay hooks used by our server.
- Real server-side entity and boss migrations for major Twilight mobs, replacing placeholder stand-ins with dependency-light Java implementations that keep combat, AI, projectiles, boss bars, phase logic, and save data usable on Arclight.
- Paired client renderer and asset support inside the same jar for Twilight block entities, particles, models, and visual parity where vanilla server-side disguises are not enough.
- Arclight runtime compatibility fixes for registry bootstrap timing, Polymer/resource-pack fallback behavior, MythicMobs coexistence, SlashBlade recipe noise, and cold-start validation on the live server profile.
- Repeatable validation workflow using Gradle builds, static reference checks, jar inspection, cold server starts, RCON/console probes, and summon/place/locate smoke tests.

## Status

Active 1.21.1 porting branch. Terrain, dimension data, many worldgen references, major entity batches, boss cores, projectile damage types, official sound parity passes, and client renderer/assets integration have been brought forward for the Codex server/client pair.

Some upstream Twilight systems are still intentionally tracked as follow-up work, especially exact multipart networking, some structure-conquered hooks, loot/death chest parity, and remaining renderer-specific edge cases.

## Layout

```
src/main/java/com/codex/twilight/
  CodexTwilight.java            entry point
  block/                        Polymer block registrations (later)
  item/                         Polymer item registrations (later)
  worldgen/                     ConfiguredFeature/PlacedFeature Java bootstraps (later)
  structure/                    Structure Java bootstrap (later)
  compat/                       MythicMobs / PlaceholderAPI hooks (later)
src/main/resources/
  fabric.mod.json
  data/catty/                   migrated from catty_twilight_realm datapack (phase F)
  data/codex_twilight/          new content (biomes/features/structures)
block_substitutions.json        TF block id -> vanilla equivalent (used by tools)
tools/replace_blocks.py         JSON walker that applies block_substitutions.json
```

## Build

This branch builds as a Fabric Loom project for Minecraft 1.21.1 and Java 21. Server and client code are split by Loom source sets but packaged into the same mod jar so registry ids, renderers, assets, particles, and sounds stay paired for the Codex server/client profile.

From this directory:

```powershell
gradle buildAndInstall
```

If Gradle is not installed globally, use the local Gradle 8.11 distribution already stored under `mods-src/slankeston-reality-anchor/gradle/wrapper/gradle-8.11-bin.zip`.

## Workflow for porting more TF content (after phase G this becomes a skill)

1. Extract source data from the TF jar into `/tmp/tf_extract/` using the JDK `jar` tool. Do this manually; the script does not redistribute TF data.
2. Author original `data/codex_twilight/...` JSONs informed by the extracted data; do not byte-copy or minor-rename TF JSONs.
3. For block names appearing inside our authored JSONs or in NBTs the maintainer wants to reuse, run `tools/replace_blocks.py` over local copies to apply the substitution table.
4. Test in-server with `/locate biome`, `/locate structure`, `/place structure`.
