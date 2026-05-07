package twilightforest.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ThrowRiderGoal extends MeleeAttackGoal {
    private int throwTimer;
    private int timeout;
    private int pickupCooldown;

    public ThrowRiderGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return this.mob.getPassengers().isEmpty()
                && target != null
                && super.canUse();
    }

    @Override
    public void start() {
        this.throwTimer = 10 + this.mob.getRandom().nextInt(30);
        this.timeout = 80 + this.mob.getRandom().nextInt(40);
        this.pickupCooldown = 0;
        super.start();
    }

    @Override
    public void tick() {
        --this.timeout;
        if (!this.mob.getPassengers().isEmpty()) {
            --this.throwTimer;
            Entity passenger = this.mob.getPassengers().get(0);
            this.mob.getLookControl().setLookAt(passenger, 100.0F, 100.0F);
        } else {
            super.tick();
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target) && this.getTicksUntilNextAttack() <= 0 && this.mob.getPassengers().isEmpty() && this.pickupCooldown-- <= 0) {
            this.pickupCooldown = 3;
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            target.stopRiding();
            target.startRiding(this.mob, true);
        }
    }

    @Override
    public void stop() {
        if (!this.mob.getPassengers().isEmpty()) {
            Entity passenger = this.mob.getPassengers().get(0);
            passenger.stopRiding();
            Vec3 direction = this.mob.getLookAngle();
            double x = direction.x() * 2.0D;
            double z = direction.z() * 2.0D;
            passenger.push(x, 0.9D, z);
            passenger.hasImpulse = true;
            if (passenger instanceof Player player) {
                player.hurtMarked = true;
            }
        }
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.throwTimer > 0 && !this.mob.getPassengers().isEmpty())
                || (this.timeout > 0 && this.mob.getPassengers().isEmpty() && super.canContinueToUse());
    }
}