package twilightforest.config;

import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.List;

public final class TFConfig {
    public static final List<GameProfile> GAME_PROFILES = new ArrayList<>();
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
    public static boolean disableLockedBiomeToasts = false;
    public static boolean showQuestRamCrosshairIndicator = true;
    public static boolean showFortificationShieldIndicator = true;
    public static boolean showFortificationShieldIndicatorInCreative = false;
    public static boolean prettifyOreMeterGui = true;
    public static int itemDisplayScreenOffsetX = 4;
    public static int itemDisplayScreenOffsetY = 4;
    public static double itemDisplayScreenScale = 1.0D;
    public static boolean twentyFourHourFormat = false;
    public static int clientCloudBlockPrecipitationDistance = -1;
    public static int commonCloudBlockPrecipitationDistance = 32;
    public static boolean manualTravellersWingsGradualGlideDefault = true;

    private TFConfig() {
    }

    public static int getClientCloudBlockPrecipitationDistance() {
        return clientCloudBlockPrecipitationDistance > 0 ? clientCloudBlockPrecipitationDistance : commonCloudBlockPrecipitationDistance;
    }
}
