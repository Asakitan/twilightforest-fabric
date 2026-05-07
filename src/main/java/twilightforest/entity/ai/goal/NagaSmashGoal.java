package twilightforest.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import twilightforest.entity.boss.Naga;
import twilightforest.util.entities.EntityUtil;

public class NagaSmashGoal extends Goal {
    private final Naga naga;

    public NagaSmashGoal(Naga naga) {
        this.naga = naga;
    }

    @Override
    public boolean canUse() {
        return this.naga.horizontalCollision && this.naga.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    @Override
    public void start() {
        if (!this.naga.level().isClientSide()) {
            AABB bounds = this.naga.getBoundingBox();
            BlockPos min = new BlockPos(Mth.floor(bounds.minX - 0.75D), Mth.floor(bounds.minY + 1.01D), Mth.floor(bounds.minZ - 0.75D));
            BlockPos max = new BlockPos(Mth.floor(bounds.maxX + 0.75D), Mth.floor(bounds.maxY), Mth.floor(bounds.maxZ + 0.75D));
            if (this.naga.level().hasChunksAt(min, max)) {
                for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                    BlockState state = this.naga.level().getBlockState(pos);
                    if (state.is(BlockTags.LEAVES) || (this.naga.shouldDestroyAllBlocks() && EntityUtil.canDestroyBlock(this.naga.level(), pos, this.naga))) {
                        this.naga.level().destroyBlock(pos, !state.is(BlockTags.LEAVES));
                    }
                }
            }
        }
    }
}