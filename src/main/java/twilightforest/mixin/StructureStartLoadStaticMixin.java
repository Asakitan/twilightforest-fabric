package twilightforest.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.asmhooks.WorldgenHooks;
import twilightforest.world.components.structures.start.TFStructureStart;
import twilightforest.world.components.structures.util.CustomStructureData;

/**
 * Codex conquered-flag persistence fix.
 *
 * <p>Upstream NeoForge ASM-patches {@code StructureStart.loadStaticStart} to call
 * {@link WorldgenHooks#loadStaticStart} so that any {@code CustomStructureData}
 * structure (every TF conquerable / progression-locked structure) gets
 * deserialized as a {@link TFStructureStart} that preserves the {@code conquered}
 * NBT flag across chunk save→load round-trips. The ASM patch is at
 * {@code twilightforest.asm.transformers.conquered.StructureStartLoadStaticTransformer}.
 *
 * <p>Without this hook, after any chunk-unload / server-restart cycle the
 * StructureStart in memory is a plain {@link StructureStart}, the saved
 * {@code conquered: true} tag is silently dropped, and
 * {@link twilightforest.util.landmarks.LandmarkUtil#markStructureConquered}
 * silently returns early (its {@code instanceof TFStructureStart} guard fails)
 * — so {@code STRUCTURE_CLEARED} advancement trigger never fires for subsequent
 * boss kills, the magic-circle progression-lock state can't be reset, and the
 * Magic Map's red-X conquered marker reverts to plain on every reload.
 *
 * <p>Strategy: inject at RETURN on the vanilla static factory. If the returned
 * StructureStart belongs to a {@code CustomStructureData} structure, delegate to
 * the upstream hook which returns the proper TFStructureStart subclass loaded
 * from the NBT — including the {@code conquered} field.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartLoadStaticMixin {

    @Inject(method = "loadStaticStart", at = @At("RETURN"), cancellable = true)
    private static void codex_twilight$rewrapAsTFStructureStart(StructurePieceSerializationContext context, CompoundTag tag, long seed, CallbackInfoReturnable<StructureStart> cir) {
        StructureStart returned = cir.getReturnValue();
        if (returned == null || returned == StructureStart.INVALID_START) return;
        if (!(returned.getStructure() instanceof CustomStructureData)) return;
        // Rebuild the PiecesContainer the same way vanilla does so we can hand the
        // exact same content to forDeserialization. Vanilla already constructed a
        // PiecesContainer to pass to the StructureStart ctor; re-derive it from the
        // returned start's pieces so we don't depend on a non-public field.
        PiecesContainer pieces = new PiecesContainer(returned.getPieces());
        StructureStart replacement = WorldgenHooks.loadStaticStart(returned, pieces, tag);
        if (replacement != null && replacement != returned) {
            cir.setReturnValue(replacement);
        }
    }
}
