package twilightforest.client.translations;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side storage for the {@code biome.*} translation overlay pushed by
 * the server through {@link twilightforest.network.BiomeNamesPayload}.
 *
 * <p>Holds the most-recent map snapshot. Lookups are read-only and only used
 * by {@code ClientLanguageMixin} to fill in missing biome translations after
 * the vanilla {@code ClientLanguage.storage} has been consulted.
 *
 * <p>Volatile reference + immutable {@link java.util.Map#copyOf} snapshot so
 * the language thread can read while a fresh payload is being applied without
 * a lock or {@link java.util.concurrent.ConcurrentHashMap} per-lookup cost.
 */
public final class BiomeNamesClientStore {

    private static volatile Map<String, String> ENTRIES = Map.of();

    private BiomeNamesClientStore() {
    }

    public static void apply(Map<String, String> entries) {
        if (entries == null || entries.isEmpty()) {
            ENTRIES = Map.of();
            return;
        }
        // Defensive copy + immutable view; the payload's record-backed map is
        // shared with whatever the netty handler queues next.
        ENTRIES = Map.copyOf(new HashMap<>(entries));
    }

    public static String get(String key) {
        return ENTRIES.get(key);
    }

    public static int size() {
        return ENTRIES.size();
    }

    public static void clear() {
        ENTRIES = Map.of();
    }
}
