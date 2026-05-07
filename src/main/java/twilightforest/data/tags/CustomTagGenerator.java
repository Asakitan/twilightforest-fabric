package twilightforest.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import twilightforest.TwilightForestMod;

public final class CustomTagGenerator {
    private CustomTagGenerator() {
    }

    public static final class DimensionTypeTagGenerator {
        public static final TagKey<DimensionType> ALLOWS_MAGIC_MAP_CHARTING =
            TagKey.create(Registries.DIMENSION_TYPE, TwilightForestMod.prefix("allows_magic_map_charting"));

        private DimensionTypeTagGenerator() {
        }
    }
}
