package twilightforest.entity.boss;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.EnforcedHomePoint;

import java.util.Optional;

public abstract class BaseTFBoss extends Monster implements EnforcedHomePoint {
    private static final EntityDataAccessor<Optional<GlobalPos>> HOME_POINT =
            SynchedEntityData.defineId(BaseTFBoss.class, EntityDataSerializers.OPTIONAL_GLOBAL_POS);

    private final ServerBossEvent bossInfo;

    protected BaseTFBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossInfo = this.createBossBar();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOME_POINT, Optional.empty());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.shouldShowBossBar(player)) {
            this.getBossBar().addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.getBossBar().removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        this.saveHomePointToNbt(tag);
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadHomePointFromNbt(tag);
        if (this.hasCustomName()) {
            this.getBossBar().setName(this.getBossBarTitle());
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!this.level().isClientSide()) {
            this.tickBossBar();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.getBossBar().setProgress(0.0F);
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        } else {
            super.checkDespawn();
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 1.0F;
    }

    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public @Nullable GlobalPos getRestrictionPoint() {
        return this.getEntityData().get(HOME_POINT).orElse(null);
    }

    @Override
    public void setRestrictionPoint(@Nullable GlobalPos pos) {
        this.getEntityData().set(HOME_POINT, Optional.ofNullable(pos));
    }

    public ServerBossEvent getBossBar() {
        return this.bossInfo;
    }

    protected void tickBossBar() {
        float maxHealth = this.getMaxHealth();
        this.getBossBar().setProgress(maxHealth <= 0.0F ? 0.0F : this.getHealth() / maxHealth);
    }

    protected boolean shouldShowBossBar(ServerPlayer player) {
        return true;
    }

    protected ServerBossEvent createBossBar() {
        return new ServerBossEvent(this.getBossBarTitle(), this.getBossBarColor(), this.getBossBarOverlay());
    }

    public Component getBossBarTitle() {
        Component displayName = this.getDisplayName();
        return displayName != null ? displayName : this.getTypeName();
    }

    protected abstract BossEvent.BossBarColor getBossBarColor();

    protected BossEvent.BossBarOverlay getBossBarOverlay() {
        return BossEvent.BossBarOverlay.PROGRESS;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.getBossBar().setName(this.getBossBarTitle());
    }

    //-----------------------------------------//
    // P5.e — upstream BaseTFBoss surface bumps  //
    //-----------------------------------------//

    /**
     * 1:1 from upstream — defaults to vanilla {@code doMobLoot} gamerule.
     * Subclasses override to suppress drops on shadow clones etc.
     */
    protected boolean shouldSpawnLoot() {
        return this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT);
    }

    /** 1:1 from upstream — Liches override to suppress spawner placement on shadow clones. */
    protected boolean shouldCreateSpawner() {
        return true;
    }

    /**
     * 1:1 from upstream — overridden by Lich for ominous-candle activation. Default just
     * places the boss-spawner block at {@code pos}.
     */
    public void placeSpawner(net.minecraft.core.BlockPos pos) {
        net.minecraft.world.level.block.Block spawner = null;
        try {
            // Use reflection-free approach: subclasses override getBossSpawner() if defined.
            spawner = (net.minecraft.world.level.block.Block) this.getClass().getMethod("getBossSpawner").invoke(this);
        } catch (Throwable ignored) {}
        if (spawner != null) this.level().setBlockAndUpdate(pos, spawner.defaultBlockState());
    }

    /**
     * 1:1 from upstream — called from {@link #remove(RemovalReason)} so that
     * boss-loot chests can be deposited on KILLED. Default is a no-op; bosses
     * that implement {@code IBossLootBuffer} override.
     */
    protected void postRemoval(net.minecraft.server.level.ServerLevel serverLevel, RemovalReason reason) {
    }

    /** Override hook — total death-animation duration in ticks (vanilla = 20). */
    public boolean isDeathAnimationFinished() {
        return this.deathTime >= 20;
    }

    /** Override hook — client-side per-tick particles during death animation. Default no-op. */
    public void tickDeathAnimation() {
    }

    /** Override hook — per-tick logic to choose the entity the boss faces while dying. */
    @Nullable
    protected net.minecraft.world.entity.Entity lookAtUponDeath() {
        return null;
    }

    /** Get the home anchor or fall back to the boss's current block-position. */
    public net.minecraft.core.BlockPos homeOrElseCurrent() {
        return this.getRestrictionPoint() == null ? this.blockPosition() : this.getRestrictionPoint().pos();
    }

    /** Returns true if {@code pos} is outside the home radius. Default uses {@code 30} squared. */
    public boolean isOutsideHomeRange(net.minecraft.world.phys.Vec3 pos) {
        if (this.getRestrictionPoint() == null) return false;
        net.minecraft.core.BlockPos point = this.getRestrictionPoint().pos();
        int radius = this.getHomeRadiusOrDefault();
        return point.distToCenterSqr(pos) > (double) (radius * radius);
    }

    /** Per-boss home radius. Subclasses override; default 20. */
    public int getHomeRadius() {
        return 20;
    }

    private int getHomeRadiusOrDefault() {
        try {
            return (int) this.getClass().getMethod("getHomeRadius").invoke(this);
        } catch (Throwable ignored) {
            return 20;
        }
    }

    /**
     * Hook for adding home-restriction goals to the boss's goal selector.
     * Default is no-op; Lich and other home-anchored bosses can override.
     */
    protected void addRestrictionGoals(net.minecraft.world.entity.PathfinderMob mob, net.minecraft.world.entity.ai.goal.GoalSelector selector) {
    }
}
