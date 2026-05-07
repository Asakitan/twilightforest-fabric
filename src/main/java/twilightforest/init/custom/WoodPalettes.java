package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.util.WoodPalette;

import java.util.Locale;

public final class WoodPalettes {
    public static final Codec<Holder<WoodPalette>> CODEC =
            RegistryFileCodec.create(TFRegistries.Keys.WOOD_PALETTES, WoodPalette.CODEC, false);

    public static final ResourceKey<WoodPalette> OAK = makeKey(ResourceLocation.withDefaultNamespace("oak"));
    public static final ResourceKey<WoodPalette> SPRUCE = makeKey(ResourceLocation.withDefaultNamespace("spruce"));
    public static final ResourceKey<WoodPalette> BIRCH = makeKey(ResourceLocation.withDefaultNamespace("birch"));
    public static final ResourceKey<WoodPalette> JUNGLE = makeKey(ResourceLocation.withDefaultNamespace("jungle"));
    public static final ResourceKey<WoodPalette> ACACIA = makeKey(ResourceLocation.withDefaultNamespace("acacia"));
    public static final ResourceKey<WoodPalette> DARK_OAK = makeKey(ResourceLocation.withDefaultNamespace("dark_oak"));
    public static final ResourceKey<WoodPalette> CRIMSON = makeKey(ResourceLocation.withDefaultNamespace("crimson"));
    public static final ResourceKey<WoodPalette> WARPED = makeKey(ResourceLocation.withDefaultNamespace("warped"));
    public static final ResourceKey<WoodPalette> VANGROVE = makeKey(ResourceLocation.withDefaultNamespace("mangrove"));

    public static final ResourceKey<WoodPalette> TWILIGHT_OAK = makeKey("twilight_oak");
    public static final ResourceKey<WoodPalette> CANOPY = makeKey("canopy");
    public static final ResourceKey<WoodPalette> MANGROVE = makeKey("mangrove");
    public static final ResourceKey<WoodPalette> DARKWOOD = makeKey("darkwood");
    public static final ResourceKey<WoodPalette> TIMEWOOD = makeKey("timewood");
    public static final ResourceKey<WoodPalette> TRANSWOOD = makeKey("transwood");
    public static final ResourceKey<WoodPalette> MINEWOOD = makeKey("minewood");
    public static final ResourceKey<WoodPalette> SORTWOOD = makeKey("sortwood");

    private WoodPalettes() {
    }

    private static ResourceKey<WoodPalette> makeKey(String name) {
        return makeKey(TwilightForestMod.prefix(name.toLowerCase(Locale.ROOT)));
    }

    private static ResourceKey<WoodPalette> makeKey(ResourceLocation name) {
        return ResourceKey.create(TFRegistries.Keys.WOOD_PALETTES, name);
    }
}
