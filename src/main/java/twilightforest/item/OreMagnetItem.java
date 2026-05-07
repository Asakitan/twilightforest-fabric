package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFSounds;

import java.util.List;

/**
 * Q37 simplified port of TF {@code OreMagnetItem}: hold + release fires a
 * 32-block raycast cone in the look direction; up to 8 ore blocks within the
 * cone are pulled out (replaced with their post-mining state) and converted to
 * floating item entities pulled toward the player. Each ore pulled costs 1
 * durability.
 *
 * <p>Skipped from TF original: the line-of-sight Voxel Bresenham iterator
 * (replaced with a simple stepped raycast), per-ore-type replacement maps
 * (we use {@link Block#getCloneItemStack} for the dropped item and replace the
 * mined block with stone), the wiggle-cone retry-with-offset pass.</p>
 */
public class OreMagnetItem extends CodexItem {

    private static final double RANGE = 32.0D;
    private static final int MAX_PULLS = 8;
    private static final int FIRING_TIME = 10;

    public OreMagnetItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int useRemaining) {
        int useTime = this.getUseDuration(stack, living) - useRemaining;
        if (level.isClientSide() || useTime < FIRING_TIME) return;
        if (!(living instanceof Player player)) return;
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        int pulls = 0;

        // Step every 1 block along the raycast; check a 1-block radius cylinder around each step.
        for (int step = 1; step <= RANGE && pulls < MAX_PULLS; step++) {
            Vec3 here = eye.add(look.scale(step));
            BlockPos centre = BlockPos.containing(here);
            for (int dx = -1; dx <= 1 && pulls < MAX_PULLS; dx++) {
                for (int dy = -1; dy <= 1 && pulls < MAX_PULLS; dy++) {
                    for (int dz = -1; dz <= 1 && pulls < MAX_PULLS; dz++) {
                        BlockPos at = centre.offset(dx, dy, dz);
                        BlockState state = serverLevel.getBlockState(at);
                        if (!state.is(BlockTags.IRON_ORES) && !state.is(BlockTags.GOLD_ORES)
                                && !state.is(BlockTags.DIAMOND_ORES) && !state.is(BlockTags.EMERALD_ORES)
                                && !state.is(BlockTags.LAPIS_ORES) && !state.is(BlockTags.REDSTONE_ORES)
                                && !state.is(BlockTags.COAL_ORES) && !state.is(BlockTags.COPPER_ORES)) {
                            continue;
                        }
                        ItemStack drop = new ItemStack(state.getBlock().asItem());
                        if (drop.isEmpty()) drop = state.getBlock().getCloneItemStack(serverLevel, at, state);
                        if (drop.isEmpty()) continue;

                        // Mine to deepslate or stone backfill depending on neighbour
                        BlockState fill = state.toString().contains("deepslate")
                                ? net.minecraft.world.level.block.Blocks.DEEPSLATE.defaultBlockState()
                                : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
                        serverLevel.destroyBlock(at, false);
                        serverLevel.setBlockAndUpdate(at, fill);

                        ItemEntity entity = new ItemEntity(serverLevel,
                                at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D,
                                drop);
                        Vec3 pull = player.position().add(0, 1, 0).subtract(at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D).normalize().scale(0.6D);
                        entity.setDeltaMovement(pull);
                        entity.setNoPickUpDelay();
                        serverLevel.addFreshEntity(entity);

                        serverLevel.sendParticles(ParticleTypes.PORTAL,
                                at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D,
                                12, 0.3, 0.3, 0.3, 0.05);
                        pulls++;
                    }
                }
            }
        }

        if (pulls > 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    TFSounds.ORE_MAGNET_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (player instanceof ServerPlayer sp && !player.getAbilities().instabuild) {
                stack.hurtAndBreak(pulls, serverLevel, sp, removed -> {});
            }
        }
        player.getCooldowns().addCooldown(this, 20);
    }
}
