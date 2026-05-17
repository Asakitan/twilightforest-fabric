package twilightforest.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.TFPart;

/**
 * Codex multipart entity-id lookup fix (server side).
 *
 * <p>The companion to {@link ProjectileUtilMixin}. Client ray-trace now reports
 * a TFPart sub-entity as the hit target; the client then fires a
 * {@code ServerboundInteractPacket} carrying that part's entity-id. The server
 * resolves that id through {@code ServerLevel#getEntity(int)} which only knows
 * about world-tracked entities — TFParts aren't tracked, so the lookup returned
 * {@code null} and the packet was discarded, undoing the client-side hit.
 *
 * <p>We scan every multipart owner in the world for a part whose id matches the
 * requested id when the vanilla lookup misses. {@code getEntity(int)} is hot
 * (every interact / attack / arrow-hit-back goes through it) but only a few
 * multipart bosses exist in a typical world, so the scan is bounded and lazy.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "getEntity(I)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"), cancellable = true)
    private void codex_twilight$findMultipartPartById(int id, CallbackInfoReturnable<Entity> cir) {
        if (cir.getReturnValue() != null) return;
        ServerLevel self = (ServerLevel) (Object) this;
        for (Entity owner : self.getAllEntities()) {
            if (owner instanceof TFPart.Owner ownerImpl) {
                TFPart<?>[] parts = ownerImpl.getParts();
                if (parts == null) continue;
                for (TFPart<?> part : parts) {
                    if (part != null && part.getId() == id) {
                        cir.setReturnValue(part);
                        return;
                    }
                }
            }
        }
    }
}
