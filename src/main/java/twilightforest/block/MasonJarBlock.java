package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.MasonJarBlockEntity;

/**
 * Q33 paired-client 1-slot mason jar. Right-click with empty hand pulls the
 * stored item out, right-click with item swaps in. No menu / no Container
 * implementation — the jar is a server-side curio rather than a chest, so we
 * skip the menu/screen registration entirely (vanilla clients only need the
 * legacy fallback block; paired clients see {@code twilightforest:mason_jar}
 * raw).
 */
public class MasonJarBlock extends CodexBlock implements EntityBlock {

    public MasonJarBlock(BlockBehaviour.Properties properties, BlockState templateState) {
        super(properties, templateState);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MasonJarBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof MasonJarBlockEntity jar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack stored = jar.getStored();

        if (heldStack.isEmpty()) {
            if (!stored.isEmpty()) {
                player.setItemInHand(hand, stored);
                jar.setStored(ItemStack.EMPTY);
                level.playSound(null, pos, SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 0.7F, 0.9F);
                return ItemInteractionResult.sidedSuccess(false);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stored.isEmpty()) {
            jar.setStored(heldStack);
            player.setItemInHand(hand, ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 0.7F, 1.1F);
            return ItemInteractionResult.sidedSuccess(false);
        }

        // Both have content → swap
        player.setItemInHand(hand, stored);
        jar.setStored(heldStack);
        level.playSound(null, pos, SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.BLOCKS, 0.7F, 1.0F);
        return ItemInteractionResult.sidedSuccess(false);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MasonJarBlockEntity jar) {
            ItemStack stored = jar.getStored();
            if (!stored.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stored);
                jar.setStored(ItemStack.EMPTY);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
