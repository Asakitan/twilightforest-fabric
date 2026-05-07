package twilightforest.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Q27 simplified replacement for TF original {@code TFDataMaps.TRANSFORMATION_POWDER}.
 *
 * <p>Original TF used NeoForge's {@code DataMapType<EntityType<?>, EntityTransformation>}
 * which is not available on Fabric. This class is a Java-only static map seeded with
 * a vanilla → TF passive entity mapping. {@link twilightforest.item.TransformPowderItem}
 * looks up entries via {@link #lookup(EntityType)}.</p>
 *
 * <p>The mapping is bidirectional: TF entities can also be reverted to their vanilla
 * counterparts. Add new entries here as new entity ports land.</p>
 */
public final class TFTransformations {

    private static final Map<EntityType<?>, EntityType<?>> MAP = new IdentityHashMap<>();

    private TFTransformations() {}

    static {
        // Vanilla → TF passives (defined in Lane B)
        bidi(EntityType.PIG, TFEntities.BOAR.get());
        bidi(EntityType.COW, TFEntities.DEER.get());
        bidi(EntityType.SHEEP, TFEntities.BIGHORN_SHEEP.get());
        bidi(EntityType.RABBIT, TFEntities.DWARF_RABBIT.get());
        bidi(EntityType.CHICKEN, TFEntities.RAVEN.get());
        // SnowGolem → Penguin is not bidi because penguin has different stats than snow golem.
        MAP.put(EntityType.SNOW_GOLEM, TFEntities.PENGUIN.get());
    }

    private static void bidi(EntityType<?> a, EntityType<?> b) {
        MAP.put(a, b);
        MAP.put(b, a);
    }

    /** Returns the transformation result, or null if no mapping exists. */
    public static EntityType<?> lookup(EntityType<?> from) {
        return MAP.get(from);
    }

    /** Returns the transformation result for a living entity's type, or null. */
    public static EntityType<?> lookup(LivingEntity entity) {
        return MAP.get(entity.getType());
    }
}
