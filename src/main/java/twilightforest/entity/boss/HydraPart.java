package twilightforest.entity.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import twilightforest.entity.TFPart;

public abstract class HydraPart extends TFPart<Hydra> {
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = SynchedEntityData.defineId(HydraPart.class, EntityDataSerializers.BOOLEAN);

    boolean markedDead;
    private final EntityDimensions activeSize;

    protected HydraPart(Hydra parent, float width, float height) {
        super(parent);
        this.activeSize = EntityDimensions.scalable(width, height);
        this.setSize(this.activeSize);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ACTIVE, true);
    }

    @Override
    public void tick() {
        this.clearFire();
        super.tick();
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.markedDead) {
            ++this.deathTime;
        }
        if (this.markedDead && this.isActive() && this.level().isClientSide()) {
            float width = this.getBbWidth();
            float height = this.getBbHeight();
            for (int k = 0; k < 10; k++) {
                this.level().addParticle(this.random.nextInt(5) == 0 ? ParticleTypes.EXPLOSION : ParticleTypes.POOF,
                        this.getX() + this.random.nextFloat() * width,
                        this.getY() + this.random.nextFloat() * height,
                        this.getZ() + this.random.nextFloat() * width,
                        this.random.nextGaussian() * 0.02D,
                        this.random.nextGaussian() * 0.02D,
                        this.random.nextGaussian() * 0.02D);
            }
        }
        if (this.deathTime == 20) {
            this.deactivate();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return this.getParent() != null && this.getParent().attackEntityFromPart(this, source, amount);
    }

    @Override
    public boolean isInvisible() {
        return !this.isActive() || super.isInvisible();
    }

    public boolean isActive() {
        return this.getEntityData().get(DATA_ACTIVE);
    }

    public void activate() {
        this.markedDead = false;
        this.deathTime = 0;
        this.setSize(this.activeSize);
        this.getEntityData().set(DATA_ACTIVE, true);
    }

    public void deactivate() {
        this.setSize(EntityDimensions.scalable(0.0F, 0.0F));
        this.getEntityData().set(DATA_ACTIVE, false);
    }
}
