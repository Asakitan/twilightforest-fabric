package twilightforest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;

import java.util.Map;

/**
 * Server pushes a {@code translation_key -> display_name} map for biome names
 * (and any other lang keys derived from the {@code biome.} prefix) so paired
 * clients can render Twilight + third-party mod biomes correctly even when:
 *
 * <ul>
 *   <li>The mod jar omitted biome translations for the player's locale
 *       (terralith, candycraftce, yggdrasil etc.); or</li>
 *   <li>The player has no translation resource pack (e.g. I18nUpdateMod's
 *       download URL is unreachable, leaving every modded biome name showing
 *       as a raw {@code biome.modid.path} key on Xaero/JourneyMap).</li>
 * </ul>
 *
 * <p>Payload format: a {@code Map<String, String>} of translation key to
 * display name. The server scans every mod jar in {@code mods/} for
 * {@code assets/<modid>/lang/<player_locale>.json} entries that start with
 * {@code "biome."} and additionally falls back to {@code en_us} and
 * title-cased path derivation for biomes that have no translation anywhere.
 *
 * <p>The client overlays this map onto {@link net.minecraft.client.resources.language.ClientLanguage}
 * <strong>only for keys the client doesn't already know</strong>, so a properly
 * downloaded resource pack still wins over the server fallback.
 */
public record BiomeNamesPayload(Map<String, String> entries) implements CustomPacketPayload {

    public static final ResourceLocation ID = TwilightForestMod.prefix("biome_names");
    public static final Type<BiomeNamesPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, BiomeNamesPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(java.util.HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
        BiomeNamesPayload::entries,
        BiomeNamesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
