package twilightforest.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.init.TFItemVisuals;
import twilightforest.init.TFSounds;

import java.util.List;

/**
 * Q25 simplified port of MoonwormShot — projectile that places a directional
 * {@code twilightforest:moonworm} on the block face it hits, or splats on
 * entities. Stripped TF deps: TFThrowable parent → ThrowableProjectile;
 * TFDamageTypes → vanilla magic; TFLootTables → no loot drop on failure.
 */
public class MoonwormShot extends ThrowableProjectile {

    public MoonwormShot(EntityType<? extends MoonwormShot> type, Level level) {
        super(type, level);
    }

    public MoonwormShot(Level level, LivingEntity thrower) {
        super(TFEntities.MOONWORM_SHOT.get(), thrower, level);
        this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0F, 1.5F, 1.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03F;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) return;
        BlockPos pos = result.getBlockPos().relative(result.getDirection());
        BlockState here = this.level().getBlockState(pos);
        if (here.canBeReplaced() && !here.is(BlockTags.FIRE) && !here.is(Blocks.LAVA)) {
            this.level().setBlockAndUpdate(pos,
                    TFBlocks.MOONWORM.get().defaultBlockState()
                            .setValue(DirectionalBlock.FACING, result.getDirection()));
            this.level().playSound(null, result.getBlockPos(),
                    TFSounds.MOONWORM_PLACED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        if (result.getEntity() instanceof LivingEntity living) {
            living.hurt(this.damageSources().indirectMagic(this, this.getOwner()),
                    this.random.nextInt(3) == 0 ? 1.0F : 0.0F);
        }
        this.discard();
    }
}
