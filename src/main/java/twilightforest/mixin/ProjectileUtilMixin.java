package twilightforest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.entity.TFPart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Codex multipart ray-trace fix.
 *
 * <p>NeoForge upstream provides multipart hit detection via {@code Entity.getParts()}
 * + a NeoForge patch on {@code ProjectileUtil} / vanilla ray-trace that iterates an
 * owner's sub-entities. Fabric has no equivalent, so without this mixin a player's
 * crosshair / projectile / arrow ray-trace never touches a TFPart (HydraHead,
 * HydraNeck, NagaSegment, …) — it always resolves to the multipart owner's main
 * bounding box. Effects observed before this fix:
 *   • Hydra appears invulnerable because hits land on the Hydra main entity, which
 *     rejects all non-BYPASSES_INVULNERABILITY damage in Hydra#hurt.
 *   • Naga segments past the head are not attackable — only the head's bbox is
 *     hit-tested.
 *   • Arrows fly through visible Hydra heads / Naga body without registering hits.
 *
 * <p>Strategy: redirect the inner {@code level.getEntities(shooter, box, filter)}
 * call inside each public {@code getEntityHitResult} overload so the returned list
 * also contains every TFPart belonging to each owner returned by vanilla. Vanilla
 * 1.21.1 ships two independent overloads:
 *   • (Entity, Vec3, Vec3, AABB, Predicate, double) — used by {@code Entity#pick}
 *     and {@code GameRenderer#pick}, i.e. the crosshair pick + melee attack picking
 *     on the client. <b>Required for melee body-segment hits.</b>
 *   • (Level, Entity, Vec3, Vec3, AABB, Predicate, float) — used by server-side
 *     projectile + arrow ray-trace validation.
 * Both must be covered or melee attacks on Hydra heads / Naga segments will not
 * resolve to a TFPart and damage will be discarded.
 */
@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {

    @Redirect(method = "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private static List<Entity> codex_twilight$includeMultipartPartsWithLevel(Level level, Entity shooter, AABB box, Predicate<Entity> filter) {
        return codex_twilight$augmentEntitiesWithParts(level, shooter, box, filter);
    }

    /**
     * Melee / crosshair path. {@code Entity#pick} → {@code GameRenderer#pick} call
     * this overload (no leading {@code Level} parameter, takes {@code double}
     * distance instead of {@code float}). Without this redirect, hovering the
     * crosshair over a Hydra head or Naga body segment resolves to the multipart
     * owner's main bounding box (which is invulnerable / off-screen), so the
     * resulting {@code ServerboundInteractPacket} never targets a part.
     */
    @Redirect(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private static List<Entity> codex_twilight$includeMultipartPartsNoLevel(Level level, Entity shooter, AABB box, Predicate<Entity> filter) {
        return codex_twilight$augmentEntitiesWithParts(level, shooter, box, filter);
    }

    private static List<Entity> codex_twilight$augmentEntitiesWithParts(Level level, Entity shooter, AABB box, Predicate<Entity> filter) {
        List<Entity> original = level.getEntities(shooter, box, filter);
        List<Entity> augmented = null;
        for (Entity ent : original) {
            if (ent instanceof TFPart.Owner owner) {
                TFPart<?>[] parts = owner.getParts();
                if (parts == null) continue;
                for (TFPart<?> part : parts) {
                    if (part == null || !part.isAlive() || !part.isPickable()) continue;
                    if (filter != null && !filter.test(part)) continue;
                    if (!part.getBoundingBox().intersects(box)) continue;
                    if (augmented == null) augmented = new ArrayList<>(original);
                    augmented.add(part);
                }
            }
        }
        return augmented != null ? augmented : original;
    }
}
