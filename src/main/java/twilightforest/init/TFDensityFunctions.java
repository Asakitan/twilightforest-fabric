package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.DensityFunction;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.chunkgenerators.AbsoluteDifferenceFunction;
import twilightforest.world.components.chunkgenerators.BoxDensityFunction;
import twilightforest.world.components.chunkgenerators.FocusedDensityFunction;
import twilightforest.world.components.chunkgenerators.HollowHillFunction;
import twilightforest.world.components.chunkgenerators.NoiseDensityRouter;
import twilightforest.world.components.chunkgenerators.SqrtDensityFunction;
import twilightforest.world.components.chunkgenerators.TerrainDensityRouter;

/**
 * Registers TF custom DensityFunction codec types into BuiltInRegistries.DENSITY_FUNCTION_TYPE.
 *
 * Names match TF's TFDensityFunctions registrations 1:1.
 */
public final class TFDensityFunctions {
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> BIOME_DRIVEN_TERRAIN =
            register("biome_driven_terrain", TerrainDensityRouter.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> BIOME_DRIVEN_NOISE =
            register("biome_driven_noise", NoiseDensityRouter.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> FOCUSED =
            register("focused", FocusedDensityFunction.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> HOLLOW_HILL =
            register("hollow_hill", HollowHillFunction.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> COORD_MIN =
            register("coord_min", AbsoluteDifferenceFunction.Min.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> COORD_MAX =
            register("coord_max", AbsoluteDifferenceFunction.Max.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> SQRT =
            register("sqrt", SqrtDensityFunction.CODEC);
    public static final TFRegistryObject<MapCodec<? extends DensityFunction>> BOX_FUNCTION =
            register("box_function", BoxDensityFunction.CODEC);

    private TFDensityFunctions() {
    }

    public static void bootstrap() {
        BIOME_DRIVEN_TERRAIN.get();
        BIOME_DRIVEN_NOISE.get();
        FOCUSED.get();
        HOLLOW_HILL.get();
        COORD_MIN.get();
        COORD_MAX.get();
        SQRT.get();
        BOX_FUNCTION.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TFRegistryObject<MapCodec<? extends DensityFunction>> register(String path, MapCodec<? extends DensityFunction> codec) {
        MapCodec<? extends DensityFunction> registered = (MapCodec<? extends DensityFunction>) Registry.register(
                BuiltInRegistries.DENSITY_FUNCTION_TYPE,
                TwilightForestMod.prefix(path),
                (MapCodec) codec);
        return new TFRegistryObject(registered);
    }
}
