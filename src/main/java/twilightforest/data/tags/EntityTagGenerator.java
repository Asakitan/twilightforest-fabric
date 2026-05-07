package twilightforest.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import twilightforest.TwilightForestMod;

public final class EntityTagGenerator {
	public static final TagKey<EntityType<?>> DONT_KILL_BUGS = create("dont_kill_bugs");
	public static final TagKey<EntityType<?>> LICH_POPPABLES = create("lich_poppables");
	public static final TagKey<EntityType<?>> SORTABLE_ENTITIES = create("sortable_entities");
	/** P5.e — projectile entity types the Lich deflects (phase 2 onwards). */
	public static final TagKey<EntityType<?>> LICH_DEFLECTS_PHASE_2 = create("lich_deflects_phase_2");

	private EntityTagGenerator() {
	}

	private static TagKey<EntityType<?>> create(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, TwilightForestMod.prefix(path));
	}
}
