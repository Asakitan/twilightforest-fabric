package twilightforest.config;

import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side stub of upstream {@code TFConfig}.
 *
 * <p>Upstream NeoForge config holds many gameplay toggles that runtime code
 * checks before doing extra work. The Fabric port here keeps the same field
 * names + types so source files copied from upstream compile, with sensible
 * default values so all features are «on» (matches upstream defaults).
 *
 * <p>The full TOML/JSON config-loading layer is not ported; values are static
 * defaults until a deeper port adds Fabric-side config IO.
 */
public final class TFConfig {
    public static final List<GameProfile> GAME_PROFILES = new ArrayList<>();

    /**
     * 1:1 of upstream {@code tfGenericGameRules.enforcedProgression}. Trophy pedestal,
     * portal openings, and several other landmark interactions check this — enabled by
     * default on upstream so we keep parity here.
     */
    public static boolean enforcedProgression = true;

    public static boolean disableEntireTable = false;
    public static boolean disableUncraftingOnly = false;
    public static boolean flipUncraftingTableHotkey = false;
    public static boolean allowShapelessUncrafting = false;
    public static boolean disableIngredientSwitching = false;
    public static boolean reverseRecipeBlacklist = false;
    public static boolean flipUncraftingModIdList = false;
    public static double uncraftingXpCostMultiplier = 1.0D;
    public static double repairingXpCostMultiplier = 1.0D;
    public static final List<String> disableUncraftingRecipes = new ArrayList<>();
    public static final List<String> blacklistedUncraftingModIds = new ArrayList<>();

    public static boolean silentCicadas = false;
    public static boolean silentCicadasOnHead = false;

    public static boolean disableSkullCandles = false;
    public static int commonCloudBlockPrecipitationDistance = 32;
    public static boolean disableTimeCore = false;
    public static int timeCoreRange = 16;
    public static boolean disableTransformationCore = false;
    public static int transformationCoreRange = 16;
    public static boolean disableMiningCore = false;
    public static int miningCoreRange = 16;
    public static boolean disableSortingCore = false;
    public static int sortingCoreRange = 16;

    /** Default matches upstream: no multiplayer scaling unless config enables it. */
    public static MultiplayerFightAdjuster multiplayerFightAdjuster = MultiplayerFightAdjuster.NONE;

    /**
     * Mirror of upstream {@code TFConfig.Common} so that log-core and other blocks that call
     * {@code TFConfig.COMMON_CONFIG.MAGIC_TREES.disableX} compile unchanged.
     */
    public static final CommonConfig COMMON_CONFIG = new CommonConfig();

    public static final class CommonConfig {
        public final MagicTrees MAGIC_TREES = new MagicTrees();

        public static final class MagicTrees {
            // Feature toggles — false = feature is ON (upstream default)
            public boolean disableTime = false;
            public boolean disableMining = false;
            public boolean disableSorting = false;
            public boolean disableTransformation = false;

            // Effect ranges (in blocks)
            public int timeRange = 16;
            public int miningRange = 16;
            public int sortingRange = 16;
            public int transformationRange = 16;
        }
    }

    /**
     * Codex stub: upstream NeoForge {@code TFConfig.bossDropChests} controls whether
     * defeated TF bosses drop their loot via a celebratory chest at the boss-room
     * spawn pad (true) or as raw drops (false). Default true matches upstream.
     */
    public static boolean bossDropChests = true;

    private TFConfig() {
    }

    public enum MultiplayerFightAdjuster {
        NONE(false, false),
        MORE_LOOT(true, false),
        MORE_HEALTH(false, true),
        MORE_LOOT_AND_HEALTH(true, true);

        private final boolean moreLoot;
        private final boolean moreHealth;

        MultiplayerFightAdjuster(boolean loot, boolean health) {
            this.moreLoot = loot;
            this.moreHealth = health;
        }

        public boolean adjustsLootRolls() {
            return this.moreLoot;
        }

        public boolean adjustsHealth() {
            return this.moreHealth;
        }
    }
}
