package twilightforest.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Real Block subclass exposing the upstream {@code particle_radius} (1..10) +
 * waterlogged state — covers AbstractParticleSpawnerBlock + FireflySpawnerBlock
 * codec needs.
 */
public class ParticleSpawnerBlock extends Block {
    public static final IntegerProperty RADIUS = IntegerProperty.create("particle_radius", 1, 10);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ParticleSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(RADIUS, 1).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, WATERLOGGED);
    }
}
