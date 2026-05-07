package twilightforest.entity.boss;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFItemVisuals;
import twilightforest.init.TFSounds;

import java.util.List;

public class KnightPhantom extends BaseTFBoss {
    private static final net.minecraft.resources.ResourceLocation CHARGING_ID = TwilightForestMod.prefix("knight_phantom_charging_attack");
    private static final net.minecraft.resources.ResourceLocation ARMOR_ID = TwilightForestMod.prefix("knight_phantom_inactive_armor");
    private static final AttributeModifier CHARGING_MODIFIER = new AttributeModifier(CHARGING_ID, 7.0D, AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier NON_CHARGING_ARMOR_MODIFIER = new AttributeModifier(ARMOR_ID, 4.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private int attackCooldown;
    private int chargeTicks;
    private boolean chargingAtPlayer;
    private int number;

    public KnightPhantom(EntityType<? extends KnightPhantom> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.xpReward = 93;
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FLYING_SPEED, 0.24D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.WHITE;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.updatePhantomAttack();
        } else if (this.isChargingAtPlayer()) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.IRON_SWORD)), this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 0.0D, -0.1D, 0.0D);
            this.level().addParticle(ParticleTypes.SMOKE, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 0.0D, 0.1D, 0.0D);
        }
    }

    private void updatePhantomAttack() {
        if (this.attackCooldown > 0) {
            --this.attackCooldown;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.setChargingAtPlayer(false);
            return;
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.chargeTicks > 0) {
            --this.chargeTicks;
            this.setChargingAtPlayer(true);
            this.moveControl.setWantedPosition(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 1.4D);
            if (this.distanceToSqr(target) < 6.25D) {
                this.doHurtTarget(target);
                this.chargeTicks = 0;
                this.attackCooldown = 80;
            }
            return;
        }
        this.setChargingAtPlayer(false);
        if (this.attackCooldown <= 0 && target.distanceToSqr(this) < 48.0D * 48.0D && this.getSensing().hasLineOfSight(target)) {
            this.chargeTicks = 50;
            this.playSound(SoundEvents.VEX_CHARGE, 2.0F, this.getVoicePitch());
        } else {
            double angle = (this.tickCount + this.getNumber() * 60) * 0.08D;
            this.moveControl.setWantedPosition(target.getX() + Math.cos(angle) * 6.0D, target.getY() + 4.0D, target.getZ() + Math.sin(angle) * 6.0D, 0.7D);
        }
    }

    public boolean isChargingAtPlayer() {
        return this.chargingAtPlayer;
    }

    public boolean hasYetToDisappear() {
        return !this.isRemoved();
    }

    private void setChargingAtPlayer(boolean charging) {
        this.chargingAtPlayer = charging;
        if (this.level().isClientSide() || this.getAttribute(Attributes.ATTACK_DAMAGE) == null || this.getAttribute(Attributes.ARMOR) == null) {
            return;
        }
        if (charging) {
            if (!this.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(CHARGING_ID)) {
                this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(CHARGING_MODIFIER);
            }
            this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_ID);
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(CHARGING_ID);
            if (!this.getAttribute(Attributes.ARMOR).hasModifier(ARMOR_ID)) {
                this.getAttribute(Attributes.ARMOR).addTransientModifier(NON_CHARGING_ARMOR_MODIFIER);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = entity.hurt(TFDamageTypes.entitySource(this.level(), TFDamageTypes.HAUNT, this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            entity.push(this.getLookAngle().x() * 0.8D, 0.3D, this.getLookAngle().z() * 0.8D);
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void knockback(double strength, double xRatio, double zRatio) {
        this.hasImpulse = true;
        float distance = 0.2F;
        float scale = net.minecraft.util.Mth.sqrt((float) (xRatio * xRatio + zRatio * zRatio));
        this.setDeltaMovement(new Vec3(this.getDeltaMovement().x() - xRatio / scale * distance, Math.min(this.getDeltaMovement().y() + distance, 0.4D), this.getDeltaMovement().z() - zRatio / scale * distance));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFSounds.KNIGHT_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFSounds.KNIGHT_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFSounds.KNIGHT_PHANTOM_DEATH;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
        this.setItemSlot(EquipmentSlot.MAINHAND, switch (number % 3) {
            case 1 -> TFItemVisuals.withModel(new ItemStack(Items.IRON_AXE), TFItemVisuals.DIAMOND_MINOTAUR_AXE);
            case 2 -> TFItemVisuals.withModel(new ItemStack(Items.IRON_PICKAXE), TFItemVisuals.STEELEAF_PICKAXE);
            default -> TFItemVisuals.withModel(new ItemStack(Items.IRON_SWORD), TFItemVisuals.KNIGHTMETAL_SWORD);
        });
        this.setItemSlot(EquipmentSlot.CHEST, TFItemVisuals.withModel(new ItemStack(Items.IRON_CHESTPLATE), TFItemVisuals.IRONWOOD_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.HEAD, TFItemVisuals.withModel(new ItemStack(Items.IRON_HELMET), TFItemVisuals.IRONWOOD_HELMET));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MyNumber", this.getNumber());
        tag.putInt("AttackCooldown", this.attackCooldown);
        tag.putInt("ChargeTicks", this.chargeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setNumber(tag.getInt("MyNumber"));
        this.attackCooldown = tag.getInt("AttackCooldown");
        this.chargeTicks = tag.getInt("ChargeTicks");
    }
}
