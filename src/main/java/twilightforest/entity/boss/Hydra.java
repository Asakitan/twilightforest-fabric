package twilightforest.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.TFPart;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFSounds;
import twilightforest.util.entities.EntityUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Hydra extends BaseTFBoss implements TFPart.Owner {
    private static final int TICKS_BEFORE_HEALING = 1000;
    private static final int MAX_HEALTH = 360;
    private static final float ARMOR_MULTIPLIER = 8.0F;
    private static final int BITE_DAMAGE = 48;
    private static final int FLAME_DAMAGE = 19;
    private static final int FLAME_BURN_SECONDS = 3;
    private static final int BITE_CHANCE = 10;
    private static final int FLAME_CHANCE = 100;
    private static final int MORTAR_CHANCE = 160;
    private static final int SECONDARY_FLAME_CHANCE = 10;
    private static final int SECONDARY_MORTAR_CHANCE = 16;
    private static final int HEAD_MAX_DAMAGE = 120;
    private static final int HEAD_RESPAWN_TICKS = 140;

    private int ticksSinceDamaged;
    private int mortarCooldown;
    private int flameCooldown;
    private float randomYawVelocity;
    private static final int HYDRA_HEADS = 7;
    private static final int HYDRA_NECK_SEGMENTS = 5;
    private final HydraHeadState[] headStates = new HydraHeadState[HYDRA_HEADS];
    private final int[] headStateTicks = new int[HYDRA_HEADS];
    private final int[] headAttackCooldowns = new int[HYDRA_HEADS];
    private final int[] headDamageTaken = new int[HYDRA_HEADS];
    private final int[] headRespawnTicks = new int[HYDRA_HEADS];
    private final String[] headNames = new String[HYDRA_HEADS];
    private final LivingEntity[] headTargets = new LivingEntity[HYDRA_HEADS];
    public boolean renderFakeHeads;
    private final double[] headTargetX = new double[HYDRA_HEADS];
    private final double[] headTargetY = new double[HYDRA_HEADS];
    private final double[] headTargetZ = new double[HYDRA_HEADS];
    private final HydraHead[] headParts = new HydraHead[HYDRA_HEADS];
    private final HydraNeck[][] neckParts = new HydraNeck[HYDRA_HEADS][HYDRA_NECK_SEGMENTS];
    private final TFPart<?>[] partArray;

    private enum HydraHeadState {
        IDLE(10),
        BITE_BEGINNING(40),
        BITE_READY(80),
        BITING(7),
        BITE_ENDING(40),
        FLAME_BEGINNING(40),
        FLAMING(100),
        FLAME_ENDING(30),
        MORTAR_BEGINNING(40),
        MORTAR_SHOOTING(25),
        MORTAR_ENDING(30),
        ATTACK_COOLDOWN(80),
        DYING(70),
        DEAD(20),
        BORN(20),
        ROAR_START(10),
        ROAR_RAWR(50);

        private final int duration;

        HydraHeadState(int duration) {
            this.duration = duration;
        }
    }

    private record HydraHeadPose(float xRotation, float yRotation, float neckLength, float mouthOpen) {
    }

    public Hydra(EntityType<? extends Hydra> type, Level level) {
        super(type, level);
        this.xpReward = 511;
        this.noCulling = true;
        for (int head = 0; head < HYDRA_HEADS; head++) {
            this.headStates[head] = HydraHeadState.IDLE;
            this.headAttackCooldowns[head] = head * 25;
        }
        List<TFPart<?>> parts = new ArrayList<>();
        for (int head = 0; head < HYDRA_HEADS; head++) {
            this.headParts[head] = new HydraHead(this, head);
            parts.add(this.headParts[head]);
            for (int segment = 0; segment < HYDRA_NECK_SEGMENTS; segment++) {
                this.neckParts[head][segment] = new HydraNeck(this.headParts[head], head, segment);
                parts.add(this.neckParts[head][segment]);
            }
        }
        this.partArray = parts.toArray(new TFPart<?>[0]);
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.BLUE;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.85D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        this.clearFire();
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientParticles();
        } else {
            this.ticksSinceDamaged++;
            if (this.ticksSinceDamaged > TICKS_BEFORE_HEALING && this.ticksSinceDamaged % 5 == 0) {
                this.heal(1.0F);
            }
            if (this.hurtTime == 0) {
                this.collideWithMultipartBody();
            }
            this.destroyBlocksInAABB(this.getBoundingBox().inflate(1.0D, 0.5D, 1.0D));
            this.destroyMultipartBlocks();
            if (this.tickCount % 20 == 0 && this.isUnsteadySurfaceBeneath()) {
                this.destroyBlocksInAABB(this.getBoundingBox().move(0.0D, -1.0D, 0.0D));
            }
        }
        this.updateMultipartDisplays();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.mortarCooldown > 0) {
            --this.mortarCooldown;
        }
        if (this.flameCooldown > 0) {
            --this.flameCooldown;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || target.distanceToSqr(this) > 48.0D * 48.0D) {
            Player nearest = this.level().getNearestPlayer(this, 48.0D);
            if (nearest != null && !nearest.isCreative()) {
                this.setTarget(nearest);
            } else if (this.random.nextFloat() < 0.05F) {
                this.randomYawVelocity = (this.random.nextFloat() - 0.5F) * 20.0F;
                this.setYRot(this.getYRot() + this.randomYawVelocity);
            }
            return;
        }
        this.lookAt(target, 10.0F, this.getMaxHeadXRot());
        this.tickHeadAttackStates(target);
        this.secondaryAttacks(target);
        this.flameCooldown = this.anyHeadInState(HydraHeadState.FLAMING, HydraHeadState.FLAME_BEGINNING) ? 5 : 0;
        this.mortarCooldown = this.anyHeadInState(HydraHeadState.MORTAR_SHOOTING, HydraHeadState.MORTAR_BEGINNING) ? 5 : 0;
    }

    private void tickHeadAttackStates(LivingEntity target) {
        boolean canSee = this.getSensing().hasLineOfSight(target);
        float distance = target.distanceTo(this);
        for (int head = 0; head < HYDRA_HEADS; head++) {
            this.refreshHeadTarget(head, target);
            this.tickHeadRespawn(head);
            if (this.headAttackCooldowns[head] > 0) {
                --this.headAttackCooldowns[head];
            }
            HydraHeadState state = this.headStates[head];
            if (state == HydraHeadState.DEAD) {
                continue;
            }
            if (state == HydraHeadState.IDLE && this.headAttackCooldowns[head] <= 0 && canSee && !this.areTooManyHeadsAttacking(head)) {
                this.chooseHeadAttack(head, distance, target);
                state = this.headStates[head];
            }
            this.executeHeadAttack(head, state, target);
            if (++this.headStateTicks[head] >= state.duration) {
                this.advanceHeadState(head, state);
            }
        }
    }

    private void chooseHeadAttack(int head, float distance, LivingEntity target) {
        boolean targetAbove = this.isTargetAboveHydra(target);
        if (head < 3 && distance > 4.0F && distance < 10.0F && this.countActiveHeads() > 2 && !this.areOtherHeadsBiting(head) && this.random.nextInt(BITE_CHANCE) == 0) {
            this.setHeadTarget(head, target);
            this.setHeadState(head, HydraHeadState.BITE_BEGINNING);
        } else if (distance > 0.0F && distance < 20.0F && this.random.nextInt(FLAME_CHANCE) == 0) {
            this.setHeadTarget(head, target);
            this.setHeadState(head, HydraHeadState.FLAME_BEGINNING);
        } else if (distance > 8.0F && distance < 32.0F && !targetAbove && this.random.nextInt(MORTAR_CHANCE) == 0) {
            this.setHeadTarget(head, target);
            this.setHeadState(head, HydraHeadState.MORTAR_BEGINNING);
        }
    }

    private boolean isTargetAboveHydra(LivingEntity target) {
        return target.getBoundingBox().minY > this.getBoundingBox().maxY;
    }

    private void secondaryAttacks(LivingEntity primaryTarget) {
        LivingEntity secondaryTarget = this.findSecondaryTarget(primaryTarget, 20.0D);
        if (secondaryTarget == null) {
            return;
        }
        float distance = secondaryTarget.distanceTo(this);
        for (int head = 1; head < HYDRA_HEADS; head++) {
            HydraHeadState state = this.headStates[head];
            if (state != HydraHeadState.IDLE || this.headAttackCooldowns[head] > 0 || this.isHeadDead(head) || !this.isTargetOnThisSide(head, secondaryTarget)) {
                continue;
            }
            if (distance > 0.0F && distance < 20.0F && this.random.nextInt(SECONDARY_FLAME_CHANCE) == 0) {
                this.setHeadTarget(head, secondaryTarget);
                this.setHeadState(head, HydraHeadState.FLAME_BEGINNING);
            } else if (distance > 8.0F && distance < 32.0F && this.random.nextInt(SECONDARY_MORTAR_CHANCE) == 0) {
                this.setHeadTarget(head, secondaryTarget);
                this.setHeadState(head, HydraHeadState.MORTAR_BEGINNING);
            }
        }
    }

    @Nullable
    private LivingEntity findSecondaryTarget(LivingEntity primaryTarget, double range) {
        AABB searchBox = new AABB(this.getX(), this.getY(), this.getZ(), this.getX() + 1.0D, this.getY() + 1.0D, this.getZ() + 1.0D).inflate(range, range, range);
        return this.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> this.isValidSecondaryTarget(entity, primaryTarget))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private boolean isValidSecondaryTarget(LivingEntity entity, LivingEntity primaryTarget) {
        if (entity == this || entity == primaryTarget || entity instanceof Hydra || this.isAnyHeadTargeting(entity) || !entity.isAlive() || !this.getSensing().hasLineOfSight(entity)) {
            return false;
        }
        return !(entity instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }

    private boolean isAnyHeadTargeting(Entity target) {
        for (LivingEntity headTarget : this.headTargets) {
            if (headTarget == target) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetOnThisSide(int head, Entity target) {
        Vec3 headPosition = this.getHeadPosition(head, this.getCurrentHeadPose(head));
        return this.distanceSqXZ(headPosition, target.position()) < this.distanceSqXZ(this.position(), target.position());
    }

    private double distanceSqXZ(Vec3 first, Vec3 second) {
        double dx = first.x() - second.x();
        double dz = first.z() - second.z();
        return dx * dx + dz * dz;
    }

    private void advanceHeadState(int head, HydraHeadState state) {
        switch (state) {
            case BITE_BEGINNING -> this.setHeadState(head, HydraHeadState.BITE_READY);
            case BITE_READY -> this.setHeadState(head, HydraHeadState.BITING);
            case BITING -> this.setHeadState(head, HydraHeadState.BITE_ENDING);
            case BITE_ENDING, FLAME_ENDING, MORTAR_ENDING -> this.setHeadState(head, HydraHeadState.ATTACK_COOLDOWN);
            case FLAME_BEGINNING -> this.setHeadState(head, HydraHeadState.FLAMING);
            case FLAMING -> this.setHeadState(head, HydraHeadState.FLAME_ENDING);
            case MORTAR_BEGINNING -> this.setHeadState(head, HydraHeadState.MORTAR_SHOOTING);
            case MORTAR_SHOOTING -> this.setHeadState(head, HydraHeadState.MORTAR_ENDING);
            case ATTACK_COOLDOWN -> {
                this.headAttackCooldowns[head] = 45 + this.random.nextInt(45) + head * 10;
                this.setHeadState(head, HydraHeadState.IDLE);
            }
            case DYING -> this.setHeadState(head, HydraHeadState.DEAD);
            case BORN -> this.setHeadState(head, HydraHeadState.ROAR_START);
            case ROAR_START -> this.setHeadState(head, HydraHeadState.ROAR_RAWR);
            case ROAR_RAWR -> this.setHeadState(head, HydraHeadState.IDLE);
            default -> this.setHeadState(head, HydraHeadState.IDLE);
        }
    }

    private void setHeadState(int head, HydraHeadState state) {
        this.headStates[head] = state;
        this.headStateTicks[head] = 0;
        if (state == HydraHeadState.IDLE || state == HydraHeadState.DEAD || state == HydraHeadState.BORN || state == HydraHeadState.DYING) {
            this.headTargets[head] = null;
        }
        if (state == HydraHeadState.FLAME_BEGINNING) {
            this.playSound(TFSounds.HYDRA_WARN, 2.0F, 0.7F + this.getRandom().nextFloat() * 0.3F);
        } else if (state == HydraHeadState.MORTAR_BEGINNING) {
            this.playSound(TFSounds.HYDRA_ROAR, 1.25F, 0.7F + this.getRandom().nextFloat() * 0.3F);
        } else if (state == HydraHeadState.BITE_BEGINNING) {
            this.playSound(TFSounds.HYDRA_WARN, 2.0F, 0.7F + this.getRandom().nextFloat() * 0.3F);
        } else if (state == HydraHeadState.DYING) {
            this.playSound(TFSounds.HYDRA_HURT, 2.0F, 0.6F + this.getRandom().nextFloat() * 0.3F);
            this.setHeadNameFor(head, "");
            this.spawnHeadDeathParticles(head, -1);
        } else if (state == HydraHeadState.BORN) {
            this.playSound(TFSounds.HYDRA_ROAR, 1.25F, 0.7F + this.getRandom().nextFloat() * 0.3F);
            this.spawnHeadBirthParticles(head);
        } else if (state == HydraHeadState.ROAR_RAWR) {
            this.seedRoarTarget(head);
            this.playSound(TFSounds.HYDRA_ROAR, 1.25F, 0.7F + this.getRandom().nextFloat() * 0.3F);
        }
    }

    private void tickHeadRespawn(int head) {
        if (this.headStates[head] == HydraHeadState.DEAD && this.headRespawnTicks[head] > 0 && --this.headRespawnTicks[head] <= 0) {
            this.headDamageTaken[head] = 0;
            this.setHeadState(head, HydraHeadState.BORN);
        }
    }

    private void executeHeadAttack(int head, HydraHeadState state, LivingEntity target) {
        LivingEntity headTarget = this.getHeadTargetEntity(head, target);
        Vec3 mouth = this.getHeadMouthPosition(head);
        if (state == HydraHeadState.BITE_READY && this.headStateTicks[head] == 60) {
            this.playSound(TFSounds.HYDRA_WARN, 2.0F, 0.7F + this.getRandom().nextFloat() * 0.3F);
        }
        if (state == HydraHeadState.BITE_READY) {
            this.updateBiteReadyTarget(head, mouth);
        } else if (state == HydraHeadState.BITING && this.headStateTicks[head] == 0) {
            AABB biteBox = new AABB(mouth, mouth).inflate(2.4D, 1.6D, 2.4D);
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, biteBox, entity -> entity != this && entity.isAlive())) {
                entity.hurt(TFDamageTypes.entitySource(this.level(), TFDamageTypes.HYDRA_BITE, this), BITE_DAMAGE);
                entity.knockback(1.0D, this.getX() - entity.getX(), this.getZ() - entity.getZ());
            }
        } else if (state == HydraHeadState.FLAMING) {
            this.moveHeadTargetTowardsTarget(head, headTarget, this.getFlameBreathTrackingSpeed());
            this.breatheHeadFlame(head, mouth);
        } else if (state == HydraHeadState.MORTAR_SHOOTING && this.headStateTicks[head] % 10 == 0) {
            this.launchHeadMortar(head, mouth, headTarget);
        } else if (state == HydraHeadState.DYING) {
            this.tickHeadDeathParticles(head);
        }
    }

    private void tickHeadDeathParticles(int head) {
        int tick = this.headStateTicks[head];
        if (tick == 0) {
            this.spawnHeadDeathParticles(head, -1);
        } else if (tick == 10 || tick == 20 || tick == 30 || tick == 40 || tick == 50) {
            this.spawnHeadDeathParticles(head, tick / 10 - 1);
        }
    }

    private void spawnHeadDeathParticles(int head, int neckSegment) {
        Vec3 center = neckSegment < 0 ? this.getHeadMouthPosition(head) : this.getNeckSegmentPosition(head, neckSegment);
        for (int i = 0; i < 12; i++) {
            double x = center.x() + (this.random.nextDouble() - 0.5D) * 1.6D;
            double y = center.y() + (this.random.nextDouble() - 0.5D) * 1.2D;
            double z = center.z() + (this.random.nextDouble() - 0.5D) * 1.6D;
            this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.POOF : ParticleTypes.EXPLOSION, x, y, z, 0.0D, 0.02D, 0.0D);
        }
    }

    private void spawnHeadBirthParticles(int head) {
        Vec3 center = this.getHeadMouthPosition(head);
        for (int i = 0; i < 18; i++) {
            double x = center.x() + (this.random.nextDouble() - 0.5D) * 2.0D;
            double y = center.y() + (this.random.nextDouble() - 0.5D) * 1.4D;
            double z = center.z() + (this.random.nextDouble() - 0.5D) * 2.0D;
            this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.SMOKE : ParticleTypes.FLAME, x, y, z, 0.0D, 0.03D, 0.0D);
        }
    }

    private boolean anyHeadInState(HydraHeadState first, HydraHeadState second) {
        for (HydraHeadState state : this.headStates) {
            if (state == first || state == second) {
                return true;
            }
        }
        return false;
    }

    private void clientParticles() {
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(5.5D), this.getRandomY(), this.getRandomZ(5.5D), 0.0D, 0.0D, 0.0D);
        }
    }

    private void launchMortar(LivingEntity target) {
        HydraMortar mortar = new HydraMortar(this.level(), this);
        double sourceX = this.getX();
        double sourceY = this.getY() + this.getBbHeight() * 0.72D;
        double sourceZ = this.getZ();
        double deltaX = target.getX() - sourceX;
        double deltaY = target.getY() + target.getBbHeight() * 0.5D - sourceY;
        double deltaZ = target.getZ() - sourceZ;
        double horizontal = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        mortar.moveTo(sourceX, sourceY, sourceZ, this.getYRot(), this.getXRot());
        mortar.shoot(deltaX, deltaY + horizontal * 0.18D, deltaZ, 0.9F, 4.0F);
        this.playSound(TFSounds.HYDRA_SHOOT, 4.0F, 0.7F + this.getRandom().nextFloat() * 0.3F);
        this.level().addFreshEntity(mortar);
    }

    private void breatheFlame(LivingEntity target) {
        AABB breathBox = this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, breathBox, entity -> entity != this && entity.distanceToSqr(target) < 25.0D)) {
            if (entity.hurt(this.damageSources().mobAttack(this), 9.0F)) {
                entity.igniteForSeconds(3);
            }
        }
        this.playSound(TFSounds.HYDRA_SHOOT_FIRE, 4.0F, 0.5F + this.getRandom().nextFloat() * 0.4F);
        for (int i = 0; i < 16; i++) {
            this.level().addParticle(ParticleTypes.FLAME, target.getRandomX(1.0D), target.getRandomY(), target.getRandomZ(1.0D), 0.0D, 0.02D, 0.0D);
        }
    }

    private void updateMultipartDisplays() {
        for (int head = 0; head < HYDRA_HEADS; head++) {
            HydraHeadPose pose = this.getCurrentHeadPose(head);
            Vec3 headPosition = this.getHeadPosition(head, pose);
            Vec3 neckAnchor = this.getNeckBasePosition(head);
            this.updateHydraHeadPart(head, headPosition, this.getHeadDisplayYRot(head, pose, headPosition), this.getHeadDisplayXRot(head, pose, headPosition), pose.mouthOpen());
            this.updateHydraNeckParts(head, neckAnchor, headPosition, this.getNeckYawOffset(head));
        }
    }

    private boolean isHeadFlaming(int head) {
        return this.headStates[head] == HydraHeadState.FLAMING || this.headStates[head] == HydraHeadState.FLAME_BEGINNING;
    }

    private boolean isHeadVulnerable(int head) {
        HydraHeadState state = this.headStates[head];
        return state == HydraHeadState.BITING || state == HydraHeadState.FLAMING || state == HydraHeadState.MORTAR_SHOOTING;
    }

    private boolean areTooManyHeadsAttacking(int testHead) {
        int attackCount = 0;
        for (int head = 0; head < this.headStates.length; head++) {
            if (head != testHead && this.isHeadAttacking(head)) {
                attackCount++;
                if (this.isHeadBiting(head)) {
                    attackCount += 2;
                }
            }
        }
        return attackCount >= 1 + this.countActiveHeads() * this.getHeadsActivityFactor();
    }

    private int countActiveHeads() {
        int count = 0;
        for (int head = 0; head < this.headStates.length; head++) {
            if (!this.isHeadDead(head)) {
                count++;
            }
        }
        return count;
    }

    private float getHeadsActivityFactor() {
        return this.level().getDifficulty() == Difficulty.HARD ? 0.5F : 0.3F;
    }

    private boolean isHeadAttacking(int head) {
        HydraHeadState state = this.headStates[head];
        return state == HydraHeadState.BITE_BEGINNING || state == HydraHeadState.BITE_READY || state == HydraHeadState.BITING || state == HydraHeadState.FLAME_BEGINNING || state == HydraHeadState.FLAMING || state == HydraHeadState.MORTAR_BEGINNING || state == HydraHeadState.MORTAR_SHOOTING;
    }

    private boolean isHeadBiting(int head) {
        HydraHeadState state = this.headStates[head];
        return state == HydraHeadState.BITE_BEGINNING || state == HydraHeadState.BITE_READY || state == HydraHeadState.BITING;
    }

    private boolean areOtherHeadsBiting(int testHead) {
        for (int head = 0; head < this.headStates.length; head++) {
            if (head != testHead && this.isHeadBiting(head)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHeadDead(int head) {
        HydraHeadState state = this.headStates[head];
        return state == HydraHeadState.DYING || state == HydraHeadState.DEAD;
    }

    private void addHeadDamage(int head, float amount) {
        this.headDamageTaken[head] += Math.max(1, Math.round(amount));
        if (!this.isHeadDead(head) && this.headDamageTaken[head] > HEAD_MAX_DAMAGE) {
            this.killHead(head);
        }
    }

    private void killHead(int head) {
        this.headAttackCooldowns[head] = HEAD_RESPAWN_TICKS;
        this.headRespawnTicks[head] = HEAD_RESPAWN_TICKS;
        this.headTargets[head] = null;
        this.setHeadState(head, HydraHeadState.DYING);
        int otherHead = this.getRandomDeadHead();
        if (otherHead >= 0) {
            this.headRespawnTicks[otherHead] = HEAD_RESPAWN_TICKS;
        }
    }

    private int getRandomDeadHead() {
        List<Integer> candidates = new ArrayList<>();
        for (int head = 0; head < this.headStates.length; head++) {
            if (this.canHeadRespawn(head)) {
                candidates.add(head);
            }
        }
        return candidates.isEmpty() ? -1 : candidates.get(this.random.nextInt(candidates.size()));
    }

    private boolean canHeadRespawn(int head) {
        return this.headStates[head] == HydraHeadState.DEAD && this.headRespawnTicks[head] <= 0;
    }

    private void updateHydraHeadPart(int index, Vec3 position, float yRot, float pitch, float mouthOpen) {
        HydraHead head = this.headParts[index];
        head.tick();
        if (this.headStates[index] == HydraHeadState.DEAD) {
            head.deactivate();
            return;
        }
        head.activate();
        head.setMouthOpen(mouthOpen);
        String name = this.getHeadNameFor(index);
        head.setCustomName(name.isBlank() ? null : Component.literal(name));
        head.setPos(position.x(), position.y(), position.z());
        head.setYRot(yRot);
        head.setXRot(pitch);
        head.yRotO = yRot;
        head.xRotO = pitch;
        head.renderYawOffset = yRot;
    }

    private String getHeadNameFor(int index) {
        return index >= 0 && index < this.headNames.length && this.headNames[index] != null ? this.headNames[index] : "";
    }

    public void setHeadNameFor(int index, String name) {
        if (index < 0 || index >= this.headNames.length) {
            return;
        }
        this.headNames[index] = name == null ? "" : name;
        this.headParts[index].setCustomName(this.headNames[index].isBlank() ? null : Component.literal(this.headNames[index]));
    }

    private void updateHydraNeckParts(int headIndex, Vec3 start, Vec3 end, float yawOffset) {
        if (this.headStates[headIndex] == HydraHeadState.DEAD) {
            for (int segment = 0; segment < HYDRA_NECK_SEGMENTS; segment++) {
                this.neckParts[headIndex][segment].tick();
                this.neckParts[headIndex][segment].deactivate();
            }
            return;
        }
        for (int segment = 0; segment < HYDRA_NECK_SEGMENTS; segment++) {
            double factor = (double) segment / (double) (HYDRA_NECK_SEGMENTS - 1);
            Vec3 position = new Vec3(Mth.lerp(factor, end.x(), start.x()), Mth.lerp(factor, end.y(), start.y()), Mth.lerp(factor, end.z(), start.z()));
            HydraNeck neck = this.neckParts[headIndex][segment];
            neck.tick();
            neck.activate();
            float pitch = (float) Mth.lerp(factor, -16.0D, -4.0D);
            float yRot = this.yBodyRot + yawOffset;
            neck.setPos(position.x(), position.y(), position.z());
            neck.setYRot(yRot);
            neck.setXRot(pitch);
            neck.yRotO = yRot;
            neck.xRotO = pitch;
            neck.renderYawOffset = yRot;
        }
    }

    private Vec3 localMultipartPosition(double localX, double localY, double localZ) {
        float yaw = this.yBodyRot * Mth.PI / 180.0F;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        double worldX = this.getX() + localX * cos - localZ * sin;
        double worldZ = this.getZ() + localX * sin + localZ * cos;
        return new Vec3(worldX, this.getY() + localY, worldZ);
    }

    private HydraHeadPose getCurrentHeadPose(int head) {
        return this.getHeadPose(head, this.headStates[head]);
    }

    private HydraHeadPose getHeadPose(int head, HydraHeadState state) {
        return switch (state) {
            case BITE_BEGINNING -> switch (head) {
                case 1 -> new HydraHeadPose(-10.0F, 60.0F, 9.0F, 0.25F);
                case 2 -> new HydraHeadPose(-10.0F, -60.0F, 9.0F, 0.25F);
                default -> new HydraHeadPose(-5.0F, 60.0F, 5.0F, 0.25F);
            };
            case BITE_READY -> switch (head) {
                case 1 -> new HydraHeadPose(-10.0F, 60.0F, 9.0F, 1.0F);
                case 2 -> new HydraHeadPose(-10.0F, -60.0F, 9.0F, 1.0F);
                default -> new HydraHeadPose(-5.0F, 60.0F, 5.0F, 1.0F);
            };
            case BITING -> switch (head) {
                case 1 -> new HydraHeadPose(-10.0F, -30.0F, 5.0F, 0.2F);
                case 2 -> new HydraHeadPose(-10.0F, 30.0F, 5.0F, 0.2F);
                default -> new HydraHeadPose(-5.0F, -30.0F, 5.0F, 0.2F);
            };
            case BITE_ENDING -> switch (head) {
                case 1 -> new HydraHeadPose(-10.0F, 60.0F, 9.0F, 0.0F);
                case 2 -> new HydraHeadPose(-10.0F, -60.0F, 9.0F, 0.0F);
                default -> new HydraHeadPose(60.0F, 0.0F, 7.0F, 0.0F);
            };
            case FLAME_BEGINNING, MORTAR_BEGINNING -> this.getRangedPose(head, 0.75F, false);
            case FLAMING, MORTAR_SHOOTING -> this.getRangedPose(head, 1.0F, true);
            case FLAME_ENDING, MORTAR_ENDING -> this.getEndingPose(head);
            case DYING -> switch (head) {
                case 5, 6 -> this.getBasePose(head, -10.0F, 0.0F);
                default -> this.getBasePose(head, -20.0F, 0.0F);
            };
            case DEAD -> new HydraHeadPose(0.0F, head == 2 || head == 4 || head == 6 ? -180.0F : 179.0F, 4.0F, 0.0F);
            case ROAR_START -> this.getRoarPose(head, 0.25F, false);
            case ROAR_RAWR -> this.getRoarPose(head, 1.0F, true);
            default -> this.getIdlePose(head);
        };
    }

    private HydraHeadPose getIdlePose(int head) {
        return switch (head) {
            case 1 -> new HydraHeadPose(10.0F, 60.0F, 9.0F, 0.0F);
            case 2 -> new HydraHeadPose(10.0F, -60.0F, 9.0F, 0.0F);
            case 3 -> new HydraHeadPose(50.0F, 90.0F, 8.0F, 0.0F);
            case 4 -> new HydraHeadPose(50.0F, -90.0F, 8.0F, 0.0F);
            case 5 -> new HydraHeadPose(-10.0F, 90.0F, 9.0F, 0.0F);
            case 6 -> new HydraHeadPose(-10.0F, -90.0F, 9.0F, 0.0F);
            default -> new HydraHeadPose(60.0F, 0.0F, 7.0F, 0.0F);
        };
    }

    private HydraHeadPose getRangedPose(int head, float mouthOpen, boolean active) {
        return switch (head) {
            case 1 -> new HydraHeadPose(30.0F, active ? 60.0F : 45.0F, 9.0F, mouthOpen);
            case 2 -> new HydraHeadPose(30.0F, active ? -60.0F : -45.0F, 9.0F, mouthOpen);
            case 3 -> new HydraHeadPose(50.0F, 90.0F, 8.0F, mouthOpen);
            case 4 -> new HydraHeadPose(50.0F, -90.0F, 8.0F, mouthOpen);
            case 5 -> new HydraHeadPose(-10.0F, 90.0F, 9.0F, mouthOpen);
            case 6 -> new HydraHeadPose(-10.0F, -90.0F, 9.0F, mouthOpen);
            default -> new HydraHeadPose(active ? 45.0F : 50.0F, 0.0F, 8.0F, mouthOpen);
        };
    }

    private HydraHeadPose getEndingPose(int head) {
        return switch (head) {
            case 1 -> new HydraHeadPose(10.0F, 45.0F, 9.0F, 0.0F);
            case 2 -> new HydraHeadPose(10.0F, -45.0F, 9.0F, 0.0F);
            default -> this.getIdlePose(head);
        };
    }

    private HydraHeadPose getBasePose(int head, float xRotation, float mouthOpen) {
        HydraHeadPose idle = this.getIdlePose(head);
        return new HydraHeadPose(xRotation, idle.yRotation(), idle.neckLength(), mouthOpen);
    }

    private HydraHeadPose getRoarPose(int head, float mouthOpen, boolean rawr) {
        HydraHeadPose idle = this.getIdlePose(head);
        return new HydraHeadPose(idle.xRotation(), idle.yRotation(), rawr ? idle.neckLength() + 2.0F : idle.neckLength(), mouthOpen);
    }

    private Vec3 getHeadPosition(int head, HydraHeadPose pose) {
        float xSwing = this.getHeadSwingX(head);
        float ySwing = this.getHeadSwingY(head);
        if (this.isHeadDead(head)) {
            xSwing = 0.0F;
            ySwing = 0.0F;
        }
        Vec3 vector = new Vec3(0.0D, 0.0D, pose.neckLength());
        vector = vector.xRot((pose.xRotation() * Mth.PI + xSwing) / 180.0F);
        vector = vector.yRot((-(this.yBodyRot + pose.yRotation() + ySwing) * Mth.PI) / 180.0F);
        return new Vec3(this.getX() + vector.x(), this.getY() + vector.y() + 3.0D, this.getZ() + vector.z());
    }

    private float getHeadSwingX(int head) {
        float period = head == 0 || head == 3 ? 20.0F : (head == 1 || head == 4 ? 5.0F : 7.0F);
        return Mth.sin(this.tickCount / period) * 3.0F;
    }

    private float getHeadSwingY(int head) {
        float period = head == 0 || head == 4 ? 10.0F : (head == 1 || head == 6 ? 6.0F : 5.0F);
        return Mth.sin(this.tickCount / period) * 5.0F;
    }

    private float getHeadYawOffset(int head, HydraHeadPose pose) {
        return pose.yRotation() + (this.isHeadDead(head) ? 0.0F : this.getHeadSwingY(head));
    }

    private float getHeadDisplayYRot(int head, HydraHeadPose pose, Vec3 position) {
        if (this.shouldHeadFaceTarget(head) && this.hasHeadTarget(head, position)) {
            return this.getHeadTargetYaw(position, head);
        }
        return this.yBodyRot + this.getHeadYawOffset(head, pose);
    }

    private float getHeadDisplayXRot(int head, HydraHeadPose pose, Vec3 position) {
        if (this.shouldHeadFaceTarget(head) && this.hasHeadTarget(head, position)) {
            return this.getHeadTargetPitch(position, head);
        }
        return pose.xRotation();
    }

    private boolean shouldHeadFaceTarget(int head) {
        HydraHeadState state = this.headStates[head];
        return state == HydraHeadState.FLAME_BEGINNING || state == HydraHeadState.FLAMING || state == HydraHeadState.BITE_READY || state == HydraHeadState.BITING || state == HydraHeadState.BITE_ENDING || state == HydraHeadState.ROAR_RAWR;
    }

    private boolean hasHeadTarget(int head, Vec3 position) {
        double dx = this.headTargetX[head] - position.x();
        double dy = this.headTargetY[head] - position.y();
        double dz = this.headTargetZ[head] - position.z();
        return dx * dx + dy * dy + dz * dz > 1.0E-4D;
    }

    private float getHeadTargetYaw(Vec3 position, int head) {
        double dx = this.headTargetX[head] - position.x();
        double dz = this.headTargetZ[head] - position.z();
        float yaw = Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * 180.0F / Mth.PI) - 90.0F);
        return this.isBiteReadyHead(head) ? this.clampBiteYaw(head, yaw) : yaw;
    }

    private float getHeadTargetPitch(Vec3 position, int head) {
        double dx = this.headTargetX[head] - position.x();
        double dy = this.headTargetY[head] - position.y();
        double dz = this.headTargetZ[head] - position.z();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) (-(Mth.atan2(dy, horizontal) * 180.0F / Mth.PI));
        if (this.headStates[head] == HydraHeadState.BITING || this.headStates[head] == HydraHeadState.BITE_ENDING) {
            pitch -= 45.0F;
        }
        return Mth.clamp(pitch, -this.getMaxHeadXRot(), this.getMaxHeadXRot());
    }

    private boolean isBiteReadyHead(int head) {
        return this.headStates[head] == HydraHeadState.BITE_READY && head < 3;
    }

    private float clampBiteYaw(int head, float yRot) {
        float yawOffset = Mth.wrapDegrees(yRot - this.yBodyRot);
        float biteMaxYaw = head == 2 ? 90.0F : -60.0F;
        float biteMinYaw = head == 2 ? 60.0F : -90.0F;
        return this.yBodyRot + Mth.clamp(yawOffset, biteMinYaw, biteMaxYaw);
    }

    private void updateBiteReadyTarget(int head, Vec3 mouth) {
        Vec3 look = Vec3.directionFromRotation(this.getHeadTargetPitch(mouth, head), this.getHeadTargetYaw(mouth, head));
        this.setHeadTarget(head, mouth.add(look.scale(16.0D)).add(0.0D, 1.5D, 0.0D));
    }

    private void seedRoarTarget(int head) {
        HydraHeadPose pose = this.getCurrentHeadPose(head);
        Vec3 headPosition = this.getHeadPosition(head, pose);
        if (!this.hasHeadTarget(head, headPosition)) {
            Vec3 look = Vec3.directionFromRotation(pose.xRotation(), this.yBodyRot + this.getHeadYawOffset(head, pose));
            this.setHeadTarget(head, headPosition.add(look.scale(16.0D)));
        }
    }

    private Vec3 getNeckBasePosition(int head) {
        double[][] anchors = this.getNeckAnchors(head);
        return this.localMultipartPosition(anchors[0][0], anchors[0][1], anchors[0][2]);
    }

    private float getNeckYawOffset(int head) {
        return switch (head) {
            case 1 -> 90.0F;
            case 2 -> -90.0F;
            case 3, 5 -> 135.0F;
            case 4, 6 -> -135.0F;
            default -> 0.0F;
        };
    }

    private Vec3 getHeadMouthPosition(int head) {
        HydraHeadPose pose = this.getCurrentHeadPose(head);
        Vec3 headPosition = this.getHeadPosition(head, pose);
        Vec3 look = Vec3.directionFromRotation(this.getHeadDisplayXRot(head, pose, headPosition), this.getHeadDisplayYRot(head, pose, headPosition));
        return headPosition.add(look.scale(1.25D));
    }

    private Vec3 getNeckSegmentPosition(int head, int segment) {
        Vec3 start = this.getNeckBasePosition(head);
        Vec3 end = this.getHeadPosition(head, this.getCurrentHeadPose(head));
        double factor = (double) Mth.clamp(segment, 0, HYDRA_NECK_SEGMENTS - 1) / (double) (HYDRA_NECK_SEGMENTS - 1);
        return new Vec3(Mth.lerp(factor, end.x(), start.x()), Mth.lerp(factor, end.y(), start.y()), Mth.lerp(factor, end.z(), start.z()));
    }

    private void setHeadTarget(int head, LivingEntity target) {
        this.headTargets[head] = target;
        this.setHeadTarget(head, target.getEyePosition());
    }

    private void setHeadTarget(int head, Vec3 target) {
        this.headTargetX[head] = target.x();
        this.headTargetY[head] = target.y();
        this.headTargetZ[head] = target.z();
    }

    private void refreshHeadTarget(int head, LivingEntity primaryTarget) {
        LivingEntity target = this.headTargets[head];
        if (target == null || !target.isAlive()) {
            this.headTargets[head] = primaryTarget;
            if (this.headStates[head] == HydraHeadState.IDLE) {
                this.setHeadTarget(head, primaryTarget.getEyePosition());
            }
        }
    }

    private LivingEntity getHeadTargetEntity(int head, LivingEntity fallback) {
        LivingEntity target = this.headTargets[head];
        return target != null && target.isAlive() ? target : fallback;
    }

    private void moveHeadTargetTowardsTarget(int head, LivingEntity target, double distance) {
        Vec3 current = new Vec3(this.headTargetX[head], this.headTargetY[head], this.headTargetZ[head]);
        Vec3 next = target.getEyePosition().subtract(current);
        if (next.lengthSqr() > 1.0E-4D) {
            this.setHeadTarget(head, current.add(next.normalize().scale(distance)));
        }
    }

    private double getFlameBreathTrackingSpeed() {
        return this.level().getDifficulty() == Difficulty.HARD ? 0.1D : 0.04D;
    }

    private double[][] getNeckAnchors(int head) {
        return switch (head) {
            case 1 -> new double[][]{{-1.35D, 2.0D, -1.05D}, {-2.05D, 2.35D, -2.05D}};
            case 2 -> new double[][]{{1.35D, 2.0D, -1.05D}, {2.05D, 2.35D, -2.05D}};
            case 3 -> new double[][]{{-1.45D, 1.95D, 0.2D}, {-2.55D, 2.15D, 0.25D}};
            case 4 -> new double[][]{{1.45D, 1.95D, 0.2D}, {2.55D, 2.15D, 0.25D}};
            case 5 -> new double[][]{{-1.15D, 1.65D, 1.55D}, {-2.15D, 1.85D, 2.45D}};
            case 6 -> new double[][]{{1.15D, 1.65D, 1.55D}, {2.15D, 1.85D, 2.45D}};
            default -> new double[][]{{0.0D, 2.15D, -1.35D}, {0.0D, 2.65D, -2.75D}};
        };
    }

    private void launchHeadMortar(int head, Vec3 mouth, LivingEntity target) {
        HydraMortar mortar = new HydraMortar(this.level(), this);
        if (!this.getSensing().hasLineOfSight(target)) {
            mortar.setToBlasting();
        }
        double deltaX = target.getX() - mouth.x();
        double deltaY = target.getY() + target.getBbHeight() * 0.5D - mouth.y();
        double deltaZ = target.getZ() - mouth.z();
        double horizontal = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        mortar.moveTo(mouth.x(), mouth.y(), mouth.z(), this.getYRot(), this.getXRot());
        mortar.shoot(deltaX, deltaY + horizontal * 0.18D, deltaZ, 0.9F, 4.0F);
        this.playSound(TFSounds.HYDRA_SHOOT, 4.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
        this.level().addFreshEntity(mortar);
    }

    private void breatheHeadFlame(int head, Vec3 mouth) {
        Vec3 aim = new Vec3(this.headTargetX[head] - mouth.x(), this.headTargetY[head] - mouth.y(), this.headTargetZ[head] - mouth.z()).normalize();
        AABB breathBox = new AABB(mouth, mouth.add(aim.scale(8.0D))).inflate(2.2D);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, breathBox, entity -> entity != this && entity.isAlive())) {
            if (!entity.fireImmune() && entity.hurt(TFDamageTypes.entitySource(this.level(), TFDamageTypes.HYDRA_FIRE, this), FLAME_DAMAGE)) {
                entity.igniteForSeconds(FLAME_BURN_SECONDS);
            }
        }
        if (this.tickCount % 5 == 0) {
            this.playSound(TFSounds.HYDRA_SHOOT_FIRE, 0.8F + this.getRandom().nextFloat(), this.getRandom().nextFloat() * 0.7F + 0.3F);
        }
        for (int i = 0; i < 5; i++) {
            Vec3 particle = mouth.add(aim.scale(1.5D + this.random.nextDouble() * 6.0D));
            this.level().addParticle(ParticleTypes.FLAME, particle.x(), particle.y(), particle.z(), aim.x() * 0.1D, aim.y() * 0.1D, aim.z() * 0.1D);
        }
    }

    private void removeMultipartDisplays() {
        for (HydraHead head : this.headParts) {
            head.deactivate();
        }
        for (HydraNeck[] neckChain : this.neckParts) {
            for (HydraNeck neck : neckChain) {
                neck.deactivate();
            }
        }
    }

    public void destroyBlocksInAABB(AABB box) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        if (this.level().hasChunksAt(min, max)) {
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                if (EntityUtil.canDestroyBlock(this.level(), pos, this)) {
                    this.level().destroyBlock(pos, false);
                }
            }
        }
    }

    private void collideWithMultipartBody() {
        AABB bodyBox = this.getHydraBodyBox();
        AABB tailBox = this.getHydraTailBox();
        this.collideWithEntities(this.level().getEntities(this, bodyBox), bodyBox);
        this.collideWithEntities(this.level().getEntities(this, tailBox), tailBox);
    }

    private AABB getHydraBodyBox() {
        Vec3 center = this.position().add(this.getBackOffset(3.0D)).add(0.0D, 1.2D, 0.0D);
        return new AABB(center, center).inflate(2.8D, 1.4D, 2.8D);
    }

    private AABB getHydraTailBox() {
        Vec3 center = this.position().add(this.getBackOffset(10.5D)).add(0.0D, 0.8D, 0.0D);
        return new AABB(center, center).inflate(2.0D, 1.0D, 2.0D);
    }

    private Vec3 getBackOffset(double distance) {
        float angle = (this.yBodyRot + 180.0F) * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(angle) * distance, 0.0D, Mth.cos(angle) * distance);
    }

    private void collideWithEntities(List<Entity> entities, AABB partBox) {
        double centerX = (partBox.minX + partBox.maxX) * 0.5D;
        double centerZ = (partBox.minZ + partBox.maxZ) * 0.5D;
        for (Entity entity : entities) {
            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }
            if (entity instanceof LivingEntity) {
                double deltaX = entity.getX() - centerX;
                double deltaZ = entity.getZ() - centerZ;
                double distance = Math.max(deltaX * deltaX + deltaZ * deltaZ, 0.1D);
                entity.push(deltaX / distance * 8.0D, 0.2D, deltaZ / distance * 8.0D);
            }
        }
    }

    private boolean isUnsteadySurfaceBeneath() {
        int minX = Mth.floor(this.getBoundingBox().minX);
        int minZ = Mth.floor(this.getBoundingBox().minZ);
        int maxX = Mth.floor(this.getBoundingBox().maxX);
        int maxZ = Mth.floor(this.getBoundingBox().maxZ);
        int y = Mth.floor(this.getBoundingBox().minY) - 1;
        int solid = 0;
        int total = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                total++;
                if (this.level().getBlockState(new BlockPos(x, y, z)).isSolid()) {
                    solid++;
                }
            }
        }
        return total > 0 && (float) solid / (float) total < 0.6F;
    }

    private void destroyMultipartBlocks() {
        for (int head = 0; head < HYDRA_HEADS; head++) {
            if (this.isHeadDead(head)) {
                continue;
            }
            HydraHeadPose pose = this.getCurrentHeadPose(head);
            Vec3 headPosition = this.getHeadPosition(head, pose);
            this.destroyBlocksInAABB(new AABB(headPosition, headPosition).inflate(1.15D, 1.15D, 1.15D));
            for (int segment = 0; segment < HYDRA_NECK_SEGMENTS; segment += 2) {
                Vec3 neckPosition = this.getNeckSegmentPosition(head, segment);
                this.destroyBlocksInAABB(new AABB(neckPosition, neckPosition).inflate(0.6D, 0.6D, 0.6D));
            }
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void knockback(double strength, double xRatio, double zRatio) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(source, amount);
        if (hurt) {
            this.ticksSinceDamaged = 0;
        }
        return hurt;
    }

    public boolean attackEntityFromPart(HydraPart part, DamageSource source, float amount) {
        if (source.getEntity() == this || source.getDirectEntity() == this || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        if (!this.isMultipartDamageInRange(source)) {
            return false;
        }
        if (part instanceof HydraHead hydraHead) {
            int head = hydraHead.getIndex();
            if (this.isHeadDead(head)) {
                return false;
            }
            float applied = this.isHeadVulnerable(head) ? amount : Math.max(1.0F, amount / ARMOR_MULTIPLIER);
            boolean hurt = super.hurt(source, applied);
            if (hurt) {
                this.ticksSinceDamaged = 0;
                this.addHeadDamage(head, applied);
                this.headAttackCooldowns[head] = Math.max(this.headAttackCooldowns[head], 20);
            }
            return hurt;
        }
        if (part instanceof HydraNeck neck) {
            boolean hurt = super.hurt(source, Math.max(1.0F, amount / ARMOR_MULTIPLIER));
            if (hurt) {
                this.ticksSinceDamaged = 0;
            }
            return hurt;
        }
        return false;
    }

    private boolean isMultipartDamageInRange(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return true;
        }
        double maxRange = source.getDirectEntity() instanceof HydraMortar ? 600.0D : 400.0D;
        return this.distanceToSqr(attacker) <= maxRange;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFSounds.HYDRA_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFSounds.HYDRA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFSounds.HYDRA_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.removeMultipartDisplays();
    }

    @Override
    public void remove(RemovalReason reason) {
        this.removeMultipartDisplays();
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TicksSinceDamaged", this.ticksSinceDamaged);
        tag.putInt("MortarCooldown", this.mortarCooldown);
        tag.putInt("FlameCooldown", this.flameCooldown);
        tag.putIntArray("HeadStates", this.headStateOrdinals());
        tag.putIntArray("HeadStateTicks", this.headStateTicks);
        tag.putIntArray("HeadAttackCooldowns", this.headAttackCooldowns);
        tag.putIntArray("HeadDamageTaken", this.headDamageTaken);
        tag.putIntArray("HeadRespawnTicks", this.headRespawnTicks);
        ListTag headNames = new ListTag();
        for (int head = 0; head < HYDRA_HEADS; head++) {
            headNames.add(StringTag.valueOf(this.getHeadNameFor(head)));
        }
        tag.put("HeadNames", headNames);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksSinceDamaged = tag.getInt("TicksSinceDamaged");
        this.mortarCooldown = tag.getInt("MortarCooldown");
        this.flameCooldown = tag.getInt("FlameCooldown");
        this.readHeadStates(tag.getIntArray("HeadStates"));
        this.readIntArray(tag.getIntArray("HeadStateTicks"), this.headStateTicks);
        this.readIntArray(tag.getIntArray("HeadAttackCooldowns"), this.headAttackCooldowns);
        this.readIntArray(tag.getIntArray("HeadDamageTaken"), this.headDamageTaken);
        this.readIntArray(tag.getIntArray("HeadRespawnTicks"), this.headRespawnTicks);
        this.readHeadNames(tag);
    }

    private void readHeadNames(CompoundTag tag) {
        if (!tag.contains("HeadNames", Tag.TAG_LIST)) {
            return;
        }
        ListTag names = tag.getList("HeadNames", Tag.TAG_STRING);
        for (int head = 0; head < HYDRA_HEADS; head++) {
            this.headNames[head] = head < names.size() ? names.getString(head) : "";
        }
    }

    private int[] headStateOrdinals() {
        int[] states = new int[HYDRA_HEADS];
        for (int head = 0; head < HYDRA_HEADS; head++) {
            states[head] = this.headStates[head].ordinal();
        }
        return states;
    }

    private void readHeadStates(int[] states) {
        HydraHeadState[] values = HydraHeadState.values();
        for (int head = 0; head < HYDRA_HEADS; head++) {
            int state = head < states.length ? states[head] : HydraHeadState.IDLE.ordinal();
            this.headStates[head] = state >= 0 && state < values.length ? values[state] : HydraHeadState.IDLE;
        }
    }

    private void readIntArray(int[] source, int[] target) {
        for (int index = 0; index < target.length; index++) {
            target[index] = index < source.length ? source[index] : 0;
        }
    }

    @Override
    public TFPart<?>[] getParts() {
        return this.partArray;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        TFPart.assignPartIDs(this);
    }
}
