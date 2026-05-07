package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Q38 simplified port of TF {@code ExanimateEssenceItem}.
 *
 * <p>TF original morphs vanilla lit candles into custom {@code OminousCandleBlock}
 * variants and places {@code OminousFireBlock} on replaceable surfaces. Porting
 * those needs new block types + a candle→ominous-candle map per dye colour
 * (16+1 vanilla candle subclasses) — heavyweight.</p>
 *
 * <p>This Fabric port keeps the *gameplay shape* with vanilla blocks: clicking
 * a lit vanilla candle surrounds it with a purple SOUL_FIRE_FLAME particle
 * burst (audible-only ominous mark). Clicking a replaceable surface places
 * a vanilla {@link SoulFireBlock} (purple flame block, identical visual to
 * what the TF original called "ominous fire"). Either path consumes 1 essence
 * + plays the ominous-fire sound.</p>
 */
public class ExanimateEssenceItem extends CodexItem {

    public ExanimateEssenceItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        boolean acted = false;

        if (state.getBlock() instanceof CandleBlock && state.getValue(CandleBlock.LIT)) {
            if (level instanceof ServerLevel sl) {
                for (int i = 0; i < 18; i++) {
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            pos.getX() + 0.5D + (sl.getRandom().nextFloat() - 0.5F) * 0.6D,
                            pos.getY() + 0.7D + sl.getRandom().nextFloat() * 0.5D,
                            pos.getZ() + 0.5D + (sl.getRandom().nextFloat() - 0.5F) * 0.6D,
                            1, 0, 0.02, 0, 0.0);
                }
            }
            playSound(level, pos);
            acted = true;
        } else {
            BlockPos placePos = pos.relative(context.getClickedFace());
            BlockState here = level.getBlockState(placePos);
            BlockState soulFire = Blocks.SOUL_FIRE.defaultBlockState();
            if (here.canBeReplaced() && soulFire.canSurvive(level, placePos)) {
                level.setBlock(placePos, soulFire, Block.UPDATE_ALL);
                level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, placePos);
                playSound(level, placePos);
                acted = true;
            }
        }

        if (!acted) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void playSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS,
                1.5F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 0.75F);
    }
}
