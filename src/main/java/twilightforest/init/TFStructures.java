package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import twilightforest.TwilightForestMod;

public final class TFStructures {
    public static final ResourceKey<Structure> AURORA_PALACE = key("aurora_palace");
    public static final ResourceKey<Structure> CAMP = key("camp");
    public static final ResourceKey<Structure> DARK_TOWER = key("dark_tower");
    public static final ResourceKey<Structure> FALLEN_TRUNK = key("fallen_trunk");
    public static final ResourceKey<Structure> FINAL_CASTLE = key("final_castle");
    public static final ResourceKey<Structure> GIANT_HOUSE = key("giant_house");
    public static final ResourceKey<Structure> HEDGE_MAZE = key("hedge_maze");
    public static final ResourceKey<Structure> HOLLOW_HILL_SMALL = key("small_hollow_hill");
    public static final ResourceKey<Structure> HOLLOW_HILL_MEDIUM = key("medium_hollow_hill");
    public static final ResourceKey<Structure> HOLLOW_HILL_LARGE = key("large_hollow_hill");
    public static final ResourceKey<Structure> HOLLOW_TREE = key("hollow_tree");
    public static final ResourceKey<Structure> HYDRA_LAIR = key("hydra_lair");
    public static final ResourceKey<Structure> KNIGHT_STRONGHOLD = key("knight_stronghold");
    public static final ResourceKey<Structure> LABYRINTH = key("labyrinth");
    public static final ResourceKey<Structure> LICH_TOWER = key("lich_tower");
    public static final ResourceKey<Structure> MUSHROOM_TOWER = key("mushroom_tower");
    public static final ResourceKey<Structure> NAGA_COURTYARD = key("naga_courtyard");
    public static final ResourceKey<Structure> QUEST_GROVE = key("quest_grove");
    public static final ResourceKey<Structure> QUEST_ISLAND = key("quest_island");
    public static final ResourceKey<Structure> TROLL_CAVE = key("troll_cave");
    public static final ResourceKey<Structure> YETI_CAVE = key("yeti_cave");

    private TFStructures() {
    }

    private static ResourceKey<Structure> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE, TwilightForestMod.prefix(path));
    }
}
