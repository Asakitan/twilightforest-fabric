package twilightforest.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.codec.TFGrassColorModifierCodec;

/**
 * Replaces {@link BiomeSpecialEffects.GrassColorModifier#CODEC} with a wrapper that
 * encodes the five Twilight Forest custom enum values (added by
 * {@link twilightforest.asm.CodexGrassColorEarlyRiser}) as their nearest vanilla
 * equivalent, while delegating decode unchanged.
 *
 * <p>This runs after the vanilla {@code <clinit>} so the wrap captures the original
 * codec — which has already enumerated the extended {@code values()} array, including
 * our 5 TF entries — and just adds an encode-side translation layer.</p>
 *
 * <p>Pair this with the {@code mutable field ... CODEC} entry in
 * {@code codex_twilight.accesswidener}.</p>
 */
@Mixin(BiomeSpecialEffects.GrassColorModifier.class)
public abstract class GrassColorModifierCodecMixin {

    @Mutable
    @Shadow
    @Final
    public static Codec<BiomeSpecialEffects.GrassColorModifier> CODEC;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void codex$wrapCodec(CallbackInfo ci) {
        CODEC = TFGrassColorModifierCodec.wrap(CODEC);
    }
}
