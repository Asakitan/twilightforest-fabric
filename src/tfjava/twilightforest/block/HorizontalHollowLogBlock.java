package twilightforest.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import twilightforest.enums.HollowLogVariants;

/**
 * Real {@link Block} subclass exposing the upstream {@code variant} +
 * {@code axis} state properties so loot-table {@code block_state_property}
 * conditions resolve. The full custom voxel shape, waterlogging, item-ability
 * carve interactions are deferred to a deeper port; this minimal port is
 * enough to make the registry id, default state, and property resolution
 * available so the datapack loader doesn't reject blocks/loot.
 */
public class HorizontalHollowLogBlock extends Block {
    public static final EnumProperty<Direction.Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final EnumProperty<HollowLogVariants.Horizontal> VARIANT =
            EnumProperty.create("variant", HollowLogVariants.Horizontal.class);

    public HorizontalHollowLogBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(HORIZONTAL_AXIS, Direction.Axis.X)
                .setValue(VARIANT, HollowLogVariants.Horizontal.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_AXIS, VARIANT);
    }
}
