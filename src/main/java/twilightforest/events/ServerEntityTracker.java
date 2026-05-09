package twilightforest.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

final class ServerEntityTracker {
	private static final Set<Entity> ENTITIES = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private static boolean bootstrapped;

	private ServerEntityTracker() {
	}

	static void bootstrap() {
		if (bootstrapped) return;
		bootstrapped = true;

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> ENTITIES.add(entity));
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> ENTITIES.remove(entity));
	}

	static List<LivingEntity> living(ServerLevel level) {
		List<LivingEntity> living = new ArrayList<>();
		for (Entity entity : snapshot()) {
			if (entity instanceof LivingEntity livingEntity && isActiveIn(level, entity)) {
				living.add(livingEntity);
			}
		}
		return living;
	}

	static List<Entity> matching(ServerLevel level, Predicate<Entity> predicate) {
		List<Entity> matches = new ArrayList<>();
		for (Entity entity : snapshot()) {
			if (isActiveIn(level, entity) && predicate.test(entity)) {
				matches.add(entity);
			}
		}
		return matches;
	}

	private static List<Entity> snapshot() {
		synchronized (ENTITIES) {
			return new ArrayList<>(ENTITIES);
		}
	}

	private static boolean isActiveIn(ServerLevel level, Entity entity) {
		return entity.level() == level && !entity.isRemoved();
	}
}
