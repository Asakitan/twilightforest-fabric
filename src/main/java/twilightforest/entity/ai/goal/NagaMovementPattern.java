package twilightforest.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import twilightforest.entity.boss.Naga;
import twilightforest.init.TFSounds;
import twilightforest.util.entities.EntityUtil;

import java.util.EnumSet;

public class NagaMovementPattern extends Goal {
    private final Naga naga;
    private MovementState state;
    private int stateCounter;
    private boolean clockwise;
    private boolean stunCalculated;

    public NagaMovementPattern(Naga naga) {
        this.naga = naga;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.stop();
    }

    @Override
    public boolean canUse() {
        return this.naga.getTarget() != null && this.naga.areSelfAndTargetInHome(this.naga.getTarget());
    }

    @Override
    public void stop() {
        this.state = MovementState.CIRCLE;
        this.stateCounter = 15;
        this.clockwise = false;
        this.stunCalculated = false;
    }

    @Override
    public void tick() {
        if (!this.naga.getNavigation().isDone()) {
            if (this.naga.getNavigation().isStuck()) {
                this.naga.getNavigation().stop();
            }
            return;
        }
        if (this.stateCounter-- <= 0) {
            this.transitionState();
            return;
        }
        switch (this.state) {
            case INTIMIDATE -> this.tickIntimidate();
            case CRUMBLE -> {
                this.naga.getNavigation().stop();
                this.crumbleBelowTarget(2);
                this.crumbleBelowTarget(3);
            }
            case CHARGE, STUNLESS_CHARGE -> this.tickCharge();
            case CIRCLE -> this.tickCircle();
            case DAZE -> {
                this.naga.getNavigation().stop();
                this.naga.setDazed(true);
                this.naga.setCharging(false);
            }
        }
    }

    private void tickIntimidate() {
        this.naga.getNavigation().stop();
        if (this.naga.getTarget() != null) {
            this.naga.getLookControl().setLookAt(this.naga.getTarget(), 30.0F, 30.0F);
            this.naga.lookAt(this.naga.getTarget(), 30.0F, 30.0F);
        }
        this.naga.zza = 0.1F;
        if (!this.stunCalculated) {
            float healthRatio = 1.0F - (this.naga.getHealth() / this.naga.getMaxHealth()) - 0.25F;
            float chance = Mth.clamp(healthRatio + (this.naga.level().getCurrentDifficultyAt(this.naga.blockPosition()).getDifficulty().getId() * 0.05F), 0.0F, 0.5F);
            this.naga.setStunlessCharging(this.naga.getRandom().nextFloat() * 0.75F < chance);
            this.stunCalculated = true;
        }
    }

    private void tickCharge() {
        if (this.naga.getTarget() != null) {
            BlockPos point = this.findCirclePoint(this.clockwise, 5.0D, Math.PI);
            this.naga.getNavigation().moveTo(point.getX(), point.getY(), point.getZ(), 1.5D);
        }
        this.naga.setCharging(true);
    }

    private void tickCircle() {
        this.naga.setCharging(false);
        this.naga.setDazed(false);
        double radius = this.stateCounter % 2 == 0 ? 12.0D : 14.0D;
        double rotation = 1.0D;
        if (this.stateCounter == 2) {
            radius = 16.0D;
        }
        if (this.stateCounter == 1) {
            rotation = 0.1D;
        }
        if (this.naga.getTarget() != null) {
            BlockPos point = this.findCirclePoint(this.clockwise, radius, rotation);
            this.naga.getNavigation().moveTo(point.getX(), point.getY(), point.getZ(), 1.0D);
        }
    }

    private void transitionState() {
        switch (this.state) {
            case INTIMIDATE -> {
                this.clockwise = !this.clockwise;
                if (this.naga.getTarget() != null && this.naga.getTarget().getBoundingBox().minY > this.naga.getBoundingBox().maxY) {
                    this.doCrumblePlayer();
                } else {
                    this.doCharge(this.naga.isStunlessCharging());
                }
            }
            case CRUMBLE -> this.doCharge(this.naga.isStunlessCharging());
            case CHARGE, STUNLESS_CHARGE, DAZE -> this.doCircle();
            case CIRCLE -> this.doIntimidate();
        }
    }

    public void doDaze() {
        this.state = MovementState.DAZE;
        this.naga.getNavigation().stop();
        this.stateCounter = 60 + this.naga.getRandom().nextInt(40);
    }

    public void doCircle() {
        this.state = MovementState.CIRCLE;
        this.stateCounter += 10 + this.naga.getRandom().nextInt(10);
        this.stunCalculated = false;
    }

    public void forceCircle() {
        this.state = MovementState.CIRCLE;
        this.stateCounter = 10 + this.naga.getRandom().nextInt(10);
        this.stunCalculated = false;
    }

    public void doCrumblePlayer() {
        this.state = MovementState.CRUMBLE;
        this.naga.getNavigation().stop();
        this.stateCounter = 20 + this.naga.getRandom().nextInt(20);
    }

    private void doCharge(boolean stunless) {
        this.state = stunless ? MovementState.STUNLESS_CHARGE : MovementState.CHARGE;
        this.stateCounter = 2;
    }

    private void doIntimidate() {
        this.state = MovementState.INTIMIDATE;
        this.naga.playSound(TFSounds.NAGA_RATTLE, 4.0F, this.naga.getVoicePitch());
        this.stateCounter += 15 + this.naga.getRandom().nextInt(10);
    }

    private void crumbleBelowTarget(int range) {
        if (!this.naga.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || this.naga.getTarget() == null) {
            return;
        }
        int floor = (int) this.naga.getBoundingBox().minY;
        int targetY = (int) this.naga.getTarget().getBoundingBox().minY;
        if (targetY > floor) {
            int dx = (int) this.naga.getTarget().getX() + this.naga.getRandom().nextInt(range) - this.naga.getRandom().nextInt(range);
            int dz = (int) this.naga.getTarget().getZ() + this.naga.getRandom().nextInt(range) - this.naga.getRandom().nextInt(range);
            int dy = targetY - this.naga.getRandom().nextInt(range) + this.naga.getRandom().nextInt(range > 1 ? range - 1 : range);
            BlockPos pos = new BlockPos(dx, Math.max(dy, targetY), dz);
            if (EntityUtil.canDestroyBlock(this.naga.level(), pos, this.naga)) {
                this.naga.level().destroyBlock(pos, true);
                for (int k = 0; k < 20; k++) {
                    this.naga.level().addParticle(ParticleTypes.CRIT,
                            this.naga.getRandomX(1.0D), this.naga.getRandomY(), this.naga.getRandomZ(1.0D),
                            this.naga.getRandom().nextGaussian() * 0.02D,
                            this.naga.getRandom().nextGaussian() * 0.02D,
                            this.naga.getRandom().nextGaussian() * 0.02D);
                }
            }
        }
    }

    private BlockPos findCirclePoint(boolean clockwise, double radius, double rotation) {
        LivingEntity target = this.naga.getTarget();
        if (target == null) {
            return this.naga.blockPosition();
        }
        double vecX = this.naga.getX() - target.getX();
        double vecZ = this.naga.getZ() - target.getZ();
        float angle = (float) Math.atan2(vecZ, vecX);
        angle += clockwise ? rotation : -rotation;
        double dx = Mth.cos(angle) * radius;
        double dz = Mth.sin(angle) * radius;
        double dy = Math.min(this.naga.getBoundingBox().minY, target.getY());
        return BlockPos.containing(target.getX() + dx, dy, target.getZ() + dz);
    }

    public MovementState getState() {
        return this.state;
    }

    public enum MovementState {
        INTIMIDATE,
        CRUMBLE,
        CHARGE,
        STUNLESS_CHARGE,
        CIRCLE,
        DAZE
    }
}