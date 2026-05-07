package twilightforest.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.projectile.LichBolt;
import twilightforest.entity.projectile.LichBomb;
import twilightforest.init.TFAttributes;
import twilightforest.init.TFItems;

import java.util.EnumSet;
import java.util.List;

public class LichShadowsGoal extends Goal {
    private final Lich lich;
    private final float attackRange;

    public LichShadowsGoal(Lich lich, float attackRange) {
        this.lich = lich;
        this.attackRange = attackRange;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.lich.getPhase() == 1 && this.lich.tickCount > 20 && this.lich.getTarget() != null;
    }

    @Override
    public void start() {
        this.lich.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFItems.TWILIGHT_SCEPTER.get()));
    }

    @Override
    public void stop() {
        this.lich.despawnClones();
    }

    @Override
    public void tick() {
        if (!this.lich.isShadowClone()) {
            this.lich.getAllClones().forEach(clone -> {
                clone.setAttackCooldown(this.lich.getAttackCooldown());
                clone.setTeleportInvisibility(this.lich.getTeleportInvisibility());
            });
        }
        if (this.lich.getTeleportInvisibility() > 0) {
            return;
        }
        if (this.lich.isShadowClone()) {
            this.checkForMaster();
            return;
        }
        LivingEntity target = this.lich.getTarget();
        if (this.lich.getAttackCooldown() == 60) {
            if (!this.lich.teleportToNewTarget(target, this.attackRange, this)) {
                this.lich.teleportHome();
            }
        } else if (target != null && this.lich.getAttackCooldown() == 0 && this.lich.distanceTo(target) < this.attackRange) {
            this.attack(this.lich);
            for (Lich clone : this.lich.getAllClones()) {
                clone.setTarget(target);
                this.attack(clone);
            }
        }
    }

    private void attack(Lich lich) {
        lich.launchProjectileAt(lich.getNextAttackType() == 0 ? new LichBolt(lich.level(), lich) : new LichBomb(lich.level(), lich));
        lich.swing(InteractionHand.MAIN_HAND);
        lich.setNextAttackType(lich.getRandom().nextInt(3) > 0 ? 0 : 1);
        lich.setAttackCooldown(100);
    }

    private void checkForMaster() {
        if (this.lich.getMaster() == null) {
            this.findNewMaster();
        }
        if (this.lich.getMaster() == null || !this.lich.getMaster().isAlive() || this.lich.getMaster().getPhase() != 1) {
            this.lich.discard();
        }
    }

    public void checkAndSpawnClones(LivingEntity target) {
        if (this.lich.countMyClones() < this.lich.getAttributeValue(TFAttributes.CLONE_COUNT)) {
            this.spawnShadowClone(target);
        }
    }

    private void spawnShadowClone(LivingEntity target) {
        Vec3 cloneSpot = this.lich.findVecInLOSOf(target);
        if (cloneSpot != null) {
            Lich clone = new Lich(this.lich.level(), this.lich);
            clone.moveTo(cloneSpot.x(), cloneSpot.y(), cloneSpot.z(), this.lich.getYRot(), 0.0F);
            this.lich.level().addFreshEntity(clone);
            clone.setTarget(target);
            clone.setAttackCooldown(60 + this.lich.getRandom().nextInt(3) - this.lich.getRandom().nextInt(3));
            clone.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFItems.TWILIGHT_SCEPTER.get()));
            clone.setTeleportInvisibility(this.lich.getTeleportInvisibility());
            this.lich.addClone(clone.getUUID());
        }
    }

    private void findNewMaster() {
        for (Lich nearbyLich : this.getNearbyLiches()) {
            if (!nearbyLich.isShadowClone() && nearbyLich.wantsNewClone(this.lich)) {
                this.lich.setMasterUUID(nearbyLich.getUUID());
                nearbyLich.addClone(this.lich.getUUID());
                this.lich.setTarget(nearbyLich.getTarget());
                break;
            }
        }
    }

    private List<Lich> getNearbyLiches() {
        return this.lich.level().getEntitiesOfClass(Lich.class, new AABB(this.lich.blockPosition()).inflate(32.0D, 16.0D, 32.0D));
    }
}