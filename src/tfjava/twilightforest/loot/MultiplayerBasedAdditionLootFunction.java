package twilightforest.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import twilightforest.config.TFConfig;
import twilightforest.init.TFLoot;

import java.util.List;

/**
 * Codec is 1:1 with upstream so loot-table JSONs parse byte-equivalent.
 * Runtime references {@link TFConfig#multiplayerFightAdjuster}, which in this
 * Fabric port returns {@code false} for {@code adjustsLootRolls()}; the call
 * still goes through, just doesn't multiply because the per-entity
 * MULTIPLAYER_FIGHT attachment is not populated server-side yet.
 */
public class MultiplayerBasedAdditionLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<MultiplayerBasedAdditionLootFunction> CODEC = RecordCodecBuilder.mapCodec(
        inst -> commonFields(inst)
            .and(NumberProviders.CODEC.fieldOf("extra_count_per_player").forGetter(o -> o.value))
            .apply(inst, MultiplayerBasedAdditionLootFunction::new)
    );

    private final NumberProvider value;

    public MultiplayerBasedAdditionLootFunction(List<LootItemCondition> predicates, NumberProvider value) {
        super(predicates);
        this.value = value;
    }

    public static Builder addForAllParticipatingPlayers(NumberProvider additionPerPlayer) {
        return new Builder(additionPerPlayer);
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return TFLoot.MULTIPLAYER_MULTIPLIER.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (TFConfig.multiplayerFightAdjuster.adjustsLootRolls()) {
            // Real attachment-based scaling lands when fabric-data-attachment-api-v1
            // is wired and TFDataAttachments.MULTIPLAYER_FIGHT is populated by the
            // boss damage hooks. Until then the configured default (false) keeps
            // this branch off and the stack passes through unchanged.
            return stack;
        }
        return stack;
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final NumberProvider count;

        public Builder(NumberProvider perPlayer) {
            this.count = perPlayer;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public MultiplayerBasedAdditionLootFunction build() {
            return new MultiplayerBasedAdditionLootFunction(this.getConditions(), this.count);
        }
    }
}
