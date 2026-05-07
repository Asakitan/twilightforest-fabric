package twilightforest.world.components.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.storage.loot.LootTable;

public record RuinedFoundationConfig(RuinedFoundationDimensions dimensions, RuinedFoundationBlocks blocks, ResourceKey<LootTable> lootTable) implements FeatureConfiguration {
    public static final Codec<RuinedFoundationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuinedFoundationDimensions.CODEC.forGetter(RuinedFoundationConfig::dimensions),
            RuinedFoundationBlocks.CODEC.forGetter(RuinedFoundationConfig::blocks),
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(RuinedFoundationConfig::lootTable)
    ).apply(instance, RuinedFoundationConfig::new));

    public record RuinedFoundationDimensions(IntProvider wallWidth, IntProvider wallHeights, IntProvider basementHeight, FloatProvider placeFloorTest) {
        public static final MapCodec<RuinedFoundationDimensions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IntProvider.codec(1, 16).fieldOf("wall_width").forGetter(RuinedFoundationDimensions::wallWidth),
                IntProvider.codec(1, 32).fieldOf("wall_heights").forGetter(RuinedFoundationDimensions::wallHeights),
                IntProvider.codec(0, 16).fieldOf("basement_height").forGetter(RuinedFoundationDimensions::basementHeight),
                FloatProvider.codec(-8, 8).fieldOf("random_floor_chance").forGetter(RuinedFoundationDimensions::placeFloorTest)
        ).apply(instance, RuinedFoundationDimensions::new));
    }

    public record RuinedFoundationBlocks(BlockStateProvider floor, BlockStateProvider basementPosts, BlockStateProvider lootContainer, BlockStateProvider wallBlock, BlockStateProvider wallTop, BlockStateProvider decayedWall, BlockStateProvider decayedTop) {
        public static final MapCodec<RuinedFoundationBlocks> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockStateProvider.CODEC.fieldOf("floor").forGetter(RuinedFoundationBlocks::floor),
                BlockStateProvider.CODEC.fieldOf("basement_posts").forGetter(RuinedFoundationBlocks::basementPosts),
                BlockStateProvider.CODEC.fieldOf("loot_container").forGetter(RuinedFoundationBlocks::lootContainer),
                BlockStateProvider.CODEC.fieldOf("wall_block").forGetter(RuinedFoundationBlocks::wallBlock),
                BlockStateProvider.CODEC.fieldOf("wall_top_block").forGetter(RuinedFoundationBlocks::wallTop),
                BlockStateProvider.CODEC.fieldOf("decayed_wall_block").forGetter(RuinedFoundationBlocks::decayedWall),
                BlockStateProvider.CODEC.fieldOf("decayed_wall_top_block").forGetter(RuinedFoundationBlocks::decayedTop)
        ).apply(instance, RuinedFoundationBlocks::new));
    }
}
