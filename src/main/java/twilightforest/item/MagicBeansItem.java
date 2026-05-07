package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import twilightforest.init.TFBlocks;

/**
 * Q17 ported behaviour: right-click {@code uberous_soil} with air above to
 * sprout a {@code beanstalk_grower} stalk and consume one bean. Sound uses a
 * vanilla grass-break fallback because TFSounds.BEANSTALK_GROWTH is not yet
 * registered. Original TF advancement award is skipped (no advancements ported).
 */
public class MagicBeansItem extends CodexItem {

    public MagicBeansItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.getBlockState(pos).is(TFBlocks.UBEROUS_SOIL.get()) && level.getBlockState(pos.above()).isAir()) {
            if (!level.isClientSide()) {
                stack.shrink(1);
                level.setBlockAndUpdate(pos.above(), TFBlocks.BEANSTALK_GROWER.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 4.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
