package twilightforest;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;
import twilightforest.world.components.layer.BiomeDensitySource;
import twilightforest.entity.passive.DwarfRabbitVariant;
import twilightforest.entity.passive.TinyBirdVariant;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.structures.StructureSpeleothemConfig;

import java.util.Locale;

public final class TFRegistries {

    // Code-side registry for BiomeLayerType codecs (registered via BiomeLayerTypes.bootstrap())
    public static final Registry<BiomeLayerType> BIOME_LAYER_TYPE =
            FabricRegistryBuilder.createSimple(Keys.BIOME_LAYER_TYPE).buildAndRegister();
    public static final Registry<MapCodec<? extends TravellersModifier>> TRAVELLERS_MODIFIER_TYPE =
            FabricRegistryBuilder.createSimple(Keys.TRAVELLERS_MODIFIER_TYPE).buildAndRegister();

    private TFRegistries() {
    }

    public static final class Keys {
        public static final String REGISTRY_NAMESPACE = "twilight";

        // Code-side registries
        public static final ResourceKey<Registry<BiomeLayerType>> BIOME_LAYER_TYPE =
                ResourceKey.createRegistryKey(namedRegistry("biome_layer_type"));
        public static final ResourceKey<Registry<MapCodec<? extends TravellersModifier>>> TRAVELLERS_MODIFIER_TYPE =
                ResourceKey.createRegistryKey(namedRegistry("travellers_modifier_type"));

        // Data-pack registries (populated by JSON under data/<namespace>/twilight/<path>/<entry>.json)
        public static final ResourceKey<Registry<BiomeLayerFactory>> BIOME_STACK =
                ResourceKey.createRegistryKey(namedRegistry("biome_layer_stack"));
        public static final ResourceKey<Registry<BiomeDensitySource>> BIOME_TERRAIN_DATA =
                ResourceKey.createRegistryKey(namedRegistry("biome_terrain_data"));
        public static final ResourceKey<Registry<StructureSpeleothemConfig>> STRUCTURE_SPELEOTHEM_SETTINGS =
                ResourceKey.createRegistryKey(namedRegistry("structure_speleothem_settings"));
        public static final ResourceKey<Registry<TinyBirdVariant>> TINY_BIRD_VARIANT =
                ResourceKey.createRegistryKey(namedRegistry("tiny_bird_variant"));
        public static final ResourceKey<Registry<DwarfRabbitVariant>> DWARF_RABBIT_VARIANT =
                ResourceKey.createRegistryKey(namedRegistry("dwarf_rabbit_variant"));
        public static final ResourceKey<Registry<TravellersModifier>> TRAVELLERS_MODIFIERS =
                ResourceKey.createRegistryKey(namedRegistry("travellers_modifiers"));
        public static final ResourceKey<Registry<twilightforest.entity.MagicPaintingVariant>> MAGIC_PAINTINGS =
                ResourceKey.createRegistryKey(namedRegistry("magic_painting_variant"));

        private Keys() {
        }

        public static ResourceLocation namedRegistry(String name) {
            return ResourceLocation.fromNamespaceAndPath(REGISTRY_NAMESPACE, name.toLowerCase(Locale.ROOT));
        }
    }
}
