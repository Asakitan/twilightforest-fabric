package twilightforest.init;

/**
 * Server-side stub of upstream {@code TFDataAttachments}.
 *
 * <p>Upstream NeoForge registers ~30 {@code AttachmentType}s on entities. The
 * Fabric port here keeps the field <em>names</em> available as opaque sentinel
 * objects so source files that say {@code TFDataAttachments.MULTIPLAYER_FIGHT}
 * compile, but {@code entity.getData(...)} / {@code .hasData(...)} are not
 * called against these sentinels — runtime callers were rewritten in this
 * port to skip the attachment branch when the per-entity state is missing.
 *
 * <p>A future deep-port can replace these {@link Object} sentinels with real
 * {@code fabric-data-attachment-api-v1} types and re-enable the runtime
 * branches without touching the import set in calling files.
 */
public final class TFDataAttachments {

    public static final Object FEATHER_FAN = new Object();
    public static final Object FLASK_DOSES = new Object();
    public static final Object FORTIFICATION_SHIELDS = new Object();
    public static final Object GIANT_PICKAXE_MINING = new Object();
    public static final Object YETI_THROWING = new Object();
    public static final Object MULTIPLAYER_FIGHT = new Object();
    public static final Object TF_PORTAL_COOLDOWN = new Object();
    public static final Object SMASH_BLOCKS = new Object();
    public static final Object ZOMBIFIED_PLAYER = new Object();
    public static final Object LEASH_PATHFINDER_OVERRIDE = new Object();
    public static final Object BANISHED_TO_TWILIGHT_FOREST = new Object();
    public static final Object TRAVELLERS_WINGS = new Object();
    public static final Object TRAVELLERS_WINGS_ANIM = new Object();
    public static final Object IS_USING_GOGGLES_ZOOM_MODIFIER = new Object();
    public static final Object TRAVELLERS_GOGGLES_RED_THREAD_VISION = new Object();
    public static final Object LAST_TICK_WATER_WALKING = new Object();
    public static final Object HAS_DOUBLE_JUMP = new Object();
    public static final Object DOUBLE_JUMP_VALIDATOR = new Object();
    public static final Object DOUBLE_JUMP_VALIDATOR_LAST_CHECK = new Object();
    public static final Object TEMPORARY_SAVED_STRAIGHT_AHEAD = new Object();
    public static final Object LAST_DAMAGE_ARMOR_TIME = new Object();
    public static final Object LAST_JUMP_KEY_PRESS_TIME = new Object();
    public static final Object LAST_HORIZONTAL_IMPULSE = new Object();
    public static final Object LAST_NON_ZERO_HORIZONTAL_IMPULSE = new Object();
    public static final Object LAST_HORIZONTAL_WALKING_TIME = new Object();
    public static final Object SIDESTEP_VALIDATOR = new Object();
    public static final Object SIDESTEP_VALIDATOR_LAST_CHECK = new Object();
    public static final Object IS_GRADUALLY_GLIDING = new Object();
    public static final Object SLIMY_SOLES_BOUNCE_INFO = new Object();

    private TFDataAttachments() {
    }
}
