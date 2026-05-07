package twilightforest.entity.boss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import twilightforest.TwilightForestMod;

public class HydraNeck extends HydraPart {
    public static final ResourceLocation RENDERER = TwilightForestMod.prefix("hydra_neck");

    public final HydraHead head;
    private final int headIndex;
    private final int segmentIndex;

    public HydraNeck(HydraHead head, int headIndex, int segmentIndex) {
        super(head.getParent(), 2.0F, 2.0F);
        this.head = head;
        this.headIndex = headIndex;
        this.segmentIndex = segmentIndex;
    }

    @Override
    public ResourceLocation renderer() {
        return RENDERER;
    }

    public int getHeadIndex() {
        return this.headIndex;
    }

    public int getSegmentIndex() {
        return this.segmentIndex;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return this.head.interact(player, hand);
    }
}
