package twilightforest.asm.grass;

import twilightforest.world.components.BiomeGrassColors;

public final class SwampGrassColorModifier extends GrassColorModifierStructMixin {
    public int modifyColor(double x, double z, int color) {
        return BiomeGrassColors.swamp(BiomeGrassColors.Type.GRASS);
    }

    // Manningham Mills copies this method verbatim into the runtime enum subclass.
    public int method_30823(double x, double z, int color) {
        return this.modifyColor(x, z, color);
    }
}
