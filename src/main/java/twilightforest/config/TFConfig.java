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

    /** Default «adjusts loot rolls when multiple players fight a boss». */
    public static final MultiplayerFightAdjuster multiplayerFightAdjuster = new MultiplayerFightAdjuster();

    private TFConfig() {
    }

    /** Stub mirror of upstream multiplayer-fight adjuster — keeps default behaviour ON. */
    public static final class MultiplayerFightAdjuster {
        public boolean adjustsLootRolls() {
            return false;
        }

        public boolean adjustsLootMultipliers() {
            return false;
        }
    }
}
