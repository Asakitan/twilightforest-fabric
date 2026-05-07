package twilightforest.entity.boss;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.monster.LichMinion;
import twilightforest.entity.projectile.LichBolt;
import twilightforest.entity.projectile.LichBomb;
import twilightforest.init.TFEntities;
import twilightforest.init.TFSounds;

import java.util.List;

public class Lich extends BaseTFBoss implements RangedAttackMob {
    public static final int MAX_SHADOW_CLONES = 2;
    public static final int INITIAL_SHIELD_STRENGTH = 6;
    public static final int INITIAL_MINIONS_TO_SUMMON = 9;
    public static final int MAX_HEALTH = 100;
    public static final int PARTICLE_BURST_COOLDOWN = 10;
    public static final int DEATH_ANIMATION_POINT_A = PARTICLE_BURST_COOLDOWN * 5;
    public static final int DEATH_ANIMATION_POINT_B = DEATH_ANIMATION_POINT_A + 16;
    private int attackCooldown;
    private int minionCooldown;
    private boolean shadowClone;
    private int shieldStrength = INITIAL_SHIELD_STRENGTH;
    private int minionsToSummon = INITIAL_MINIONS_TO_SUMMON;
    private int nextAttackType;
    @Nullable
    private Lich masterLich;

    public Lich(EntityType<? extends Lich> type, Level level) {
        super(type, level);
        this.xpReward = 217;
    }

    public Lich(Level level, Lich master) {
        this(TFEntities.LICH.get(), level);
        this.setShadowClone(true);
        this.masterLich = master;
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected BossEvent.BossBarOverlay getBossBarOverlay() {
        return BossEvent.BossBarOverlay.NOTCHED_6;
    }

    @Override
    protected boolean shouldShowBossBar(ServerPlayer player) {
        return !this.isShadowClone();
    }

    @Override
    protected void tickBossBar() {
        this.getBossBar().setProgress(this.getPhase() == 1 ? this.getShieldStrength() / (float) INITIAL_SHIELD_STRENGTH : this.getHealth() / this.getMaxHealth());
        this.getBossBar().setOverlay(this.getPhase() == 1 ? BossEvent.BossBarOverlay.NOTCHED_6 : BossEvent.BossBarOverlay.PROGRESS);
        this.getBossBar().setColor(this.getPhase() == 1 ? BossEvent.BossBarColor.YELLOW : this.getPhase() == 2 ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.RED);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 40, 24.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("ShadowClone", this.isShadowClone());
        tag.putInt("ShieldStrength", this.getShieldStrength());
        tag.putInt("MinionsToSummon", this.getMinionsToSummon());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setShadowClone(tag.getBoolean("ShadowClone"));
        this.setShieldStrength(tag.contains("ShieldStrength") ? tag.getInt("ShieldStrength") : INITIAL_SHIELD_STRENGTH);
        this.setMinionsToSummon(tag.contains("MinionsToSummon") ? tag.getInt("MinionsToSummon") : INITIAL_MINIONS_TO_SUMMON);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isDeadOrDying()) {
            return;
        }

        if (this.level().isClientSide()) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth();
            double y = this.getY() + this.getBbHeight() * 0.85D;
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth();
            this.level().addParticle(this.getNextAttackType() == 0 ? ParticleTypes.WITCH : ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.attackCooldown > 0) {
            --this.attackCooldown;
        }
        if (this.minionCooldown > 0) {
            --this.minionCooldown;
        }
        if (this.getPhase() == 2 && this.getTarget() != null && this.getMinionsToSummon() > 0 && this.countMyMinions() < 3 && this.minionCooldown <= 0) {
            this.summonMinion();
            this.minionCooldown = 80;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isShadowClone()) {
            this.playSound(TFSounds.LICH_CLONE_HURT, 1.0F, this.getVoicePitch() * 2.0F);
            this.discard();
            return false;
        }

        if (source.getEntity() instanceof Lich) {
            return false;
        }

        if (this.getShieldStrength() > 0) {
            int newShieldStrength = this.getShieldStrength() - 1;
            this.setShieldStrength(newShieldStrength);
            float volume = 1.5F;
            if (newShieldStrength < INITIAL_SHIELD_STRENGTH) {
                volume += 0.25F * (INITIAL_SHIELD_STRENGTH - newShieldStrength);
            }
            if (newShieldStrength == 0) {
                volume += 0.5F;
            }
            this.playSound(newShieldStrength == 0 ? TFSounds.FORTIFICATION_SHIELD_BREAK : TFSounds.FORTIFICATION_SHIELD_BLOCK, volume, this.getVoicePitch() * 1.25F);
            if (source.getEntity() instanceof LivingEntity living) {
                this.setLastHurtByMob(living);
            }
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && this.getTarget() != null && this.random.nextInt(3) == 0) {
            this.teleportNearTarget();
        }
        return hurt;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && !this.isShadowClone()) {
            this.discard();
        } else {
            super.checkDespawn();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFSounds.LICH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFSounds.LICH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFSounds.LICH_DEATH;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.attackCooldown > 0) {
            return;
        }
        ThrowableProjectile projectile = this.getNextAttackType() == 0 ? new LichBolt(this.level(), this) : new LichBomb(this.level(), this);
        this.launchProjectileAt(projectile, target);
        this.setNextAttackType(this.random.nextInt(4) == 0 ? 1 : 0);
        this.attackCooldown = 45;
    }

    public void launchProjectileAt(ThrowableProjectile projectile, LivingEntity target) {
        float angle = (this.yBodyRot * Mth.PI) / 180.0F;
        double sourceX = this.getX() + Mth.cos(angle) * 0.65D;
        double sourceY = this.getY() + this.getBbHeight() * 0.82D;
        double sourceZ = this.getZ() + Mth.sin(angle) * 0.65D;
        double deltaX = target.getX() - sourceX;
        double deltaY = target.getBoundingBox().minY + target.getBbHeight() * 0.5D - (this.getY() + this.getBbHeight() * 0.5D);
        double deltaZ = target.getZ() - sourceZ;

        projectile.moveTo(sourceX, sourceY, sourceZ, this.getYRot(), this.getXRot());
        projectile.shoot(deltaX, deltaY, deltaZ, 0.5F, 1.0F);
        this.playSound(TFSounds.LICH_SHOOT, this.getSoundVolume(), (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F + 1.0F);
        this.level().addFreshEntity(projectile);
    }

    public boolean wantsNewClone(Lich clone) {
        return clone.isShadowClone() && this.countMyClones() < MAX_SHADOW_CLONES;
    }

    public int countMyClones() {
        return this.level().getEntitiesOfClass(Lich.class, this.getNearbyBox(), Lich::isShadowClone).size();
    }

    public boolean wantsNewMinion() {
        return !this.isShadowClone() && this.getPhase() == 2 && this.getMinionsToSummon() > 0 && this.countMyMinions() < 3;
    }

    public int countMyMinions() {
        return this.level().getEntitiesOfClass(LichMinion.class, this.getNearbyBox(), minion -> minion.getMaster() == this).size();
    }

    private void summonMinion() {
        LichMinion minion = new LichMinion(this.level(), this);
        minion.moveTo(this.getX() + (this.random.nextDouble() - 0.5D) * 4.0D, this.getY(), this.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D, this.getYRot(), 0.0F);
        minion.setTarget(this.getTarget());
        this.level().addFreshEntity(minion);
        this.setMinionsToSummon(this.getMinionsToSummon() - 1);
        this.makeMagicTrail(this.getEyePosition(), minion.getEyePosition(), 0.37F, 0.99F, 0.89F);
    }

    public void makeMagicTrail(Vec3 start, Vec3 end, float red, float green, float blue) {
        Vec3 delta = end.subtract(start);
        for (int i = 0; i <= 8; i++) {
            Vec3 pos = start.add(delta.scale(i / 8.0D));
            this.level().addParticle(ParticleTypes.WITCH, pos.x(), pos.y(), pos.z(), red, green, blue);
        }
    }

    private AABB getNearbyBox() {
        return this.getBoundingBox().inflate(32.0D, 16.0D, 32.0D);
    }

    private void teleportNearTarget() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        double x = target.getX() + (this.random.nextDouble() - 0.5D) * 8.0D;
        double y = target.getY();
        double z = target.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D;
        if (this.randomTeleport(x, y, z, true)) {
            this.playSound(TFSounds.LICH_TELEPORT, this.getSoundVolume(), (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F + 1.0F);
        }
    }

    public boolean isShadowClone() {
        return this.shadowClone;
    }

    public void setShadowClone(boolean clone) {
        this.shadowClone = clone;
    }

    public int getShieldStrength() {
        return this.shieldStrength;
    }

    public void setShieldStrength(int strength) {
        this.shieldStrength = Math.max(0, strength);
    }

    public int getMinionsToSummon() {
        return this.minionsToSummon;
    }

    public void setMinionsToSummon(int count) {
        this.minionsToSummon = Math.max(0, count);
    }

    public int getNextAttackType() {
        return this.nextAttackType;
    }

    public void setNextAttackType(int type) {
        this.nextAttackType = type;
    }

    public int getPhase() {
        if (this.getShieldStrength() > 0) {
            return 1;
        }
        return this.getMinionsToSummon() > 0 || this.countMyMinions() > 0 ? 2 : 3;
    }

}
