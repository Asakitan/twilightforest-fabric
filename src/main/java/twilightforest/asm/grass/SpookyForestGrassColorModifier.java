package twilightforest.asm.grass;

import twilightforest.world.components.BiomeGrassColors;

public final class SpookyForestGrassColorModifier {
    public int modifyColor(double x, double z, int color) {
        return BiomeGrassColors.spookyGrass(x, z);
    }
}
