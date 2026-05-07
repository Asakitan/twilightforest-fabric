package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFSounds;

/**
 * Q31 simplified port of TF {@code ChainBlockItem}. Original spawns a flying
 * {@code ChainBlock} projectile entity that swings on a chain, breaks soft
 * blocks, and returns to the wielder. Fabric port has no ChainBlock projectile
 * registered yet, so we collapse the swing into a single instant raycast on
 * use:
 * <ul>
 *   <li>5-block ranged swing in the player's look direction.</li>
 *   <li>If an entity is in the swing, deals 8 HP and applies knockback.</li>
 *   <li>If a soft block is in the swing (dirt/grass/sand/leaves), shatters it
 *       (server-side {@code destroyBlock}).</li>
 *   <li>Plays the chain-fired sound and a spinning CRIT particle trail.</li>
 *   <li>Consumes 1 durability per swing.</li>
 * </ul>
 */
public class BlockAndChainItem extends CodexItem {

    private static final double REACH = 5.0D;

    public BlockAndChainItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 reach = eye.add(look.x * REACH, look.y * REACH, look.z * REACH);

        boolean hit = false;

        // Entity raycast first
        AABB swingBox = player.getBoundingBox().expandTowards(look.scale(REACH)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, eye, reach, swingBox,
                e -> e != player && e.isAlive() && e.isPickable());
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            DamageSource src = level.damageSources().mobAttack(player);
            target.hurt(src, 8.0F);
            target.knockback(0.6D, -look.x, -look.z);
            hit = true;
        }

        // Block raycast for soft-block break (only if no entity hit, to mirror swing)
        if (!hit) {
            BlockHitResult blockHit = level.clip(new ClipContext(eye, reach, ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE, player));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = blockHit.getBlockPos();
                BlockState state = level.getBlockState(pos);
                float hardness = state.getDestroySpeed(level, pos);
                if (hardness >= 0.0F && hardness < 1.5F) {
                    serverLevel.destroyBlock(pos, true, player);
                    hit = true;
                }
            }
        }

        // Particle trail along the swing arc
        for (int i = 1; i <= 8; i++) {
            double t = i / 8.0D;
            double x = eye.x + look.x * REACH * t;
            double y = eye.y + look.y * REACH * t;
            double z = eye.z + look.z * REACH * t;
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                TFSounds.BLOCK_AND_CHAIN_FIRED, SoundSource.PLAYERS, 0.5F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F));

        if (!player.getAbilities().instabuild && hit && player instanceof ServerPlayer sp) {
            stack.hurtAndBreak(1, serverLevel, sp, removed -> {});
        }

        player.getCooldowns().addCooldown(this, 12);
        return InteractionResultHolder.success(stack);
    }
}
