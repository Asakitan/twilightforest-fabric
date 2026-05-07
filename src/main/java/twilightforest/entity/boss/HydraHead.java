package twilightforest.entity.boss;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import twilightforest.TwilightForestMod;

public class HydraHead extends HydraPart {
    public static final ResourceLocation RENDERER = TwilightForestMod.prefix("hydra_head");

    private static final EntityDataAccessor<Float> DATA_MOUTH_POSITION = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MOUTH_POSITION_LAST = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.FLOAT);

    private final int index;

    public HydraHead(Hydra hydra, int index) {
        super(hydra, 4.0F, 4.0F);
        this.index = index;
    }

    @Override
    public ResourceLocation renderer() {
        return RENDERER;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOUTH_POSITION, 0.0F);
        builder.define(DATA_MOUTH_POSITION_LAST, 0.0F);
    }

    public int getIndex() {
        return this.index;
    }

    public float getMouthOpen() {
        return this.getEntityData().get(DATA_MOUTH_POSITION);
    }

    public float getMouthOpenLast() {
        return this.getEntityData().get(DATA_MOUTH_POSITION_LAST);
    }

    public void setMouthOpen(float openness) {
        this.getEntityData().set(DATA_MOUTH_POSITION_LAST, this.getMouthOpen());
        this.getEntityData().set(DATA_MOUTH_POSITION, openness);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Component tagName = stack.get(DataComponents.CUSTOM_NAME);
        if (stack.is(Items.NAME_TAG) && tagName != null) {
            if (!this.level().isClientSide() && this.isAlive()) {
                this.setCustomName(tagName);
                this.getParent().setHeadNameFor(this.index, tagName.getString());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.interact(player, hand);
    }
}
