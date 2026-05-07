package twilightforest.block;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Stub block class providing the static state properties referenced by FallenLeavesFeature.
 * The actual block at runtime is a vanilla block (mapped via TFBlocks.FALLEN_LEAVES alias),
 * but to compile TF feature source 1:1 we expose the same property handles TF used.
 *
 * <p>LAYERS is the same property vanilla SnowLayerBlock uses (1..8), so feature
 * code that calls {@code state.setValue(FallenLeavesBlock.LAYERS, n)} on a vanilla
 * snow block also works correctly.
 */
public final class FallenLeavesBlock {
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;

    private FallenLeavesBlock() {
    }
}
