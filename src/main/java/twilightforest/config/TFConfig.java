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

    public static boolean disableEntireTable = false;
    public static boolean disableUncraftingOnly = false;
    public static boolean flipUncraftingTableHotkey = false;

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
