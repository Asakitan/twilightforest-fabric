package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.init.TFSounds;

/**
 * Q22 ported behaviour: right-click crumbles cobblestone/stone-brick blocks in a
 * 3x3x3 cube around the player into their cobble/cracked variants. Consumes 1
 * durability per use. Simplified from TF original which had cone-shape and more
 * recipe entries — Q22 keeps the most common conversions.
 */
public class CrumbleHornItem extends CodexItem {

    public CrumbleHornItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos center = player.blockPosition();
        int crumbled = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    BlockState replacement = crumbleConversion(state);
                    if (replacement != null) {
                        level.setBlock(pos, replacement, Block.UPDATE_ALL);
                        crumbled++;
                    }
                }
            }
        }
        if (crumbled > 0) {
            level.playSound(null, center, TFSounds.CRUMBLE_HORN_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild && player instanceof ServerPlayer sp) {
                stack.hurtAndBreak(crumbled, serverLevel, sp, removed -> {});
            }
        }
        return InteractionResultHolder.success(stack);
    }

    private static BlockState crumbleConversion(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE) return Blocks.COBBLESTONE.defaultBlockState();
        if (block == Blocks.STONE_BRICKS) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (block == Blocks.DEEPSLATE_BRICKS) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        if (block == Blocks.NETHER_BRICKS) return Blocks.CRACKED_NETHER_BRICKS.defaultBlockState();
        if (block == Blocks.POLISHED_BLACKSTONE_BRICKS) return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (block == Blocks.COBBLESTONE) return Blocks.GRAVEL.defaultBlockState();
        if (block == Blocks.GRAVEL) return Blocks.SAND.defaultBlockState();
        return null;
    }
}
