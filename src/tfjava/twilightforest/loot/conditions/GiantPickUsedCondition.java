package twilightforest.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import twilightforest.init.TFLoot;

import java.util.Set;

/**
 * Fabric port — codec 1:1 with upstream. Runtime simplified: TFDataAttachments
 * .GIANT_PICKAXE_MINING is not ported in this mod; without that per-entity
 * state we cannot detect the giant-pick mine event, so this always returns
 * false (drop nothing extra).
 */
public record GiantPickUsedCondition(LootContext.EntityTarget target) implements LootItemCondition {

    public static final MapCodec<GiantPickUsedCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(o -> o.target))
                .apply(instance, GiantPickUsedCondition::new));

    @Override
    public LootItemConditionType getType() {
        return TFLoot.GIANT_PICK_USED_CONDITION.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.target.getParam());
    }

    @Override
    public boolean test(LootContext context) {
        return false;
    }

    public static LootItemCondition.Builder builder(LootContext.EntityTarget target) {
        return () -> new GiantPickUsedCondition(target);
    }
}
