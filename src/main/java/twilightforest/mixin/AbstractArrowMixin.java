package twilightforest.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.List;

/**
 * Q34 mixin for {@link AbstractArrow}: implements the Travellers Vest /
 * Goggles "arrow magnetism" modifier. Arrows fired by an entity wearing the
 * appropriate piece subtly home onto the nearest valid living target within
 * a 12-block sphere, with a 5% velocity blend per tick toward the target.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void codex_twilight$arrowMagnetism(CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.level().isClientSide()) return;
        if (self.inGround) return;
        if (!(self.getOwner() instanceof LivingEntity owner)) return;

        // Agile Ranger (gloves) — once on spawn, tighten the firing spread by
        // re-normalising the arrow to a clean direction with no jitter (only if
        // the owner was moving when shooting — moving accuracy is the modifier's
        // signature). Implemented via single-tick scrub on first tick.
        if (self.tickCount == 1 && TravellersModifiersManager.isAgileRangerActive(owner)
                && owner.getDeltaMovement().horizontalDistance() > 0.05D) {
            Vec3 m = self.getDeltaMovement();
            double speed = m.length();
            if (speed > 0.1D) {
                Vec3 lookDir = owner.getLookAngle();
                self.setDeltaMovement(lookDir.scale(speed));
            }
        }

        if (!TravellersModifiersManager.isArrowMagnetismActive(owner)) return;
        // Throttle the AABB scan to every other tick — arrows fly fast enough that
        // half-resolution homing is indistinguishable but halves CPU cost.
        if ((self.tickCount & 1) != 0) return;

        Vec3 here = self.position();
        AABB scan = new AABB(here, here).inflate(12.0D);
        List<LivingEntity> candidates = self.level().getEntitiesOfClass(LivingEntity.class, scan,
                e -> e != owner && e.isAlive() && !e.isInvisible() && e.attackable());
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity c : candidates) {
            double d = c.position().distanceToSqr(here);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        if (best == null) return;

        Vec3 toTarget = best.getEyePosition().subtract(here).normalize();
        Vec3 currentMotion = self.getDeltaMovement();
        double speed = currentMotion.length();
        if (speed < 0.1D) return;
        Vec3 currentDir = currentMotion.normalize();
        Vec3 blended = currentDir.scale(0.95D).add(toTarget.scale(0.05D)).normalize().scale(speed);
        self.setDeltaMovement(blended);
    }
}
