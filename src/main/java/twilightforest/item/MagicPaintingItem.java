package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import twilightforest.init.custom.MagicPaintingVariants;

import java.util.Optional;

/**
 * Q34 promotion of Q32 {@code MagicPaintingItem}. Adds variant tracking via
 * {@link MagicPaintingVariants}: shift+right-click in air cycles through the
 * 5 registered variants (stored in the held stack's CustomModelData).
 * Right-click on a wall places a vanilla {@link Painting} and (if the variant
 * has a CMD override in the paired client assets) the held item already shows the
 * variant model server-side.
 *
 * <p>The TF original {@code MagicPainting} entity with parallax/opacity
 * compositing requires a paired-client renderer that codex-twilight does not
 * ship. Without that, the placed entity is a vanilla painting with vanilla art;
 * the variant identity lives only in the held stack until paired client assets add
 * per-variant CMD overrides on {@code minecraft:painting}.</p>
 */
public class MagicPaintingItem extends CodexItem {

    public MagicPaintingItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(player.getItemInHand(hand));
        ItemStack stack = player.getItemInHand(hand);
        int currentCmd = stack.has(DataComponents.CUSTOM_MODEL_DATA)
                ? stack.get(DataComponents.CUSTOM_MODEL_DATA).value()
                : MagicPaintingVariants.Legacy.DEFAULT.cmd();
        MagicPaintingVariants.Legacy.Variant next = MagicPaintingVariants.Legacy.nextAfter(currentCmd);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(next.cmd()));
        if (!level.isClientSide()) {
            player.displayClientMessage(
                    Component.translatable("magic_painting.twilightforest." + next.id() + ".title")
                            .append(Component.literal(" [" + next.id() + "]")),
                    true);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isVertical()) return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos().relative(face);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null || !player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        Optional<Painting> optional = Painting.create(level, pos, face);
        if (optional.isEmpty()) return InteractionResult.CONSUME;

        Painting painting = optional.get();
        if (!painting.survives()) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            painting.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, painting.position());
            level.addFreshEntity(painting);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
