package twilightforest.world.components;

import net.minecraft.util.Mth;

/**
 * Helper math for the 5 Twilight Forest custom {@code GrassColorModifier} entries.
 *
 * <p>The actual enum values are added at runtime by {@link twilightforest.asm.CodexGrassColorEarlyRiser}
 * via Manningham Mills {@code ClassTinkerers.enumBuilder}. This class only exposes the
 * pure-math helpers used by the {@code ColorModifier} lambdas, so we keep the formulas
 * 1:1 with NeoForge Twilight Forest while staying server-side only.</p>
 */
public final class BiomeGrassColors {

    private BiomeGrassColors() {
    }

    public static int getEnchantedColor(int x, int z) {
        // center of the biome is at % 256 - 8 (1:1 with NeoForge TF)
        int cx = 256 * Math.round((x - 8) / 256F) + 8;
        int cz = 256 * Math.round((z - 8) / 256F) - 8;

        int dist = (int) Mth.sqrt((cx - x) * (cx - x) + (cz - z) * (cz - z));
        int color = dist * 64;
        color %= 512;

        if (color > 255) {
            color = 511 - color;
        }

        color = 255 - color;
        return color;
    }

    public static int blendColors(int a, int b, double ratio) {
        int mask1 = 0x00FF00FF;
        int mask2 = 0xFF00FF00;

        int f2 = (int) (256 * ratio);
        int f1 = 256 - f2;

        return (((((a & mask1) * f1) + ((b & mask1) * f2)) >> 8) & mask1)
                | (((((a & mask2) * f1) + ((b & mask2) * f2)) >> 8) & mask2);
    }
}
