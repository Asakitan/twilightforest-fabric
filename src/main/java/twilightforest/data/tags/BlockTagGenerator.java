package twilightforest.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import twilightforest.TwilightForestMod;

public final class BlockTagGenerator {
    public static final TagKey<Block> CASTLE_BLOCKS = create("castle_blocks");
    public static final TagKey<Block> CLOUDS = create("clouds");
    public static final TagKey<Block> CANNOT_TROLL_CAVE_HOLLOW = create("cannot_troll_cave_hollow");
    public static final TagKey<Block> DARK_TOWER_ALLOWED_POTS = create("dark_tower_allowed_pots");
    public static final TagKey<Block> DRUID_PROJECTILE_REPLACEABLE = create("druid_projectile_replaceable");
    public static final TagKey<Block> ROOT_TRACE_SKIP = create("tree_roots_skip");
    public static final TagKey<Block> PLANTS_HANG_ON = create("plants_hang_on");
    public static final TagKey<Block> SUPPORTS_STALAGMITES = create("supports_stalagmites");
    public static final TagKey<Block> WORLDGEN_REPLACEABLES = create("worldgen_replaceables");
    public static final TagKey<Block> TF_BERRY_BUSHES_REPLACE = create("tf_berry_bushes_replace");
    public static final TagKey<Block> SMALL_LAKES_DONT_REPLACE = create("small_lakes_dont_replace");
    public static final TagKey<Block> ICE_BOMB_REPLACEABLES = create("ice_bomb_replaceables");
    public static final TagKey<Block> PENGUINS_SPAWNABLE_ON = create("penguins_spawnable_on");
    public static final TagKey<Block> GIANTS_SPAWNABLE_ON = create("giants_spawnable_on");
    public static final TagKey<Block> BANISTERS = create("banisters");
    public static final TagKey<Block> DRYING_RACKS = create("drying_racks");
    public static final TagKey<Block> HOLLOW_LOGS = create("hollow_logs");
    public static final TagKey<Block> TOWERWOOD = create("towerwood");
    public static final TagKey<Block> OREBERRY_BUSHES_SURVIVE = create("oreberry_bushes_survive");
    public static final TagKey<Block> TROPHY_PEDESTAL_ACTIVATION_BLOCKS = create("trophy_pedestal_activation_blocks");
    public static final TagKey<Block> FIRE_JET_FUEL = create("fire_jet_fuel");
    public static final TagKey<Block> MAZESTONE = create("mazestone");
    public static final TagKey<Block> CARMINITE_REACTOR_IMMUNE = create("carminite_reactor_immune");
    public static final TagKey<Block> CARMINITE_REACTOR_ORES = create("carminite_reactor_ores");

    private BlockTagGenerator() {
    }

    private static TagKey<Block> create(String path) {
        return TagKey.create(Registries.BLOCK, TwilightForestMod.prefix(path));
    }

    @SuppressWarnings("unused")
    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
