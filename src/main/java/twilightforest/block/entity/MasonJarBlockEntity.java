package twilightforest.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.init.TFBlockEntities;

/**
 * Q33 1-slot storage block entity for {@code twilightforest:mason_jar}.
 * Persists a single {@link ItemStack} across chunk loads. The interaction
 * surface lives on {@link twilightforest.block.MasonJarBlock} — empty-hand
 * right-click pulls the stored stack out, right-click with item-in-hand
 * either inserts it (if empty) or swaps it with the stored stack.
 */
public class MasonJarBlockEntity extends BlockEntity {

    private ItemStack stored = ItemStack.EMPTY;
    private int itemRotation;

    public MasonJarBlockEntity(BlockPos pos, BlockState state) {
        super(TFBlockEntities.MASON_JAR, pos, state);
    }

    public ItemStack getStored() {
        return this.stored;
    }

    public void setStored(ItemStack stack) {
        this.stored = stack.copy();
        if (!this.stored.isEmpty()) {
            this.itemRotation = this.level != null ? this.level.random.nextInt(16) : 0;
        }
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getItemRotation() {
        return this.itemRotation;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("stored")) {
            this.stored = ItemStack.parse(registries, tag.getCompound("stored")).orElse(ItemStack.EMPTY);
        } else {
            this.stored = ItemStack.EMPTY;
        }
        this.itemRotation = tag.getInt("rotation");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.stored.isEmpty()) {
            tag.put("stored", this.stored.save(registries, new CompoundTag()));
        }
        tag.putInt("rotation", this.itemRotation);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }
}
