package twilightforest.util.landmarks;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Optional;

/**
 * Stub minimal LandmarkUtil for codex-twilight 1:1 port.
 *
 * <p>The full TF version pulls in TFAdvancements, TFGameRules, EnforcedHomePoint,
 * TFStructureStart, CustomStructureData, StructureTagGenerator — most of which we have not
 * ported. This stub returns {@link Optional#empty()} for every "find nearest landmark" query,
 * which only affects the "don't spawn this feature inside a structure" check used by
 * {@code UndergroundPlantFeature} (and similar). Worst-case visual: an underground plant
 * may rarely appear in the air pocket of a TF structure interior — acceptable.
 *
 * <p>If we ever fully port the landmark/structure system, replace this stub with the
 * original {@code local/twilightforest-1.21.1-src/.../LandmarkUtil.java} byte-for-byte.
 */
public final class LandmarkUtil {
    private LandmarkUtil() {
    }

    public static Optional<StructureStart> locateNearestLandmarkStart(LevelAccessor level, int chunkX, int chunkZ) {
        return Optional.empty();
    }
}
