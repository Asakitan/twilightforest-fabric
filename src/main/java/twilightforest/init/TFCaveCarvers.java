package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.TFCavesCarver;

public final class TFCaveCarvers {
    public static final TFRegistryObject<WorldCarver<CaveCarverConfiguration>> TFCAVES = carver("tf_caves", new TFCavesCarver(CaveCarverConfiguration.CODEC, false, BlockStateProvider.simple(Blocks.DIRT)));
    public static final TFRegistryObject<WorldCarver<CaveCarverConfiguration>> HIGHLAND_CAVES = carver("highland_caves", new TFCavesCarver(CaveCarverConfiguration.CODEC, true, BlockStateProvider.simple(Blocks.STONE)));

    private TFCaveCarvers() {
    }

    public static void bootstrap() {
        TFCAVES.get();
        HIGHLAND_CAVES.get();
    }

    private static <C extends CaveCarverConfiguration> TFRegistryObject<WorldCarver<C>> carver(String path, WorldCarver<C> carver) {
        ResourceKey<WorldCarver<?>> key = ResourceKey.create(BuiltInRegistries.CARVER.key(), TwilightForestMod.prefix(path));
        @SuppressWarnings({"unchecked", "rawtypes"})
        WorldCarver<C> registered = (WorldCarver<C>) Registry.register(BuiltInRegistries.CARVER, key.location(), carver);
        @SuppressWarnings({"unchecked", "rawtypes"})
        TFRegistryObject<WorldCarver<C>> holder = new TFRegistryObject(registered, key);
        return holder;
    }
}
