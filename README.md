# Codex Twilight

Fabric mod that backports Twilight Forest 1.21.1 content into our Arclight server/client pair. Client assets are embedded in the mod jar; the old `codex-twilight` CodexResourcePack module is no longer the distribution path.

## Status

Phase A complete (terrain/dimension wired in legacy `catty_twilight_realm` datapack).
Scaffold (this commit): empty entry point, no registered content yet.

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

This first release is a server-side data/resource mod with a minimal Fabric entrypoint. It builds with the plain Gradle Java plugin and compiles only against the local Fabric Loader jar, so it does not need network access or Loom remapping until Java-side worldgen/Polymer registrations are added.

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
