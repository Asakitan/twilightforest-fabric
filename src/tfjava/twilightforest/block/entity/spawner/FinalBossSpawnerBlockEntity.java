package twilightforest.block.entity.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.entity.boss.PlateauBoss;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFEntities;
import twilightforest.init.TFParticleType;

public class FinalBossSpawnerBlockEntity extends BossSpawnerBlockEntity<PlateauBoss> {

	public FinalBossSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.FINAL_BOSS_SPAWNER, TFEntities.PLATEAU_BOSS.get(), pos, state);
	}

	// Inherit default spawnMyBoss() — boss now spawns when a player enters range.

	@Override
	public ParticleOptions getSpawnerParticle() {
		return TFParticleType.ANNIHILATE;
	}
}
