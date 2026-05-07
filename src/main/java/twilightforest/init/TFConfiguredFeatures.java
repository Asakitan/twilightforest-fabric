package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import twilightforest.TwilightForestMod;

public final class TFConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CANOPY_TREE = key("tree/canopy_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAINBOW_OAK_TREE = key("tree/rainbow_oak");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SMALLER_JUNGLE_TREE = key("tree/smaller_jungle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TROLL_BIG_MUSHGLOOMS = key("troll_big_mushglooms");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TROLL_CAVE_DIRT = key("troll_cave_dirt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TROLL_CAVE_MYCELIUM = key("troll_cave_mycelium");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TROLL_HUGE_BROWN_MUSHROOMS = key("troll_huge_brown_mushrooms");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TROLL_HUGE_RED_MUSHROOMS = key("troll_huge_red_mushrooms");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TWILIGHT_OAK_TREE = key("tree/twilight_oak_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UBEROUS_SOIL_PATCH_BIG = key("uberous_soil_patch_big");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UBEROUS_SOIL_PATCH_SMALL = key("uberous_soil_patch_small");

    private TFConfiguredFeatures() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, TwilightForestMod.prefix(path));
    }
}
