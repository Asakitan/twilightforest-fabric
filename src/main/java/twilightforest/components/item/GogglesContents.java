package twilightforest.components.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Q38 simplified goggles bundle storage. Carries up to {@link #MAX_SLOTS} maps
 * inside the {@code travellers_goggles} item. Vanilla clients see the goggles
 * normally; the inventory secondary-click handler shifts items in/out via the
 * standard bundle interaction pattern.
 *
 * <p>Replaces TF's {@code ItemDisplayContents} (which carried filter rules and
 * an active-slot marker for HUD-overlay map rendering — both depend on a
 * paired-client renderer the codex-twilight port does not ship).</p>
 */
public record GogglesContents(List<ItemStack> items) {

    public static final int MAX_SLOTS = 4;
    public static final GogglesContents EMPTY = new GogglesContents(List.of());

    public static final Codec<GogglesContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(GogglesContents::items)
    ).apply(instance, GogglesContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GogglesContents> STREAM_CODEC =
            ItemStack.OPTIONAL_LIST_STREAM_CODEC.map(GogglesContents::new, GogglesContents::items);

    /** Returns the first non-empty stored stack, or {@link ItemStack#EMPTY}. */
    public ItemStack first() {
        for (ItemStack s : this.items) {
            if (!s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    public boolean canFit() {
        return this.items.size() < MAX_SLOTS;
    }

    public GogglesContents withInserted(ItemStack stack) {
        if (stack.isEmpty()) return this;
        if (!this.canFit()) return this;
        List<ItemStack> next = new ArrayList<>(this.items);
        next.add(stack.copy());
        return new GogglesContents(List.copyOf(next));
    }

    public GogglesContents withRemovedFirst() {
        if (this.items.isEmpty()) return this;
        List<ItemStack> next = new ArrayList<>(this.items);
        next.remove(0);
        return new GogglesContents(List.copyOf(next));
    }

    public NonNullList<ItemStack> asNonNullList() {
        NonNullList<ItemStack> list = NonNullList.create();
        list.addAll(this.items);
        return list;
    }
}
