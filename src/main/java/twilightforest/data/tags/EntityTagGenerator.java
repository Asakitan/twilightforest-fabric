package twilightforest.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import twilightforest.TwilightForestMod;

public final class EntityTagGenerator {
	public static final TagKey<EntityType<?>> DONT_KILL_BUGS = create("dont_kill_bugs");

	private EntityTagGenerator() {
	}

	private static TagKey<EntityType<?>> create(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, TwilightForestMod.prefix(path));
	}
}
