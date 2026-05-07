package twilightforest.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import twilightforest.init.TFEntities;
import twilightforest.init.TFItemVisuals;
import twilightforest.init.TFSounds;

import java.util.List;

/**
 * Q24 simplified port of TwilightWandBolt — homing-less magic bolt that
 * damages mobs on hit and emits enchant-glyph particles in flight.
 *
 * <p>Original TF version extends a custom {@code TFThrowable} base and uses
 * {@code TFParticleType.MAGIC_EFFECT}/{@code TWILIGHT_ORB} + {@code
 * TFDamageTypes.TWILIGHT_SCEPTER}. Q24 substitutes vanilla
 * {@link ParticleTypes#ENCHANT} and the indirect-magic damage source for those
 * to keep the dependency surface tight; the result is gameplay-equivalent.</p>
 */
public class TwilightWandBolt extends ThrowableProjectile {

    public TwilightWandBolt(EntityType<? extends TwilightWandBolt> type, Level level) {
        super(type, level);
    }

    public TwilightWandBolt(Level level, LivingEntity thrower) {
        super(TFEntities.WAND_BOLT.get(), thrower, level);
        this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0F, 1.5F, 1.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.003D;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            // Server-driven trail of vanilla enchant particles for non-paired clients.
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT,
                        this.getX(), this.getY(), this.getZ(),
                        4, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(target instanceof LichBolt) && !(target instanceof LichBomb)
                && !(target instanceof TwilightWandBolt);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity) {
            hit.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 6.0F);
        }
        this.level().playSound(null, hit.blockPosition(),
                TFSounds.TWILIGHT_SCEPTER_USE,
                this.getOwner() != null ? this.getOwner().getSoundSource() : SoundSource.PLAYERS,
                1.0F, 1.0F);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.level().playSound(null, result.getBlockPos(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5F, 1.4F);
            this.discard();
        }
    }
}
