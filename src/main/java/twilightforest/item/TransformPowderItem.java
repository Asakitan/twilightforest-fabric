package twilightforest.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import twilightforest.init.TFTransformations;

/**
 * Q17/Q27 ported behaviour:
 * <ul>
 *   <li>Right-click on a living entity → if {@link TFTransformations#lookup(LivingEntity)}
 *       returns a target type, the entity is replaced (NBT preserved where possible),
 *       consumes 1 powder.</li>
 *   <li>Right-click in air → CRIT particle burst at the look-vector AABB.</li>
 * </ul>
 *
 * <p>Q27 promoted Q17's particle-only sentinel to a real transformation flow
 * using {@link TFTransformations} (a Java-side replacement for
 * NeoForge-only {@code TFDataMaps.TRANSFORMATION_POWDER}).</p>
 */
public class TransformPowderItem extends CodexItem {

    public TransformPowderItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!target.isAlive() || target.level().isClientSide()) {
            return InteractionResult.PASS;
        }
        EntityType<?> targetType = TFTransformations.lookup(target);
        if (targetType == null) return InteractionResult.PASS;

        if (target.level() instanceof ServerLevel serverLevel) {
            Entity replacement = targetType.create(serverLevel);
            if (replacement == null) return InteractionResult.PASS;
            replacement.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
            // Carry over health proportionally, name, and persistent NBT.
            CompoundTag nbt = new CompoundTag();
            target.saveWithoutId(nbt);
            // Strip type-specific fields so the new entity uses its defaults.
            nbt.remove("Pos");
            nbt.remove("Motion");
            nbt.remove("UUID");
            nbt.remove("id");
            try { replacement.load(nbt); } catch (Throwable ignored) {}
            if (replacement instanceof net.minecraft.world.entity.Mob mob) {
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(target.blockPosition()), MobSpawnType.CONVERSION, null);
            }
            target.discard();
            serverLevel.addFreshEntity(replacement);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    replacement.getX(), replacement.getY() + 0.5, replacement.getZ(),
                    20, 0.5, 0.5, 0.5, 0.0);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide()) {
            AABB area = this.getEffectAABB(player);
            for (int i = 0; i < 30; i++) {
                level.addParticle(ParticleTypes.CRIT,
                        area.minX + level.getRandom().nextFloat() * (area.maxX - area.minX),
                        area.minY + level.getRandom().nextFloat() * (area.maxY - area.minY),
                        area.minZ + level.getRandom().nextFloat() * (area.maxZ - area.minZ),
                        0, 0, 0);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    private AABB getEffectAABB(Player player) {
        double range = 2.0D;
        double radius = 1.0D;
        Vec3 srcVec = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vec3 lookVec = player.getLookAngle();
        Vec3 destVec = srcVec.add(lookVec.x() * range, lookVec.y() * range, lookVec.z() * range);
        return new AABB(destVec.x() - radius, destVec.y() - radius, destVec.z() - radius,
                destVec.x() + radius, destVec.y() + radius, destVec.z() + radius);
    }
}
