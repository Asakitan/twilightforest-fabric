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
}
