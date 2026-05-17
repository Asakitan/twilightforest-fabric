package twilightforest.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.monster.CarminiteGhastling;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFEntities;
import twilightforest.init.TFStructures;

import java.util.List;

/**
 * 3-phase Final Castle boss for Codex Twilight.
 *
 * Phase 1 (HP 100%→67%) Mage: hovers, lightning + Ghastling summons, 50% melee resist.
 * Phase 2 (HP 67%→33%) Ice armor: grounded, frost aura + cone shockwave, ranged-resistant.
 * Phase 3 (HP 33%→0%) Berserk: high speed + charge attack, no special resists.
 *
 * Spawning is driven by {@link twilightforest.block.entity.spawner.FinalBossSpawnerBlockEntity}
 * within the {@code codex_twilight:final_castle} structure.
 */
public class PlateauBoss extends BaseTFBoss {

    private static final EntityDataAccessor<Byte> DATA_PHASE =
            SynchedEntityData.defineId(PlateauBoss.class, EntityDataSerializers.BYTE);

    private static final int LIGHTNING_COOLDOWN_TICKS = 80;     // 4s
    private static final int SUMMON_COOLDOWN_TICKS = 240;       // 12s
    private static final int CONE_COOLDOWN_TICKS = 100;         // 5s
    private static final int CHARGE_COOLDOWN_TICKS = 60;        // 3s
    private static final int MAX_GHASTLINGS_NEARBY = 3;

    private int lightningCooldown = 40;
    private int summonCooldown = 60;
    private int coneCooldown = 60;
    private int chargeCooldown = 60;

    public PlateauBoss(EntityType<? extends PlateauBoss> type, Level level) {
        super(type, level);
        this.xpReward = 647;
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, (byte) 1);
    }

    public int getPhase() {
        return this.getEntityData().get(DATA_PHASE);
    }

    private void setPhase(int phase) {
        this.getEntityData().set(DATA_PHASE, (byte) phase);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                // Phase 1 prefers ranged; phase 2 close-quarters; phase 3 full melee aggression.
                return PlateauBoss.this.getPhase() != 1 && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Phase-specific defensive modifiers BEFORE applying damage.
        if (!this.level().isClientSide()) {
            int phase = this.getPhase();
            boolean isProjectileOrMagic = source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
                    || source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO);
            boolean isMelee = source.getDirectEntity() instanceof LivingEntity
                    && !isProjectileOrMagic;

            if (phase == 1 && isMelee) {
                amount *= 0.5F;  // P1 mage: 50% reduction vs melee
            } else if (phase == 2 && !isMelee) {
                amount *= 0.5F;  // P2 ice armor: 50% reduction vs ranged
            }
        }

        boolean result = super.hurt(source, amount);

        // Apply phase transitions AFTER damage has been written to health.
        if (!this.level().isClientSide() && result) {
            updatePhaseFromHealth();
        }
        return result;
    }

    private void updatePhaseFromHealth() {
        float ratio = this.getHealth() / this.getMaxHealth();
        int target;
        if (ratio > 0.67F) target = 1;
        else if (ratio > 0.33F) target = 2;
        else target = 3;

        if (target != this.getPhase()) {
            this.setPhase(target);
            onPhaseEntered(target);
        }
    }

    private void onPhaseEntered(int phase) {
        // Audible/visual transition cue + heal-tick for fairness.
        this.playSound(SoundEvents.WITHER_SPAWN, 4.0F, 0.6F + 0.2F * phase);
        // Brief invuln window via short-lived resistance to soften phase-change combos.
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 4, false, false));
    }

    @Override
    protected void tickBossBar() {
        super.tickBossBar();
        int phase = this.getPhase();
        BossEvent.BossBarColor color = phase == 1 ? BossEvent.BossBarColor.WHITE
                : phase == 2 ? BossEvent.BossBarColor.BLUE
                : BossEvent.BossBarColor.RED;
        this.getBossBar().setColor(color);
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.WHITE;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.level().isClientSide()) return;

        LivingEntity target = this.getTarget();
        int phase = this.getPhase();

        if (lightningCooldown > 0) lightningCooldown--;
        if (summonCooldown > 0) summonCooldown--;
        if (coneCooldown > 0) coneCooldown--;
        if (chargeCooldown > 0) chargeCooldown--;

        switch (phase) {
            case 1 -> tickMagePhase(target);
            case 2 -> tickIceArmorPhase(target);
            case 3 -> tickBerserkPhase(target);
        }

        emitPhaseAura(phase);
    }

    private void emitPhaseAura(int phase) {
        if (!(this.level() instanceof ServerLevel server)) return;
        // Subtle ambient particles so players can read the current phase at a glance.
        net.minecraft.core.particles.ParticleOptions particle = switch (phase) {
            case 2 -> net.minecraft.core.particles.ParticleTypes.SNOWFLAKE;
            case 3 -> net.minecraft.core.particles.ParticleTypes.FLAME;
            default -> net.minecraft.core.particles.ParticleTypes.END_ROD;
        };
        server.sendParticles(particle,
                this.getX(), this.getY() + 1.8D, this.getZ(),
                3, 0.5D, 0.8D, 0.5D, 0.01D);
    }

    // ============ PHASE 1: MAGE ============

    private void tickMagePhase(LivingEntity target) {
        // Hover ~4 blocks above ground while a target exists.
        if (target != null && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.42D, 0.0D));
        } else if (target != null) {
            // Maintain altitude — apply small upward correction when below desired height.
            double desiredY = findGroundY(this.blockPosition()) + 4.0D;
            if (this.getY() < desiredY) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
            }
            // Slow gravity for floaty feel
            Vec3 v = this.getDeltaMovement();
            if (v.y < 0) this.setDeltaMovement(v.x, v.y * 0.6D, v.z);
        }

        if (target == null) return;

        // Lightning strike
        if (lightningCooldown <= 0 && this.hasLineOfSight(target)) {
            castLightningAt(target);
            lightningCooldown = LIGHTNING_COOLDOWN_TICKS;
        }

        // Summon ghastlings if too few nearby
        if (summonCooldown <= 0 && countNearbyGhastlings() < MAX_GHASTLINGS_NEARBY) {
            summonGhastlings();
            summonCooldown = SUMMON_COOLDOWN_TICKS;
        }
    }

    private void castLightningAt(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) return;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) return;
        BlockPos pos = target.blockPosition();
        bolt.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        bolt.setVisualOnly(false);
        server.addFreshEntity(bolt);
        this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 1.2F);
    }

    private int countNearbyGhastlings() {
        AABB box = this.getBoundingBox().inflate(20.0D);
        return this.level().getEntitiesOfClass(CarminiteGhastling.class, box).size();
    }

    private void summonGhastlings() {
        if (!(this.level() instanceof ServerLevel server)) return;
        int count = 2 + this.random.nextInt(2);  // 2-3
        RandomSource r = this.random;
        for (int i = 0; i < count; i++) {
            CarminiteGhastling g = TFEntities.CARMINITE_GHASTLING.get().create(server);
            if (g == null) continue;
            double dx = (r.nextDouble() - 0.5D) * 6.0D;
            double dz = (r.nextDouble() - 0.5D) * 6.0D;
            g.moveTo(this.getX() + dx, this.getY() + 2.0D, this.getZ() + dz,
                    r.nextFloat() * 360.0F, 0.0F);
            g.finalizeSpawn(server, server.getCurrentDifficultyAt(g.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null);
            if (this.getTarget() != null) g.setTarget(this.getTarget());
            server.addFreshEntity(g);
        }
        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 1.0F);
    }

    // ============ PHASE 2: ICE ARMOR ============

    private void tickIceArmorPhase(LivingEntity target) {
        // Frost aura: any non-boss entity within 6 blocks gets Slowness II for 60 ticks.
        AABB auraBox = this.getBoundingBox().inflate(6.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, auraBox,
                e -> !(e instanceof PlateauBoss) && e != this);
        for (LivingEntity e : targets) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
        }

        if (target == null) return;

        // Cone shockwave when on cooldown + target within 8 blocks in front cone.
        if (coneCooldown <= 0 && this.distanceTo(target) <= 8.0F && this.hasLineOfSight(target)) {
            castConeShock(target);
            coneCooldown = CONE_COOLDOWN_TICKS;
        }
    }

    private void castConeShock(LivingEntity primary) {
        if (!(this.level() instanceof ServerLevel server)) return;
        Vec3 forward = primary.position().subtract(this.position()).normalize();
        AABB box = this.getBoundingBox().inflate(8.0D);
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && !(e instanceof PlateauBoss));
        for (LivingEntity e : candidates) {
            Vec3 dir = e.position().subtract(this.position()).normalize();
            double dot = dir.dot(forward);
            if (dot < 0.5D) continue;  // ~60° half-cone
            // P2 cone damage = 10 * 1.3 = 13
            e.hurt(this.damageSources().source(TFDamageTypes.SLAM, this), 13.0F);
            Vec3 push = forward.scale(1.5D).add(0.0D, 0.4D, 0.0D);
            e.push(push.x, push.y, push.z);
        }
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                this.getX() + forward.x * 3, this.getY() + 1.0D, this.getZ() + forward.z * 3,
                40, 1.5D, 1.0D, 1.5D, 0.05D);
        this.playSound(SoundEvents.GLASS_BREAK, 3.0F, 0.6F);
    }

    // ============ PHASE 3: BERSERK ============

    private void tickBerserkPhase(LivingEntity target) {
        // Apply Speed II + Strength I as a buff aura on self while in phase 3.
        if (!this.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, false));
        }
        if (!this.hasEffect(MobEffects.DAMAGE_BOOST)) {
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, false));
        }

        if (target == null) return;

        // Charge attack: when target 5-20 blocks away, launch toward target.
        double dist = this.distanceTo(target);
        if (chargeCooldown <= 0 && dist >= 5.0D && dist <= 20.0D && this.hasLineOfSight(target)) {
            chargeAt(target);
            chargeCooldown = CHARGE_COOLDOWN_TICKS;
        }
    }

    private void chargeAt(LivingEntity target) {
        Vec3 dir = target.position().subtract(this.position()).normalize();
        // Vertical hop included so we clear small walls.
        this.setDeltaMovement(dir.x * 1.6D, 0.5D, dir.z * 1.6D);
        this.playSound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.8F);
        // Damage all entities along the charge path on the same tick of arrival.
        AABB sweep = this.getBoundingBox().expandTowards(dir.scale(2.0D));
        List<LivingEntity> hits = this.level().getEntitiesOfClass(LivingEntity.class, sweep,
                e -> e != this && !(e instanceof PlateauBoss));
        for (LivingEntity e : hits) {
            // P3 charge damage = 15 * 1.3 = ~20
            e.hurt(this.damageSources().mobAttack(this), 20.0F);
        }
    }

    private double findGroundY(BlockPos pos) {
        for (int dy = 0; dy < 32; dy++) {
            BlockPos check = pos.below(dy);
            if (!this.level().getBlockState(check).isAir()) return check.getY() + 1;
        }
        return pos.getY();
    }

    @Override
    public int getHomeRadius() {
        return 30;
    }

    @Override
    public ResourceKey<Structure> getHomeStructure() {
        return TFStructures.FINAL_CASTLE;
    }

    @Override
    public Block getDeathContainer(RandomSource random) {
        return TFBlocks.CANOPY_CHEST.get();
    }

    @Override
    public Block getBossSpawner() {
        return TFBlocks.FINAL_BOSS_BOSS_SPAWNER.get();
    }
}
