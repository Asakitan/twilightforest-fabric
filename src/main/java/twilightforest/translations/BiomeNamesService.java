package twilightforest.translations;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Builds and caches per-locale biome-translation maps by scanning every mod
 * jar in {@code <server-root>/mods/} for {@code assets/<modid>/lang/*.json}
 * files. Used by {@link twilightforest.network.BiomeNamesPayload} dispatch on
 * player join.
 *
 * <p>Resolution order for each biome id in the live BIOME registry:
 * <ol>
 *   <li>Translation in the requested locale (e.g. {@code zh_cn});</li>
 *   <li>Translation in {@code en_us} (universal fallback);</li>
 *   <li>Title-cased path derivation (e.g.
 *       {@code yggdrasil:ginnungagap} -> {@code "Ginnungagap"}).</li>
 * </ol>
 *
 * <p>Cache lifetime: the per-locale aggregate is built once per locale, lazily,
 * and reused across player joins. The underlying jar lang files are read via
 * {@link ZipFile}, so changes to mods between server restarts are not picked
 * up until the next start (acceptable since mod jars don't change at runtime).
 */
public final class BiomeNamesService {

    private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/biome-names");

    private static final Pattern LANG_FILE = Pattern.compile("assets/([^/]+)/lang/([a-z]{2,3}(?:_[a-z]{2,4})?)\\.json$");
    private static final String BIOME_PREFIX = "biome.";

    /** modid -> locale -> (key -> translation). Built lazily on first scan. */
    private static volatile Map<String, Map<String, Map<String, String>>> JAR_LANG_CACHE;

    /** locale -> (key -> translation), aggregated. Built lazily per locale. */
    private static final Map<String, Map<String, String>> LOCALE_CACHE = new ConcurrentHashMap<>();

    private BiomeNamesService() {
    }

    /**
     * Build (or return cached) biome-translation map for the given player.
     * Walks the live BIOME registry so only biomes actually registered on the
     * server are included.
     */
    public static Map<String, String> resolveForPlayer(MinecraftServer server, ServerPlayer player) {
        // 1.21.1 ServerPlayer keeps the locale on the ClientInformation record
        // (set during play handshake via updateOptions). The previous private
        // String field is still there but only updated through that record, so
        // clientInformation().language() is the canonical accessor.
        String locale = normalizeLocale(player.clientInformation().language());
        Map<String, String> cached = LOCALE_CACHE.get(locale);
        if (cached != null) return cached;

        ensureScanned(server);
        Map<String, String> built = buildForLocale(server, locale);
        LOCALE_CACHE.put(locale, built);
        return built;
    }

    private static void ensureScanned(MinecraftServer server) {
        if (JAR_LANG_CACHE != null) return;
        synchronized (BiomeNamesService.class) {
            if (JAR_LANG_CACHE != null) return;
            JAR_LANG_CACHE = scanModsDirectory(server);
        }
    }

    private static Map<String, Map<String, Map<String, String>>> scanModsDirectory(MinecraftServer server) {
        Path modsDir = server.getServerDirectory().resolve("mods");
        Map<String, Map<String, Map<String, String>>> result = new HashMap<>();
        if (!Files.isDirectory(modsDir)) {
            LOGGER.warn("mods/ directory not found at {} - biome name push disabled", modsDir);
            return result;
        }

        Gson gson = new Gson();
        int jars = 0, biomeKeys = 0;
        try (var stream = Files.list(modsDir)) {
            for (Path jarPath : (Iterable<Path>) stream::iterator) {
                String name = jarPath.getFileName().toString();
                if (!name.endsWith(".jar")) continue;
                jars++;
                try (ZipFile zip = new ZipFile(jarPath.toFile())) {
                    var entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry e = entries.nextElement();
                        Matcher m = LANG_FILE.matcher(e.getName());
                        if (!m.matches()) continue;
                        String modid = m.group(1);
                        String locale = m.group(2).toLowerCase(Locale.ROOT);
                        try (InputStream is = zip.getInputStream(e);
                             InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                            JsonElement parsed = gson.fromJson(r, JsonElement.class);
                            if (parsed == null || !parsed.isJsonObject()) continue;
                            JsonObject obj = parsed.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> kv : obj.entrySet()) {
                                String key = kv.getKey();
                                if (!key.startsWith(BIOME_PREFIX)) continue;
                                if (!kv.getValue().isJsonPrimitive()) continue;
                                String value = kv.getValue().getAsString();
                                result.computeIfAbsent(modid, k -> new HashMap<>())
                                    .computeIfAbsent(locale, k -> new HashMap<>())
                                    .put(key, value);
                                biomeKeys++;
                            }
                        } catch (Exception ex) {
                            // Skip malformed lang files silently — common with patched/legacy jars.
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.debug("Could not read mod jar {}: {}", name, ex.getMessage());
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("mods/ directory scan failed: {}", ex.getMessage());
        }
        LOGGER.info("Scanned {} mod jar(s), harvested {} biome.* lang entries across {} mods",
            jars, biomeKeys, result.size());
        return result;
    }

    private static Map<String, String> buildForLocale(MinecraftServer server, String locale) {
        Registry<Biome> registry = server.registryAccess().registryOrThrow(Registries.BIOME);
        Map<String, String> out = new LinkedHashMap<>();
        for (ResourceLocation id : registry.keySet()) {
            String modid = id.getNamespace();
            // Vanilla biomes are always client-known — skip to keep payload small.
            if ("minecraft".equals(modid)) continue;
            String key = "biome." + modid + "." + id.getPath();
            String translation = lookup(modid, locale, key);
            if (translation == null && !"en_us".equals(locale)) {
                translation = lookup(modid, "en_us", key);
            }
            if (translation == null) {
                translation = derivePath(id.getPath());
            }
            out.put(key, translation);
        }
        return out;
    }

    private static String lookup(String modid, String locale, String key) {
        Map<String, Map<String, String>> perLocale = JAR_LANG_CACHE.get(modid);
        if (perLocale == null) return null;
        Map<String, String> entries = perLocale.get(locale);
        if (entries == null) return null;
        return entries.get(key);
    }

    /**
     * Convert {@code "alpha_islands"} -> {@code "Alpha Islands"}; reasonable
     * for both Latin and CJK locales (better than raw key on minimaps).
     */
    static String derivePath(String path) {
        String[] parts = path.split("[_/]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) sb.append(parts[i].substring(1));
        }
        return sb.length() == 0 ? path : sb.toString();
    }

    /**
     * Normalize Minecraft locale string. Vanilla send {@code "zh_cn"} but mods
     * sometimes ship as {@code "zh_CN"}; lowercase to match jar naming.
     */
    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isEmpty()) return "en_us";
        return locale.toLowerCase(Locale.ROOT);
    }

    /** Test/diagnostic invalidation — public so /codex commands can rebuild. */
    public static void invalidate() {
        synchronized (BiomeNamesService.class) {
            JAR_LANG_CACHE = null;
            LOCALE_CACHE.clear();
        }
    }
}
