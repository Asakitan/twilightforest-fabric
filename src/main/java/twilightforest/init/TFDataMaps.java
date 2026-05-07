package twilightforest.init;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import twilightforest.util.datamaps.MagicMapBiomeColor;

import java.util.Map;

public final class TFDataMaps {
	private static final Map<ResourceKey<Biome>, MagicMapBiomeColor> MAGIC_MAP_BIOME_COLORS = Map.ofEntries(
		Map.entry(TFBiomes.FOREST, new MagicMapBiomeColor(MapColor.PLANT, 1)),
		Map.entry(TFBiomes.DENSE_FOREST, new MagicMapBiomeColor(MapColor.PLANT, 0)),
		Map.entry(TFBiomes.LAKE, new MagicMapBiomeColor(MapColor.WATER, 3)),
		Map.entry(TFBiomes.STREAM, new MagicMapBiomeColor(MapColor.WATER, 1)),
		Map.entry(TFBiomes.SWAMP, new MagicMapBiomeColor(MapColor.DIAMOND, 3)),
		Map.entry(TFBiomes.FIRE_SWAMP, new MagicMapBiomeColor(MapColor.NETHER, 1)),
		Map.entry(TFBiomes.CLEARING, new MagicMapBiomeColor(MapColor.GRASS, 2)),
		Map.entry(TFBiomes.OAK_SAVANNAH, new MagicMapBiomeColor(MapColor.GRASS, 0)),
		Map.entry(TFBiomes.HIGHLANDS, new MagicMapBiomeColor(MapColor.DIRT, 0)),
		Map.entry(TFBiomes.THORNLANDS, new MagicMapBiomeColor(MapColor.WOOD, 3)),
		Map.entry(TFBiomes.FINAL_PLATEAU, new MagicMapBiomeColor(MapColor.COLOR_LIGHT_GRAY, 2)),
		Map.entry(TFBiomes.FIREFLY_FOREST, new MagicMapBiomeColor(MapColor.EMERALD, 1)),
		Map.entry(TFBiomes.DARK_FOREST, new MagicMapBiomeColor(MapColor.COLOR_GREEN, 3)),
		Map.entry(TFBiomes.DARK_FOREST_CENTER, new MagicMapBiomeColor(MapColor.COLOR_ORANGE, 3)),
		Map.entry(TFBiomes.SNOWY_FOREST, new MagicMapBiomeColor(MapColor.SNOW, 1)),
		Map.entry(TFBiomes.GLACIER, new MagicMapBiomeColor(MapColor.ICE, 1)),
		Map.entry(TFBiomes.MUSHROOM_FOREST, new MagicMapBiomeColor(MapColor.COLOR_ORANGE, 0)),
		Map.entry(TFBiomes.DENSE_MUSHROOM_FOREST, new MagicMapBiomeColor(MapColor.COLOR_PINK, 0)),
		Map.entry(TFBiomes.ENCHANTED_FOREST, new MagicMapBiomeColor(MapColor.COLOR_CYAN, 2)),
		Map.entry(TFBiomes.SPOOKY_FOREST, new MagicMapBiomeColor(MapColor.COLOR_PURPLE, 0))
	);

	private TFDataMaps() {
	}

	@Nullable
	public static MagicMapBiomeColor getMagicMapBiomeColor(Holder<Biome> biome) {
		return biome.unwrapKey().map(MAGIC_MAP_BIOME_COLORS::get).orElse(null);
	}
}
