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

import java.util.List;

/**
 * 1:1 port of upstream {@code twilightforest.init.custom.MagicPaintingVariants}
 * — {@link ResourceKey} table for the 5 datapack-registered Magic Painting
 * variants. Datapacks under {@code data/<ns>/twilight/magic_paintings/<key>.json}
 * supply the actual {@link MagicPaintingVariant} entries; if no JSONs ship the
 * registry remains empty and {@link MagicPaintingVariant#getVariant} returns
 * {@code Optional.empty()}.
 *
 * <p>Codex Fabric port note: the original codex-twilight {@code Q34} simplified
 * variant table (CustomModelData-based, used by {@code MagicPaintingItem}) is
 * preserved as the nested {@link Legacy} class — gameplay backed by paired-client
 * texture overrides keeps working, while the new entity-side ResourceKey API
 * matches upstream 1:1 for {@code MagicPainting} entity dispatch. Bootstrap +
 * datagen helpers ({@code AtlasGenerator.MAGIC_PAINTING_HELPER},
 * {@code LangGenerator.MAGIC_PAINTING_HELPER}) are not ported — those run only
 * in upstream's Forge datagen pipeline; codex ships precomputed JSON if needed.</p>
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

	/**
	 * Codex-twilight legacy CustomModelData-based variant table — preserved verbatim
	 * for {@code MagicPaintingItem} cycling logic. Each variant has a distinct CMD
	 * value so paired clients with the matching CMD overrides on
	 * {@code minecraft:painting} can render the variant background.
	 */
	public static final class Legacy {

		public record Variant(String id, int cmd) {}

		public static final Variant CASTAWAY_PARADISE = new Variant("castaway_paradise", 32001);
		public static final Variant DARKNESS = new Variant("darkness", 32002);
		public static final Variant LUCID_LANDS = new Variant("lucid_lands", 32003);
		public static final Variant MUSIC_IN_THE_MIRE = new Variant("music_in_the_mire", 32004);
		public static final Variant THE_HOSTILE_PARADISE = new Variant("the_hostile_paradise", 32005);

		public static final List<Variant> ALL = List.of(
				CASTAWAY_PARADISE, DARKNESS, LUCID_LANDS, MUSIC_IN_THE_MIRE, THE_HOSTILE_PARADISE);

		public static final Variant DEFAULT = CASTAWAY_PARADISE;

		private Legacy() {}

		public static Variant byId(String id) {
			for (Variant v : ALL) {
				if (v.id().equals(id)) return v;
			}
			return DEFAULT;
		}

		public static Variant byCmd(int cmd) {
			for (Variant v : ALL) {
				if (v.cmd() == cmd) return v;
			}
			return DEFAULT;
		}

		/** Returns the next variant after the one with this CMD, wrapping. */
		public static Variant nextAfter(int cmd) {
			for (int i = 0; i < ALL.size(); i++) {
				if (ALL.get(i).cmd() == cmd) {
					return ALL.get((i + 1) % ALL.size());
				}
			}
			return DEFAULT;
		}
	}
}
