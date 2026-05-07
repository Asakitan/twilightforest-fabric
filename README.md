# Twilight Forest - Codex 1.21.1 Fork

[![Discord](https://img.shields.io/discord/313006291012288521.svg?colorB=7289DA&logo=discord&style=flat-square)](https://discord.gg/6v3z26B)
[![Crowdin](https://badges.crowdin.net/twilight-forest/localized.svg)](https://crowdin.com/project/twilight-forest)
[![CurseForge](http://cf.way2muchnoise.eu/full_227639_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest)
[![For MC](http://cf.way2muchnoise.eu/versions/For%20MC_227639_all.svg)](https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest)
[![Codex Branch](https://img.shields.io/badge/Codex%20fork-1.21.1-4c6fff?style=flat-square)](https://github.com/Asakitan/twilightforest-fabric-1.21.1/tree/1.21.1)
[![Loader](https://img.shields.io/badge/loader-Fabric%20%2F%20Arclight-f5a623?style=flat-square)](https://fabricmc.net/)

This is a fork of the Twilight Forest Fabric repository for Minecraft 1.21.1.
The upstream Twilight Forest project remains the original mod and creative/code base; this branch carries Codex-side compatibility work for our Fabric/Arclight Cobblemon server stack.

Upstream project links:

- Original mod/codebase: [TeamTwilight/twilightforest](https://github.com/TeamTwilight/twilightforest)
- Fabric fork lineage: [TeamTwilight/twilightforest-fabric](https://github.com/TeamTwilight/twilightforest-fabric)
- Codex 1.21.1 fork branch: [Asakitan/twilightforest-fabric-1.21.1/tree/1.21.1](https://github.com/Asakitan/twilightforest-fabric-1.21.1/tree/1.21.1)

## Codex Branch Contributions

This 1.21.1 fork adds server/client compatibility work on top of Twilight Forest rather than replacing upstream authorship:

- Fabric/Arclight 1.21.1 build wiring with official Mojang mappings, Java 21, split server/client source sets, and bundled Fabric API modules needed by the port.
- Server-loadable Twilight registry/data migration for biomes, configured features, structures, loot compatibility, tags, particles, sounds, damage types, and gameplay hooks used by our server.
- Real server-side entity and boss migrations for major Twilight mobs, replacing placeholder stand-ins with dependency-light Java implementations that keep combat, AI, projectiles, boss bars, phase logic, and save data usable on Arclight.
- Paired client renderer and asset support inside the same jar for Twilight block entities, particles, models, and visual parity where vanilla server-side disguises are not enough.
- Arclight runtime compatibility fixes for registry bootstrap timing, Polymer/resource-pack fallback behavior, MythicMobs coexistence, SlashBlade recipe noise, and cold-start validation on the live server profile.
- Repeatable validation workflow using Gradle builds, static reference checks, jar inspection, cold server starts, RCON/console probes, and summon/place/locate smoke tests.

## About Twilight Forest

Twilight Forest is a dimension exploration mod for Minecraft, originally developed by the Twilight Forest team.
Official releases, community links, translation work, and general mod information remain available from the upstream project pages linked above.

## Status

Active 1.21.1 porting branch. This repository is no longer a scaffold: it contains the Fabric mod entry point, Twilight Forest registry/bootstrap ports, server-side entity and worldgen code, paired Fabric client renderers, bundled assets, sounds, structures, and loot/data files.

Some upstream Twilight systems are still intentionally tracked as follow-up work, especially exact multipart networking, some structure-conquered hooks, loot/death chest parity, and remaining renderer-specific edge cases.

## Current Project Layout

```
src/main/java/
  com/codex/twilight/           Codex entry point and shared S2C payloads
  twilightforest/               Fabric/Arclight-side Twilight bootstrap, registries, blocks, items,
                                entities, particles, sounds, loot hooks, worldgen, commands, and mixins
  net/neoforged/, tamaized/     Compatibility shims used by translated Twilight code
src/tfjava/
  twilightforest/               Larger translated Twilight server/common code surface
src/client/java/
  com/codex/twilight/client/    Fabric client initializer, renderer bootstrap, particles, model layers
  twilightforest/client/        Client-side hooks and model-loading support
src/tfjava-client/
  twilightforest/client/        Translated Twilight entity/block models, renderers, and renderer layers
src/main/resources/
  fabric.mod.json               codex_twilight metadata, entrypoints, mixins, access widener
  assets/{catty,codex_twilight,minecraft,twilightforest}/
                                bundled textures, models, blockstates, lang, shaders, sounds, particles
  data/{catty,codex_twilight,minecraft,twilightforest}/
                                dimensions, biomes, tags, loot, recipes, structures, functions, registries
```

At the time this README was refreshed, the project contains roughly 850 Java source files plus thousands of bundled data and asset files, including Twilight JSON data, PNG textures, NBT structures, OGG sounds, shaders, and pack metadata.

## Runtime Scope

- Main mod id: `codex_twilight`.
- Minecraft target: `1.21.1`; Java target: `21`.
- Loader stack: Fabric Loader on the Codex Arclight server/client profile.
- Server entry point: `com.codex.twilight.CodexTwilight`.
- Client entry point: `com.codex.twilight.client.CodexTwilightClient`.
- Early hook: `twilightforest.asm.CodexGrassColorEarlyRiser` for the current grass-color compatibility path.
- Dynamic synced registries include Twilight bird/rabbit variants and custom structure speleothem settings.
- The same jar carries both server logic and paired client visuals so Twilight registry ids, renderers, particles, sounds, and assets stay in sync for our client profile.

## Build

This branch builds as a Fabric Loom project for Minecraft 1.21.1 and Java 21. `splitEnvironmentSourceSets()` keeps client-only classes out of the dedicated-server classloader, while the remapped jar still packages the main and client outputs together for the paired Codex client.

Important build settings:

- Archive name: `codex-twilight`.
- Main mappings: official Mojang mappings.
- Fabric Loader: `0.18.1`.
- Fabric API modules: lifecycle, entity events, registry sync, object builder, transfer/API lookup, command API, rendering, renderer API, resource loader, networking, particles, and model loading.
- `src/tfjava` is attached to the main source set, with a few still-incompatible upstream structure utility classes excluded.
- `src/tfjava-client` is attached to the Loom client source set when present.

From this directory:

```powershell
.\gradlew.bat buildAndInstall
```

The `buildAndInstall` task runs `remapJar`, copies the built mod jar into the server `mods/` directory, and mirrors `src/main/resources/data` into `global_packs/required_data/codex_twilight/data` for the required data-pack distribution.

## Validation And Maintenance Workflow

This fork's published repository intentionally does not include the local `tools/` maintenance directory; it is ignored by Git. Use focused validation for the area being changed with the local helper scripts when working inside the server workspace, but do not rely on those scripts being present after a normal clone of this fork.

1. Build with `.\gradlew.bat compileJava` for Java-only checks, or `.\gradlew.bat buildAndInstall` when the server jar/data mirror should be refreshed.
2. After data, loot, structure, biome, tag, registry JSON, visual, language, or resource changes, run the matching local validation/audit helper if your workspace has the ignored maintenance scripts.
3. For runtime-sensitive changes, cold-start the Arclight server profile and probe with commands such as `/summon`, `/locate biome`, `/locate structure`, `/place structure`, and `/codex` as appropriate.

When importing more upstream Twilight content, prefer targeted translated ports and focused validation over broad blind copies. Keep upstream-derived code/assets clearly attributable and keep Codex compatibility shims isolated in the relevant source packages.

## Licensing

This fork follows the licensing requirements of the upstream Twilight Forest projects.
See this repository's [LICENSE](LICENSE) file and the upstream Twilight Forest license/assets notices before redistributing code, assets, sounds, or structures.
