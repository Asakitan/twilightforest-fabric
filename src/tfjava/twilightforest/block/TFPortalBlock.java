package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port of upstream {@link twilightforest.block.TFPortalBlock} — paired
 * down to the runtime essentials so the block carries proper state properties
 * (so {@code block_state_property} loot conditions resolve, no «no property X»
 * error) and triggers a real dimension transition when a player drops in. The
 * full portal-frame validation + lightning + toast wiring from upstream is
 * deferred to a deeper port; players still need a real pool for the portal
 * to be created (handled by the {@code MagicBeansItem}/{@code MagicMapItem}
 * existing logic), but once it exists, stepping into it teleports.
 */
public class TFPortalBlock extends HalfTransparentBlock implements Portal {

    public static final BooleanProperty DISALLOW_RETURN = BooleanProperty.create("is_one_way");
    private static final VoxelShape AABB = Shapes.create(new AABB(0.0F, 0.0F, 0.0F, 1.0F, 0.8125F, 1.0F));
    public static final ResourceKey<Level> TWILIGHT_DIMENSION = ResourceKey.create(Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("catty", "twilight_realm"));

    public TFPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(DISALLOW_RETURN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISALLOW_RETURN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return state.getValue(DISALLOW_RETURN) ? AABB : Shapes.empty();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        // Portal pool is treated as water for sugar-cane / waterlogged compatibility.
        return Fluids.WATER.getFlowing(1, false);
    }

    @Override
    @Nullable
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(DISALLOW_RETURN) && level.dimension().equals(TWILIGHT_DIMENSION)) {
            return null;
        }
        ResourceKey<Level> targetKey;
        if (level.dimension().equals(TWILIGHT_DIMENSION)) {
            targetKey = Level.OVERWORLD;
        } else {
            targetKey = TWILIGHT_DIMENSION;
        }
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        ServerLevel target = server.getLevel(targetKey);
        if (target == null) return null;
        Vec3 dest = entity.position();
        return new DimensionTransition(target, dest, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return entity.isOnPortalCooldown() ? 0 : 80;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel serverLevel && entity.canChangeDimensions(level, serverLevel)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }
}
