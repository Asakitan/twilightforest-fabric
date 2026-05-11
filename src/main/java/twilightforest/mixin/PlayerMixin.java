package twilightforest.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.init.custom.TravellersModifiersManager;

/**
 * Q34 mixin for {@link Player}: keeps the Travellers Wings crouch high-jump
 * impulse. Mid-air double jump is handled by the official traveller packet path
 * in {@code TravellersClientEvents -> PerformDoubleJumpPacket -> TravellersGearLogic}.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "jumpFromGround", at = @At("RETURN"))
    private void codex_twilight$travellersHighJump(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) return;
        if (!TravellersModifiersManager.isHighJumpActive(self)) return;
        if (!self.isCrouching()) return;
        net.minecraft.world.phys.Vec3 m = self.getDeltaMovement();
        // Boost vertical impulse by 0.25 (≈+50% jump height when sneak-jumping).
        self.setDeltaMovement(m.x, m.y + 0.25D, m.z);
        self.hasImpulse = true;
    }
}
