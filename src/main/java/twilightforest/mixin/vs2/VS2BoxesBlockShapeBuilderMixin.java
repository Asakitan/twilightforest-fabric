package twilightforest.mixin.vs2;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Suppress VS2's per-pair "solid box must not intersect negative box" WARN.
 *
 * <p>Why: VS2's {@code BoxesBlockShapeImpl.BuilderImpl#build()} validates that
 * the positive and negative AABB lists it self-sampled from a vanilla
 * {@code VoxelShape} are disjoint. When VS2 samples a TF block whose shape is
 * built via {@code Shapes.or(...)} of overlapping pillars + slabs (candelabra,
 * banister, hollow log, trophy, etc.) the sampler produces overlapping pos/neg
 * boxes, and VS2 fires this WARN once per intersecting pair. Bytecode shows the
 * {@code build()} method then proceeds normally and produces a working
 * {@code BoxesBlockShape} — the WARN is purely informational.
 *
 * <p>Observed in remote server log: 9629 hits in one session, dwarfing every
 * other warning category combined. The shape that VS2 actually uses for ship
 * collision is unchanged.
 *
 * <p>Why this fix is safe under "no mod feature loss / no content change":
 * <ul>
 *   <li>We do not modify any TF block's {@code VoxelShape}, so vanilla
 *       collision/visual remains identical.</li>
 *   <li>We do not modify VS2's shape construction either — the same pos/neg
 *       AABB lists go into the resulting {@code BoxesBlockShape}, so VS2 ship
 *       physics interaction with TF blocks is identical.</li>
 *   <li>We only redirect the {@code Logger.warn} call to {@code Logger.debug}
 *       so the message is hidden from default log levels.</li>
 * </ul>
 *
 * <p>Why server-only: this mixin lives in the common (non-client) source set.
 * VS2 runs both client and server, but the warning fires from VS2's shape
 * sampler which only touches the server-side block shapes; suppressing on the
 * common side covers both. No client-mod requirement added.
 *
 * <p>Conditional load: registered through {@code codex_twilight_vs2.mixins.json}
 * with {@code "defaultRequire": 0}, so if VS2 is absent (e.g. dev environment
 * without the VS jar) the mixin gracefully no-ops instead of crashing the mod.
 */
@Mixin(
    targets = "org.valkyrienskies.core.impl.api_impl.physics.blockstates.BoxesBlockShapeImpl$BuilderImpl",
    remap = false
)
public abstract class VS2BoxesBlockShapeBuilderMixin {

    @Redirect(
        method = "build",
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V",
            remap = false
        ),
        remap = false
    )
    private void codex$demoteOverlapWarn(Logger logger, String message) {
        // Demote to debug so the message survives for diagnostic builds (run with
        // -Dlog4j.logger.org.valkyrienskies.core.impl.api_impl.physics.blockstates.BoxesBlockShapeImpl=DEBUG)
        // but vanishes from default INFO/WARN log output.
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}
