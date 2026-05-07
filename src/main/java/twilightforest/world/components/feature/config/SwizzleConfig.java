package twilightforest.world.components.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import twilightforest.world.components.processors.StateTransfiguringProcessor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record SwizzleConfig(List<ProcessorRule> preprocessingRules) implements FeatureConfiguration {
    public static final Codec<SwizzleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.PASSTHROUGH.optionalFieldOf("target_palettes").forGetter(config -> Optional.<Dynamic<?>>empty()),
            Codec.PASSTHROUGH.listOf().optionalFieldOf("palette_choices", List.of()).forGetter(config -> List.of()),
            ProcessorRule.CODEC.listOf().fieldOf("preprocessing_rules").orElseGet(Collections::emptyList).forGetter(SwizzleConfig::preprocessingRules)
    ).apply(instance, (targetPalettes, paletteChoices, preprocessingRules) -> new SwizzleConfig(preprocessingRules)));

    public void buildAddProcessors(StructurePlaceSettings settings, RandomSource random) {
        if (!this.preprocessingRules().isEmpty()) {
            settings.addProcessor(new StateTransfiguringProcessor(this.preprocessingRules()));
        }
    }
}
