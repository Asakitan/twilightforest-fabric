package twilightforest.asm.grass;

import twilightforest.world.components.BiomeGrassColors;

public final class SwampGrassColorModifier {
    public int modifyColor(double x, double z, int color) {
        return BiomeGrassColors.swamp(BiomeGrassColors.Type.GRASS);
    }
}
