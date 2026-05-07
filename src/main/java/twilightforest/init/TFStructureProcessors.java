package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.feature.templates.GraveyardFeature;
import twilightforest.world.components.processors.*;
import twilightforest.world.components.structures.courtyard.CourtyardTerraceTemplateProcessor;

import java.util.function.Supplier;

public final class TFStructureProcessors {
    public static final TFRegistryObject<StructureProcessorType<CobbleVariants>> COBBLE_VARIANTS = processor("cobble_variants", () -> CobbleVariants.CODEC);
    public static final TFRegistryObject<StructureProcessorType<SmoothStoneVariants>> SMOOTH_STONE_VARIANTS = processor("smooth_stone_variants", () -> SmoothStoneVariants.CODEC);
    public static final TFRegistryObject<StructureProcessorType<StoneBricksVariants>> STONE_BRICK_VARIANTS = processor("stone_brick_variants", () -> StoneBricksVariants.CODEC);
    public static final TFRegistryObject<StructureProcessorType<NagastoneVariants>> NAGASTONE_VARIANTS = processor("nagastone_variants", () -> NagastoneVariants.CODEC);
    public static final TFRegistryObject<StructureProcessorType<StateTransfiguringProcessor>> STATE_TRANSFIGURING = processor("state_transfiguring", () -> StateTransfiguringProcessor.CODEC);
    public static final TFRegistryObject<StructureProcessorType<WoodPaletteSwizzle>> PLANK_SWIZZLE = processor("wood_swizzle", () -> WoodPaletteSwizzle.CODEC);
    public static final TFRegistryObject<StructureProcessorType<SmartGrassProcessor>> SMART_GRASS = processor("smart_grass", () -> SmartGrassProcessor.CODEC);
    public static final TFRegistryObject<StructureProcessorType<BoxCuttingProcessor>> BOX_CUTTING_PROCESSOR = processor("box_cutting", () -> BoxCuttingProcessor.CODEC);
    public static final TFRegistryObject<StructureProcessorType<TargetedRotProcessor>> TARGETED_ROT = processor("targeted_rot", () -> TargetedRotProcessor.CODEC);
    public static final TFRegistryObject<StructureProcessorType<CourtyardTerraceTemplateProcessor>> COURTYARD_TERRACE = processor("courtyard_terrace", () -> CourtyardTerraceTemplateProcessor.CODEC);
    public static final TFRegistryObject<StructureProcessorType<GraveyardFeature.WebTemplateProcessor>> WEB = processor("web", () -> GraveyardFeature.WebTemplateProcessor.CODEC);

    private TFStructureProcessors() {
    }

    public static void bootstrap() {
    }

    private static <P extends StructureProcessor> TFRegistryObject<StructureProcessorType<P>> processor(String path, Supplier<Codec<P>> codec) {
        MapCodec<P> mapCodec = codec.get().fieldOf("config");
        StructureProcessorType<P> type = () -> mapCodec;
        ResourceKey<StructureProcessorType<?>> key = ResourceKey.create(BuiltInRegistries.STRUCTURE_PROCESSOR.key(), TwilightForestMod.prefix(path));
        @SuppressWarnings({"unchecked", "rawtypes"})
        StructureProcessorType<P> registered = (StructureProcessorType<P>) Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, key.location(), type);
        @SuppressWarnings({"unchecked", "rawtypes"})
        TFRegistryObject<StructureProcessorType<P>> holder = new TFRegistryObject(registered, key);
        return holder;
    }
}
