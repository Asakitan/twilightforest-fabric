package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFSounds;

import java.util.List;

/**
 * Q23 ported behaviour: right-click drains 4 HP from the closest living target
 * within 16 blocks of player's gaze (raycast-based), heals the player by 2 HP,
 * applies WITHER particles, and consumes 1 durability. Simplified from TF
 * original which used a TwilightWandBolt projectile.
 */
public class LifedrainScepterItem extends CodexItem {

    public LifedrainScepterItem(Properties properties, Item fallback, int cmd) {
        super(properties, fallback, cmd);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(stack);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                TFSounds.LIFEDRAIN_SCEPTER_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            LivingEntity target = findTargetInLineOfSight(serverLevel, player, 16.0);
            if (target != null) {
                target.hurt(serverLevel.damageSources().magic(), 4.0F);
                player.heal(2.0F);
                serverLevel.sendParticles(ParticleTypes.SOUL,
                        target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                        20, 0.5, 0.5, 0.5, 0.0);
                if (!player.getAbilities().instabuild && player instanceof ServerPlayer sp) {
                    stack.hurtAndBreak(1, serverLevel, sp, removed -> {});
                }
            }
        }
        return InteractionResultHolder.success(stack);
    }

    private static LivingEntity findTargetInLineOfSight(ServerLevel level, Player player, double range) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        HitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDist = blockHit.getType() == HitResult.Type.MISS ? range * range : eye.distanceToSqr(blockHit.getLocation());

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
        LivingEntity best = null;
        double bestDist = maxDist;
        for (LivingEntity candidate : candidates) {
            AABB cBox = candidate.getBoundingBox().inflate(0.3);
            var clip = cBox.clip(eye, end);
            if (clip.isPresent()) {
                double d = eye.distanceToSqr(clip.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = candidate;
                }
            }
        }
        return best;
    }

    @SuppressWarnings("unused")
    private static EntityHitResult forceUseEntityHitResult() { return null; }
}
