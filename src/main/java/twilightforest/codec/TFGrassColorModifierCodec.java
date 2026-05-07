package twilightforest.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;

/**
 * Wraps the vanilla {@code GrassColorModifier.CODEC} so encode-to-network produces a
 * vanilla-compatible string for the five Twilight Forest custom enum values added by
 * {@link twilightforest.asm.CodexGrassColorEarlyRiser}.
 *
 * <p>Decode is delegated unchanged: the vanilla {@code StringRepresentable.fromEnum}
 * codec already accepts the new {@code "twilightforest:*"} names because the extended
 * enum's {@code values()} contains them with matching {@code getSerializedName()}.</p>
 *
 * <p>Encode swaps each TF entry to its closest vanilla {@code GrassColorModifier}
 * before delegating, so any consumer ({@code BiomeSpecialEffects.CODEC} during the
 * configuration / registry-sync packet, datapack-export tools, or {@code /datapack}
 * round-trips) sees a value the unmodified Minecraft codec can re-serialise:</p>
 * <ul>
 *   <li>{@code twilightforest:dark_forest}        → {@code dark_forest}</li>
 *   <li>{@code twilightforest:dark_forest_center} → {@code dark_forest}</li>
 *   <li>{@code twilightforest:enchanted_forest}   → {@code dark_forest}</li>
 *   <li>{@code twilightforest:spooky_forest}      → {@code dark_forest}</li>
 *   <li>{@code twilightforest:swamp}              → {@code swamp}</li>
 * </ul>
 *
 * <p>This is the Phase-1 "vanilla-approximate" path. The position-independent
 * formulas (DarkForest, Swamp) match vanilla output very closely after this swap;
 * the three position-dependent ones (DarkForestCenter, EnchantedForest, SpookyForest)
 * lose their per-block variation and fall back to vanilla {@code dark_forest} tinting.
 * Phase 2 ({@code Block} static colour patches + {@code Display} ambience) will
 * recover the rest of the look without touching this codec.</p>
 */
public final class TFGrassColorModifierCodec {

    private TFGrassColorModifierCodec() {
    }

    public static Codec<GrassColorModifier> wrap(Codec<GrassColorModifier> vanilla) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<GrassColorModifier, T>> decode(DynamicOps<T> ops, T input) {
                // Intercept the raw string before the vanilla StringRepresentable lookup.
                // If it is one of the five Twilight Forest custom ids we substitute the
                // nearest vanilla {@link GrassColorModifier} and report success, so the
                // server can finish loading the {@code twilightforest:*} biome JSONs that
                // would otherwise trip vanilla with "Unknown element name". Anything else
                // falls through to vanilla decode unchanged.
                DataResult<String> asString = ops.getStringValue(input);
                if (asString.result().isPresent()) {
                    String raw = asString.result().get();
                    GrassColorModifier mapped = mapTfIdToVanilla(raw);
                    if (mapped != null) {
                        return DataResult.success(Pair.of(mapped, ops.empty()));
                    }
                }
                return vanilla.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(GrassColorModifier value, DynamicOps<T> ops, T prefix) {
                return vanilla.encode(swapToVanilla(value), ops, prefix);
            }
        };
    }

    /**
     * Returns the vanilla {@link GrassColorModifier} equivalent for one of the five
     * Twilight Forest custom modifier ids, or {@code null} if {@code raw} is not a
     * recognised TF id (and should therefore fall through to vanilla decoding).
     */
    private static GrassColorModifier mapTfIdToVanilla(String raw) {
        if (raw == null || !raw.startsWith("twilightforest:")) {
            return null;
        }
        return switch (raw) {
            case "twilightforest:swamp" -> GrassColorModifier.SWAMP;
            case "twilightforest:dark_forest",
                 "twilightforest:dark_forest_center",
                 "twilightforest:enchanted_forest",
                 "twilightforest:spooky_forest" -> GrassColorModifier.DARK_FOREST;
            default -> null; // unknown twilightforest:* id, let vanilla raise its own error
        };
    }

    private static GrassColorModifier swapToVanilla(GrassColorModifier value) {
        // After Phase 2 lands a real {@code GrassColorModifier} subclass via MM enum
        // extension, this guard will start matching real TF subclass instances. Until then
        // the substitution all happens at decode time and this branch is dormant; we still
        // defend against the case where an upstream caller hands us a custom modifier that
        // doesn't match the vanilla 3-value enum — encode it as the closest vanilla string
        // so packet codecs never emit an unknown id.
        String name = value.getSerializedName();
        if (name == null || !name.startsWith("twilightforest:")) {
            return value;
        }
        GrassColorModifier mapped = mapTfIdToVanilla(name);
        return mapped != null ? mapped : GrassColorModifier.NONE;
    }
}
