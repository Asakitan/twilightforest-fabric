package twilightforest.entity.boss;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.Nullable;
import twilightforest.config.TFConfig;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.loot.TFLootTables;
import twilightforest.util.entities.EntityUtil;
import twilightforest.util.landmarks.LandmarkUtil;

import java.util.Optional;

public abstract class BaseTFBoss extends Monster implements IBossLootBuffer, EnforcedHomePoint {
    private static final EntityDataAccessor<Optional<GlobalPos>> HOME_POINT =
            SynchedEntityData.defineId(BaseTFBoss.class, EntityDataSerializers.OPTIONAL_GLOBAL_POS);

    private final ServerBossEvent bossInfo;
    private final NonNullList<ItemStack> dyingInventory = NonNullList.withSize(IBossLootBuffer.CONTAINER_SIZE, ItemStack.EMPTY);

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
        this.addDeathItemsSaveData(tag, this.registryAccess());
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readDeathItemsSaveData(tag, this.registryAccess());
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
            if (this instanceof twilightforest.entity.TFPart.Owner) {
                twilightforest.util.multiparts.MultipartEntityUtil.sendDirtyMultipartEntityData(this);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.getBossBar().setProgress(0.0F);
        if (this.shouldSpawnLoot() && this.level() instanceof ServerLevel server) {
            try {
                this.postmortem(server, source);
            } catch (Throwable t) {
                // Defensive: if postmortem (loot roll into dyingInventory + structure-conquered
                // marking) throws, the death animation must still complete and remove() must still
                // fire so the boss doesn't sit frozen mid-death with health=0.
                twilightforest.TwilightForestMod.LOGGER.error("BaseTFBoss.postmortem threw for {} at {}: {}",
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()),
                    this.blockPosition(), t.toString(), t);
            }
            // Codex addition: record the boss's spawner position for daily respawn. This is
            // strictly additive — only places the boss spawner block back after the configured
            // day delay, does NOT touch the structure conquered flag, does NOT re-fire any
            // first-kill criteria. Subsequent kills go through this same path; structure
            // conquered=true is now idempotent on LandmarkUtil.markStructureConquered, and
            // CriteriaTriggers are vanilla idempotent on already-granted advancements.
            try {
                this.codex$recordKillForDailyRespawn(server);
            } catch (Throwable t) {
                twilightforest.TwilightForestMod.LOGGER.warn("Daily boss respawn record failed for {}: {}",
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()), t.toString());
            }
        }
    }

    /**
     * Stash this boss's spawner position into the per-level {@code DailyBossRespawnState}
     * so the spawner block can be re-placed after {@code TFConfig.dailyBossRespawnDelayDays}
     * in-world days. The boss's restriction point is set during initial spawn by
     * {@code BossSpawnerBlockEntity.initializeCreature} to the spawner's block position;
     * we use that here as the canonical respawn position.
     */
    private void codex$recordKillForDailyRespawn(ServerLevel server) {
        if (!twilightforest.config.TFConfig.dailyBossRespawn) return;
        Block spawnerBlock = this.getBossSpawner();
        if (spawnerBlock == null) return;
        net.minecraft.core.GlobalPos home = this.getRestrictionPoint();
        if (home == null) return;
        if (home.dimension() != server.dimension()) return;  // boss moved cross-dim; skip
        twilightforest.util.boss.DailyBossRespawnState.get(server)
            .recordKill(spawnerBlock, home.pos(), server);
    }

    @Override
    public void lavaHurt() {
        if (!this.fireImmune()) {
            this.igniteForSeconds(5.0F);
            if (this.hurt(this.damageSources().lava(), 4.0F)) {
                this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.getRandom().nextFloat() * 0.4F);
                EntityUtil.killLavaAround(this);
            }
        }
    }

    protected void postmortem(ServerLevel serverLevel, DamageSource source) {
        IBossLootBuffer.saveDropsIntoBoss(this, TFLootTables.createLootParams(this, true, source).create(LootContextParamSets.ENTITY), serverLevel);
        java.util.Optional.ofNullable(this.getHomeStructure()).ifPresent(structure -> LandmarkUtil.markStructureConquered(serverLevel, this, structure, true));
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel) {
            try {
                this.postRemoval(serverLevel, reason);
            } catch (Throwable t) {
                // Defensive: if postRemoval (chest deposit) throws, dump every dyingInventory
                // item on the ground at the boss's current position so players still get their
                // loot. super.remove() must still fire so the entity is actually removed and
                // tickDeath stops looping forever at deathTime=200.
                twilightforest.TwilightForestMod.LOGGER.error("BaseTFBoss.postRemoval threw for {} at {}: {}",
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()),
                    this.blockPosition(), t.toString(), t);
                this.codex$dropLootOnGroundFallback(serverLevel);
            }
        }
        super.remove(reason);
    }

    /**
     * Last-resort: drop every non-empty dyingInventory item as a vanilla ItemEntity at the
     * boss's current position. Called from the {@link #remove(RemovalReason)} catch branch when
     * the chest-deposit chain throws, so the player still gets the loot.
     */
    private void codex$dropLootOnGroundFallback(ServerLevel serverLevel) {
        net.minecraft.core.BlockPos pos = this.blockPosition();
        for (int i = 0; i < IBossLootBuffer.CONTAINER_SIZE; i++) {
            ItemStack stack = this.getItem(i);
            if (!stack.isEmpty()) {
                try {
                    Block.popResource(serverLevel, pos, stack);
                } catch (Throwable inner) {
                    // Per-item failure must not block the rest of the inventory.
                    twilightforest.TwilightForestMod.LOGGER.warn("Failed to popResource boss loot slot {}: {}", i, inner.toString());
                }
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            if (this.shouldCreateSpawner() && this.isRestrictionPointValid(this.level().dimension()) && this.getRestrictionPoint() != null && this.level().isLoaded(this.getRestrictionPoint().pos())) {
                this.placeSpawner(this.getRestrictionPoint().pos());
            }
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
    protected boolean shouldDropLoot() {
        return !TFConfig.bossDropChests;
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

    protected boolean shouldSpawnLoot() {
        return this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT);
    }

    protected boolean shouldCreateSpawner() {
        return true;
    }

    public void placeSpawner(BlockPos pos) {
        Block spawner = this.getBossSpawner();
        if (spawner != null) this.level().setBlockAndUpdate(pos, spawner.defaultBlockState());
    }

    protected void postRemoval(ServerLevel serverLevel, RemovalReason reason) {
        Block deathContainer = this.getDeathContainer(this.getRandom());
        if (reason.equals(RemovalReason.KILLED) && deathContainer != null && this.shouldSpawnLoot()) {
            IBossLootBuffer.depositDropsIntoChest(this, deathContainer.defaultBlockState().setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(this.level().getRandom())), EntityUtil.bossChestLocation(this), serverLevel);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.isRemoved()) {
            if (!this.level().isClientSide()) {
                if (this.isDeathAnimationFinished()) {
                    this.level().broadcastEntityEvent(this, (byte) 60);
                    this.remove(RemovalReason.KILLED);
                } else {
                    this.tickBossBar();
                }
            } else {
                this.tickDeathAnimation();
            }
        }
    }

    public boolean isDeathAnimationFinished() {
        return this.deathTime >= 20;
    }

    public void tickDeathAnimation() {
    }

    @Nullable
    protected net.minecraft.world.entity.Entity lookAtUponDeath() {
        return null;
    }

    public net.minecraft.core.BlockPos homeOrElseCurrent() {
        return this.getRestrictionPoint() == null ? this.blockPosition() : this.getRestrictionPoint().pos();
    }

    public boolean isOutsideHomeRange(net.minecraft.world.phys.Vec3 pos) {
        if (this.getRestrictionPoint() == null) return false;
        net.minecraft.core.BlockPos point = this.getRestrictionPoint().pos();
        int radius = this.getHomeRadius();
        return point.distToCenterSqr(pos) > (double) (radius * radius);
    }

    public int getHomeRadius() {
        return 20;
    }

    protected void addRestrictionGoals(net.minecraft.world.entity.PathfinderMob mob, net.minecraft.world.entity.ai.goal.GoalSelector selector) {
    }

    @Nullable
    public net.minecraft.resources.ResourceKey<Structure> getHomeStructure() {
        return null;
    }

    @Nullable
    public Block getDeathContainer(RandomSource random) {
        return null;
    }

    @Nullable
    public Block getBossSpawner() {
        return null;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.dyingInventory;
    }
}
