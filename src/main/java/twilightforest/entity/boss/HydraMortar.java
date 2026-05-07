package twilightforest.entity.boss;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFEntities;
import twilightforest.init.TFItemVisuals;

import java.util.List;

public class HydraMortar extends ThrowableProjectile implements ItemSupplier {
    private static final int BURN_FACTOR = 5;
    private static final int DIRECT_DAMAGE = 18;

    public int fuse = 80;
    private boolean megaBlast;

    public HydraMortar(EntityType<? extends HydraMortar> type, Level level) {
        super(type, level);
    }

    public HydraMortar(Level level, LivingEntity owner) {
        super(TFEntities.HYDRA_MORTAR.get(), owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        this.makeTrail();
        if (this.onGround() && !this.level().isClientSide() && this.fuse-- <= 0) {
            this.detonate();
        }
    }

    private void makeTrail() {
        this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    public void setToBlasting() {
        this.megaBlast = true;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.megaBlast || result.getDirection() != Direction.UP) {
            this.detonate();
        } else {
            this.setDeltaMovement(this.getDeltaMovement().x(), 0.0D, this.getDeltaMovement().z());
            this.setOnGround(true);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) result);
        } else if (result.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) result);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (!this.level().isClientSide() && this.getOwner() != null && !entity.is(this.getOwner()) && !this.isSameOwnerMortar(entity)) {
            this.detonate();
        }
    }

    private boolean isSameOwnerMortar(Entity entity) {
        return entity instanceof HydraMortar mortar && mortar.getOwner() != null && mortar.getOwner().is(this.getOwner());
    }

    private void detonate() {
        if (this.level().isClientSide()) {
            return;
        }
        float power = this.megaBlast ? 4.0F : 0.1F;
        boolean griefing = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, griefing, Level.ExplosionInteraction.MOB);
        for (Entity nearby : this.level().getEntities(this, this.getBoundingBox().inflate(1.0D))) {
            if (!nearby.is(this.getOwner()) && !this.isSameOwnerMortar(nearby) && nearby.hurt(TFDamageTypes.indirectSource(this.level(), TFDamageTypes.HYDRA_MORTAR, this, this.getOwner()), DIRECT_DAMAGE)) {
                nearby.igniteForSeconds(BURN_FACTOR);
            }
        }
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        super.hurt(source, amount);
        if (!this.level().isClientSide() && source.getEntity() != null && !source.is(DamageTypeTags.IS_EXPLOSION)) {
            Vec3 look = source.getEntity().getLookAngle();
            this.shoot(look.x(), look.y(), look.z(), 1.5F, 0.1F);
            this.setOnGround(false);
            this.fuse += 20;
            if (source.getEntity() instanceof LivingEntity) {
                this.setOwner(source.getEntity());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.5F;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }
}