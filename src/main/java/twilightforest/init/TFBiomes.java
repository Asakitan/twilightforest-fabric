package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import twilightforest.TwilightForestMod;

public final class TFBiomes {
    public static final ResourceKey<Biome> FOREST = key("forest");
    public static final ResourceKey<Biome> DENSE_FOREST = key("dense_forest");
    public static final ResourceKey<Biome> FIREFLY_FOREST = key("firefly_forest");
    public static final ResourceKey<Biome> CLEARING = key("clearing");
    public static final ResourceKey<Biome> OAK_SAVANNAH = key("oak_savannah");
    public static final ResourceKey<Biome> STREAM = key("stream");
    public static final ResourceKey<Biome> LAKE = key("lake");
    public static final ResourceKey<Biome> MUSHROOM_FOREST = key("mushroom_forest");
    public static final ResourceKey<Biome> DENSE_MUSHROOM_FOREST = key("dense_mushroom_forest");
    public static final ResourceKey<Biome> ENCHANTED_FOREST = key("enchanted_forest");
    public static final ResourceKey<Biome> SPOOKY_FOREST = key("spooky_forest");
    public static final ResourceKey<Biome> SWAMP = key("swamp");
    public static final ResourceKey<Biome> FIRE_SWAMP = key("fire_swamp");
    public static final ResourceKey<Biome> DARK_FOREST = key("dark_forest");
    public static final ResourceKey<Biome> DARK_FOREST_CENTER = key("dark_forest_center");
    public static final ResourceKey<Biome> SNOWY_FOREST = key("snowy_forest");
    public static final ResourceKey<Biome> GLACIER = key("glacier");
    public static final ResourceKey<Biome> HIGHLANDS = key("highlands");
    public static final ResourceKey<Biome> HIGHLANDS_UNDERGROUND = key("highlands_underground");
    public static final ResourceKey<Biome> THORNLANDS = key("thornlands");
    public static final ResourceKey<Biome> FINAL_PLATEAU = key("final_plateau");
    public static final ResourceKey<Biome> UNDERGROUND = key("underground");

    private TFBiomes() {
    }

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, TwilightForestMod.prefix(path));
    }
}
