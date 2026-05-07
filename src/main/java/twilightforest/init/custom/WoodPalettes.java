package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Blocks;
import twilightforest.util.WoodPalette;

public final class WoodPalettes {
    private static final Holder<WoodPalette> DEFAULT = Holder.direct(new WoodPalette(
            Blocks.OAK_PLANKS,
            Blocks.OAK_STAIRS,
            Blocks.OAK_SLAB,
            Blocks.OAK_BUTTON,
            Blocks.OAK_FENCE,
            Blocks.OAK_FENCE_GATE,
            Blocks.OAK_PRESSURE_PLATE,
            Blocks.OAK_FENCE
    ));

    public static final Codec<Holder<WoodPalette>> CODEC = Codec.unit(DEFAULT);

    private WoodPalettes() {
    }
}
