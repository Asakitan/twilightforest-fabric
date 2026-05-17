package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import twilightforest.data.tags.StructureTagGenerator;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFDataMaps;
import twilightforest.init.TFItems;
import twilightforest.item.mapdata.TFMagicMapData;
import twilightforest.util.datamaps.MagicMapBiomeColor;
import twilightforest.util.landmarks.LandmarkUtil;
import twilightforest.util.landmarks.LegacyLandmarkPlacements;
import twilightforest.world.components.structures.util.LandmarkStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicMapItem extends MapItem {
	public static final String STR_ID = "magicmap";
	private static final Map<ChunkPos, Holder<Biome>[]> CACHE = new HashMap<>();

	public MagicMapItem(Properties properties) {
		super(properties);
	}

	public static ItemStack setupNewMap(Level level, int worldX, int worldZ, byte scale, boolean trackingPosition, boolean unlimitedTracking) {
		ItemStack itemstack = new ItemStack(TFItems.FILLED_MAGIC_MAP.get());
		createMapData(itemstack, level, worldX, worldZ, scale, trackingPosition, unlimitedTracking, level.dimension());
		return itemstack;
	}

	@Nullable
	public static TFMagicMapData getData(ItemStack stack, Level level) {
		MapId mapid = stack.get(DataComponents.MAP_ID);
		return mapid == null ? null : TFMagicMapData.getMagicMapData(level, getMapName(mapid.id()));
	}

	@Nullable
	public static TFMagicMapData getData(ItemStack stack, TooltipContext context) {
		MapId mapid = stack.get(DataComponents.MAP_ID);
		return mapid != null && context.mapData(mapid) instanceof TFMagicMapData mapData ? mapData : null;
	}

	@Nullable
	protected TFMagicMapData getCustomMapData(ItemStack stack, Level level) {
		TFMagicMapData mapdata = getData(stack, level);
		if (mapdata == null && !level.isClientSide()) {
			BlockPos sharedSpawnPos = level.getSharedSpawnPos();
			mapdata = MagicMapItem.createMapData(stack, level, sharedSpawnPos.getX(), sharedSpawnPos.getZ(), 3, false, false, level.dimension());
		}
		return mapdata;
	}

	public static ColumnPos getMagicMapCenter(int x, int z) {
		int mapSize = 2048;
		int roundX = (int) Math.round((double) (x - 1024) / mapSize);
		int roundZ = (int) Math.round((double) (z - 1024) / mapSize);
		int scaledX = roundX * mapSize + 1024;
		int scaledZ = roundZ * mapSize + 1024;
		return new ColumnPos(scaledX, scaledZ);
	}

	private static TFMagicMapData createMapData(ItemStack stack, Level level, int x, int z, int scale, boolean trackingPosition, boolean unlimitedTracking, ResourceKey<Level> dimension) {
		MapId freeMapId = level.getFreeMapId();
		ColumnPos pos = getMagicMapCenter(x, z);
		TFMagicMapData mapdata = new TFMagicMapData(pos.x(), pos.z(), (byte) scale, trackingPosition, unlimitedTracking, false, dimension);
		TFMagicMapData.registerMagicMapData(level, mapdata, getMapName(freeMapId.id()));
		stack.set(DataComponents.MAP_ID, freeMapId);
		// Codex fix: prefill the entire 128×128 map at creation time so the player
		// sees the full magic map immediately, instead of only the viewer's 32-radius
		// patch (which leaves new maps looking 99% empty until the player walks the
		// 2048×2048 grid). Upstream relied on the player gradually filling the map,
		// which produced an empty-looking map on first use; this prefill matches the
		// effective "always-filled" feel.
		if (!level.isClientSide()) {
			prefillEntireMap(level, mapdata);
		}
		return mapdata;
	}

	private static void prefillEntireMap(Level level, TFMagicMapData data) {
		int blocksPerPixel = 16;
		int centerX = data.centerX;
		int centerZ = data.centerZ;

		Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		for (int xPixel = 0; xPixel < 128; ++xPixel) {
			for (int zPixel = 0; zPixel < 128; ++zPixel) {
				int worldX = (centerX / blocksPerPixel + xPixel - 64) * blocksPerPixel;
				int worldZ = (centerZ / blocksPerPixel + zPixel - 64) * blocksPerPixel;
				Holder<Biome> biome = level.getBiome(new BlockPos(worldX, 0, worldZ));
				MagicMapBiomeColor cb = TFDataMaps.getMagicMapBiomeColor(biome);
				if (cb == null) cb = new MagicMapBiomeColor(MapColor.COLOR_MAGENTA);
				byte newPixel = (byte) (cb.color().id * 4 + cb.brightness());
				if (data.colors[xPixel + zPixel * 128] != newPixel) {
					data.setColor(xPixel, zPixel, newPixel);
				}

				if (LegacyLandmarkPlacements.blockIsInLandmarkCenter(worldX, worldZ)) {
					ResourceKey<Structure> structureKey = LegacyLandmarkPlacements.pickLandmarkAtBlock(worldX, worldZ, level);
					structureRegistry.getHolder(structureKey).ifPresent(structureRef -> {
						if (structureRef.is(StructureTagGenerator.LANDMARK) && structureRef.value() instanceof LandmarkStructure landmark) {
							landmark.getMapIcon().ifPresent(icon -> data.addTFDecoration(icon, level, makeName(icon, worldX, worldZ), worldX, worldZ, 0.0F, LandmarkUtil.isConquered(level, worldX, worldZ)));
						}
					});
				}
			}
		}
		data.setDirty();
	}

	public static String getMapName(int id) {
		return STR_ID + "_" + id;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.get(DataComponents.MAP_ID) == null) {
			if (!level.isClientSide()) {
				getCustomMapData(stack, level);
			}
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		return super.use(level, player, hand);
	}

	@Override
	public void update(Level level, Entity viewer, MapItemSavedData data) {
		if (level.dimension() == data.dimension && viewer instanceof Player && !level.isClientSide()) {
			int biomesPerPixel = 4;
			int blocksPerPixel = 16;
			int centerX = data.centerX;
			int centerZ = data.centerZ;
			int viewerX = Mth.floor(viewer.getX() - centerX) / blocksPerPixel + 64;
			int viewerZ = Mth.floor(viewer.getZ() - centerZ) / blocksPerPixel + 64;
			// Codex fix: was 512 / 16 = 32 (only the viewer's 32-radius patch updates per
			// tick, so newly-created maps look 99% empty until the player walks the
			// 2048×2048 grid). Bumped to 256 (large enough that the in-bounds distance
			// check `xDist² + zDist² < viewRadiusPixels²` and the fuzz boundary
			// `xDist² + zDist² > (viewRadiusPixels-2)²` both pass for every pixel even
			// when viewer is at a map corner, so every inventoryTick refreshes the full
			// 128×128 map — matches the "always-filled" feel reported as upstream.
			int viewRadiusPixels = 256;
			int startX = (centerX / blocksPerPixel - 64) * biomesPerPixel;
			int startZ = (centerZ / blocksPerPixel - 64) * biomesPerPixel;
			Holder<Biome>[] biomes = CACHE.computeIfAbsent(new ChunkPos(startX, startZ), pos -> {
				@SuppressWarnings({"unchecked", "rawtypes"})
				Holder<Biome>[] array = new Holder[128 * biomesPerPixel * 128 * biomesPerPixel];
				for (int z = 0; z < 128 * biomesPerPixel; ++z) {
					for (int x = 0; x < 128 * biomesPerPixel; ++x) {
						array[z * 128 * biomesPerPixel + x] = level.getBiome(new BlockPos(startX * biomesPerPixel + x * biomesPerPixel, 0, startZ * biomesPerPixel + z * biomesPerPixel));
					}
				}
				return array;
			});

			Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
			for (int xPixel = viewerX - viewRadiusPixels + 1; xPixel < viewerX + viewRadiusPixels; ++xPixel) {
				for (int zPixel = viewerZ - viewRadiusPixels - 1; zPixel < viewerZ + viewRadiusPixels; ++zPixel) {
					if (xPixel >= 0 && zPixel >= 0 && xPixel < 128 && zPixel < 128) {
						int xPixelDist = xPixel - viewerX;
						int zPixelDist = zPixel - viewerZ;
						boolean shouldFuzz = xPixelDist * xPixelDist + zPixelDist * zPixelDist > (viewRadiusPixels - 2) * (viewRadiusPixels - 2);
						int biomeIndex = xPixel * biomesPerPixel + zPixel * biomesPerPixel * 128 * biomesPerPixel;
						Holder<Biome> biome = biomes[biomeIndex];
						Holder<Biome> overBiome = biomes[Math.min(biomes.length - 1, biomeIndex + 1)];
						Holder<Biome> downBiome = biomes[Math.min(biomes.length - 1, biomeIndex + 128 * biomesPerPixel)];
						biome = overBiome != null && overBiome.is(TFBiomes.STREAM) ? overBiome : downBiome != null && downBiome.is(TFBiomes.STREAM) ? downBiome : biome;
						MagicMapBiomeColor colorBrightness = this.getMapColorPerBiome(biome);
						MapColor mapcolor = colorBrightness.color();
						int brightness = colorBrightness.brightness();
						if (xPixelDist * xPixelDist + zPixelDist * zPixelDist < viewRadiusPixels * viewRadiusPixels && (!shouldFuzz || (xPixel + zPixel & 1) != 0)) {
							byte oldPixel = data.colors[xPixel + zPixel * 128];
							byte newPixel = (byte) (mapcolor.id * 4 + brightness);
							if (oldPixel != newPixel) {
								data.setColor(xPixel, zPixel, newPixel);
								data.setDirty();
							}
							int worldX = (centerX / blocksPerPixel + xPixel - 64) * blocksPerPixel;
							int worldZ = (centerZ / blocksPerPixel + zPixel - 64) * blocksPerPixel;
							if (LegacyLandmarkPlacements.blockIsInLandmarkCenter(worldX, worldZ)) {
								ResourceKey<Structure> structureKey = LegacyLandmarkPlacements.pickLandmarkAtBlock(worldX, worldZ, level);
								structureRegistry.getHolder(structureKey).ifPresent(structureRef -> {
									if (structureRef.is(StructureTagGenerator.LANDMARK) && structureRef.value() instanceof LandmarkStructure landmark) {
										landmark.getMapIcon().ifPresent(icon -> ((TFMagicMapData) data).addTFDecoration(icon, level, makeName(icon, worldX, worldZ), worldX, worldZ, 0.0F, LandmarkUtil.isConquered(level, worldX, worldZ)));
									}
								});
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
		if (!level.isClientSide()) {
			TFMagicMapData mapdata = getCustomMapData(stack, level);
			if (mapdata != null) {
				if (entity instanceof Player player) {
					mapdata.tickCarriedBy(player, stack);
				}

				if (!mapdata.locked && (isSelected || entity instanceof Player player && player.getOffhandItem() == stack)) {
					this.update(level, entity, mapdata);
				}
			}
		}
	}

	public static String makeName(Holder<MapDecorationType> type, int x, int z) {
		return type.value().assetId() + "_" + x + "_" + z;
	}

	private MagicMapBiomeColor getMapColorPerBiome(Holder<Biome> biome) {
		MagicMapBiomeColor color = TFDataMaps.getMagicMapBiomeColor(biome);
		return color != null ? color : new MagicMapBiomeColor(MapColor.COLOR_MAGENTA);
	}

	@Override
	public void onCraftedBy(ItemStack stack, Level level, Player player) {
	}

	@Override
	@Nullable
	public Packet<?> getUpdatePacket(ItemStack stack, Level level, Player player) {
		TFMagicMapData mapdata = getCustomMapData(stack, level);
		MapId mapId = stack.get(DataComponents.MAP_ID);
		return mapId == null || mapdata == null ? null : mapdata.getUpdatePacket(mapId, player);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		MapId mapId = stack.get(DataComponents.MAP_ID);
		if (mapId != null) {
			if (flag.isAdvanced()) {
				MapItemSavedData mapitemsaveddata = TFMagicMapData.getClientMagicMapData(getMapName(mapId.id()));
				if (mapitemsaveddata != null) {
					tooltip.add(Component.translatable("filled_map.id", mapId.id()).withStyle(ChatFormatting.GRAY));
					tooltip.add(Component.translatable("filled_map.scale", 1 << mapitemsaveddata.scale).withStyle(ChatFormatting.GRAY));
					tooltip.add(Component.translatable("filled_map.level", mapitemsaveddata.scale, 4).withStyle(ChatFormatting.GRAY));
				} else {
					tooltip.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
				}
			} else {
				tooltip.add(MapItem.getTooltipForId(mapId));
			}
		}
	}
}
