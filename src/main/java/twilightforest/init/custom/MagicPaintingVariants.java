package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.entity.MagicPaintingVariant;

/**
 * 1:1 port of upstream {@code twilightforest.init.custom.MagicPaintingVariants}
 * — {@link ResourceKey} table for the 5 datapack-registered Magic Painting
 * variants. Datapacks under {@code data/<ns>/twilight/magic_painting_variant/<key>.json}
 * supply the actual {@link MagicPaintingVariant} entries.
 */
public class MagicPaintingVariants {
	public static final Codec<Holder<MagicPaintingVariant>> CODEC = RegistryFileCodec.create(TFRegistries.Keys.MAGIC_PAINTINGS, MagicPaintingVariant.CODEC, false);
	public static final StreamCodec<? super RegistryFriendlyByteBuf, Holder<MagicPaintingVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(TFRegistries.Keys.MAGIC_PAINTINGS);

	public static final ResourceKey<MagicPaintingVariant> DARKNESS = makeKey(TwilightForestMod.prefix("darkness"));
	public static final ResourceKey<MagicPaintingVariant> LUCID_LANDS = makeKey(TwilightForestMod.prefix("lucid_lands"));
	public static final ResourceKey<MagicPaintingVariant> THE_HOSTILE_PARADISE = makeKey(TwilightForestMod.prefix("the_hostile_paradise"));
	public static final ResourceKey<MagicPaintingVariant> CASTAWAY_PARADISE = makeKey(TwilightForestMod.prefix("castaway_paradise"));
	public static final ResourceKey<MagicPaintingVariant> MUSIC_IN_THE_MIRE = makeKey(TwilightForestMod.prefix("music_in_the_mire"));

	public static final ResourceKey<MagicPaintingVariant> DEFAULT = MagicPaintingVariants.LUCID_LANDS;

	private static ResourceKey<MagicPaintingVariant> makeKey(ResourceLocation name) {
		return ResourceKey.create(TFRegistries.Keys.MAGIC_PAINTINGS, name);
	}

	private MagicPaintingVariants() {}
}
