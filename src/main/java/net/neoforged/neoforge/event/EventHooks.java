package net.neoforged.neoforge.event;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

public final class EventHooks {
    private EventHooks() {
    }

    public static void finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnData) {
        mob.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }
}
