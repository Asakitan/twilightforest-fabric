package twilightforest.init.custom;

import java.util.List;

/**
 * Q34 simplified replacement for TF's dynamic-registry-driven
 * {@code MagicPaintingVariants}. Original supports parallax/opacity layered
 * paintings via a 24-modifier dynamic registry. Codex-twilight ships a static
 * list of 5 variant names matching the texture folders shipped in the paired
 * client mod under {@code assets/twilightforest/textures/magic_paintings/}.
 *
 * <p>Each variant has a distinct {@code CustomModelData} value so paired
 * clients with the matching CMD overrides on {@code minecraft:painting}
 * (or any vanilla item used as the legacy fallback) can render the variant's
 * background. Until paired client assets supply those overrides, the held item displays
 * the default magic_painting model and the placed entity shows a vanilla
 * painting (Q32 behaviour).</p>
 */
public final class MagicPaintingVariants {

    public record Variant(String id, int cmd) {}

    public static final Variant CASTAWAY_PARADISE = new Variant("castaway_paradise", 32001);
    public static final Variant DARKNESS = new Variant("darkness", 32002);
    public static final Variant LUCID_LANDS = new Variant("lucid_lands", 32003);
    public static final Variant MUSIC_IN_THE_MIRE = new Variant("music_in_the_mire", 32004);
    public static final Variant THE_HOSTILE_PARADISE = new Variant("the_hostile_paradise", 32005);

    public static final List<Variant> ALL = List.of(
            CASTAWAY_PARADISE, DARKNESS, LUCID_LANDS, MUSIC_IN_THE_MIRE, THE_HOSTILE_PARADISE);

    public static final Variant DEFAULT = CASTAWAY_PARADISE;

    private MagicPaintingVariants() {}

    public static Variant byId(String id) {
        for (Variant v : ALL) {
            if (v.id().equals(id)) return v;
        }
        return DEFAULT;
    }

    public static Variant byCmd(int cmd) {
        for (Variant v : ALL) {
            if (v.cmd() == cmd) return v;
        }
        return DEFAULT;
    }

    /** Returns the next variant after the one with this CMD, wrapping. */
    public static Variant nextAfter(int cmd) {
        for (int i = 0; i < ALL.size(); i++) {
            if (ALL.get(i).cmd() == cmd) {
                return ALL.get((i + 1) % ALL.size());
            }
        }
        return DEFAULT;
    }
}
