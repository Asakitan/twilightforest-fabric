package twilightforest.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import twilightforest.enums.HollowLogVariants;

/**
 * Real Block subclass exposing the upstream {@code variant} + {@code facing}
 * state properties so loot-table {@code block_state_property} conditions
 * resolve. Detailed voxel shapes / waterlogging / right-click ability carving
 * are deferred to a deeper port — minimal registration is enough for the
 * datapack loader to bind a real block id with the property names it needs.
 */
public class ClimbableHollowLogBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<HollowLogVariants.Climbable> VARIANT =
            EnumProperty.create("variant", HollowLogVariants.Climbable.class);

    public ClimbableHollowLogBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, HollowLogVariants.Climbable.LADDER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }
}
