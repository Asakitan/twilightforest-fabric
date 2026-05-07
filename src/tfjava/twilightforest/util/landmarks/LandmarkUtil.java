package twilightforest.util.landmarks;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import twilightforest.data.tags.StructureTagGenerator;
import twilightforest.world.components.structures.start.TFStructureStart;
import twilightforest.world.components.structures.util.CustomStructureData;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class LandmarkUtil {
	private LandmarkUtil() {
	}

	public static Optional<StructureStart> locateNearestLandmarkStart(LevelAccessor level, int chunkX, int chunkZ) {
		return locateNearestMatchingLandmark(level, StructureTagGenerator.LANDMARK, chunkX, chunkZ);
	}

	public static Optional<StructureStart> locateNearestMatchingLandmark(LevelAccessor level, TagKey<Structure> matching, int chunkX, int chunkZ) {
		var structureRegistry = level.registryAccess().registry(Registries.STRUCTURE);
		if (structureRegistry.isEmpty()) return Optional.empty();
		var holders = structureRegistry.get().getTag(matching);
		if (holders.isEmpty()) return Optional.empty();
		return locateNearestMatchingLandmark(level, holders.get(), chunkX, chunkZ);
	}

	public static Optional<StructureStart> locateNearestMatchingLandmark(LevelAccessor level, HolderSet<Structure> matching, int chunkX, int chunkZ) {
		Set<Structure> structures = matching.stream().map(Holder::value).collect(Collectors.toSet());
		return locateNearestMatchingLandmark(level, structures::contains, chunkX, chunkZ, true);
	}

	public static Optional<StructureStart> locateNearestMatchingLandmark(LevelAccessor level, Predicate<Structure> filter, int chunkX, int chunkZ, boolean checkReady) {
		net.minecraft.core.BlockPos nearestFeature = LegacyLandmarkPlacements.getNearestCenterXZ(chunkX, chunkZ);
		int centerX = net.minecraft.core.SectionPos.blockToSectionCoord(nearestFeature.getX());
		int centerZ = net.minecraft.core.SectionPos.blockToSectionCoord(nearestFeature.getZ());
		if (checkReady && !level.hasChunk(centerX, centerZ)) return Optional.empty();
		ChunkAccess chunkAccess = level.getChunk(centerX, centerZ, ChunkStatus.STRUCTURE_STARTS);
		for (java.util.Map.Entry<Structure, StructureStart> structureEntry : chunkAccess.getAllStarts().entrySet()) {
			if (filter.test(structureEntry.getKey())) {
				return Optional.of(structureEntry.getValue());
			}
		}
		return Optional.empty();
	}

	public static boolean isConquered(Level level, int blockX, int blockZ) {
		Optional<StructureStart> start = locateNearestMatchingLandmark(level, s -> s instanceof CustomStructureData, blockX >> 4, blockZ >> 4, false);
		return start.filter(structureStart -> structureStart instanceof TFStructureStart tfStructureStart && tfStructureStart.isConquered()).isPresent();
	}

	public static Optional<StructureStart> locateNearestLandmarkStart(LevelAccessor level, Structure structure, int chunkX, int chunkZ) {
		net.minecraft.core.BlockPos nearestLandmark = LegacyLandmarkPlacements.getNearestCenterXZ(chunkX, chunkZ);
		ChunkAccess chunkAccess = level.getChunk(net.minecraft.core.SectionPos.blockToSectionCoord(nearestLandmark.getX()), net.minecraft.core.SectionPos.blockToSectionCoord(nearestLandmark.getZ()), ChunkStatus.STRUCTURE_STARTS);
		for (Long packedChunkPos : chunkAccess.getReferencesForStructure(structure)) {
			int packedX = ChunkPos.getX(packedChunkPos);
			int packedZ = ChunkPos.getZ(packedChunkPos);
			if (level.hasChunk(packedX, packedZ)) {
				StructureStart structureStart = level.getChunk(packedX, packedZ, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(structure);
				if (structureStart != null && structureStart.isValid()) return Optional.of(structureStart);
			}
		}
		return Optional.empty();
	}

	public static boolean isProgressionEnforced(Level level) {
		return twilightforest.config.TFConfig.enforcedProgression;
	}

	/**
	 * P5.e stub for upstream's structure-conquered marker. Upstream walks the structure
	 * piece tree and stamps a "conquered" flag so the structure no longer contains live
	 * spawners. Codex's progression is purely TFConfig-driven so this is a no-op for
	 * runtime correctness; full landmark-conquest integration is a separate phase.
	 */
	public static void markStructureConquered(net.minecraft.server.level.ServerLevel serverLevel,
	                                          net.minecraft.world.entity.Entity boss,
	                                          net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> structureKey,
	                                          boolean conquered) {
		// no-op stub
	}
}
