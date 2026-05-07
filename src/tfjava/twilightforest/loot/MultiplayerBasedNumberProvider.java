package twilightforest.loot;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import twilightforest.init.TFLoot;

import java.util.Set;

/**
 * Fabric port — DI-free, no TFConfig / TFDataAttachments dependency.
 *
 * <p>Codec is identical to upstream so loot-table JSON parses byte-equivalent.
 * The {@link #getFloat} hook is simplified to «always return defaultRolls»;
 * paired-fight multiplayer adjustment is not wired in this server-only port,
 * but parsing succeeds and chests still drop the default loot count.</p>
 */
public record MultiplayerBasedNumberProvider(NumberProvider rollsPerPlayer, NumberProvider defaultRolls) implements NumberProvider {
    public static final MapCodec<MultiplayerBasedNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviders.CODEC.fieldOf("per_player_rolls").forGetter(MultiplayerBasedNumberProvider::rollsPerPlayer),
            NumberProviders.CODEC.fieldOf("default_rolls").forGetter(MultiplayerBasedNumberProvider::defaultRolls))
        .apply(instance, MultiplayerBasedNumberProvider::new)
    );

    @Override
    public LootNumberProviderType getType() {
        return TFLoot.MULTIPLAYER_ROLLS.get();
    }

    @Override
    public float getFloat(LootContext context) {
        return this.defaultRolls.getFloat(context);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.rollsPerPlayer.getReferencedContextParams(), this.defaultRolls.getReferencedContextParams());
    }
}
