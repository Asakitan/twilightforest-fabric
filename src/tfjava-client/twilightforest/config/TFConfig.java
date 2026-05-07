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

    private TFConfig() {
    }
}
