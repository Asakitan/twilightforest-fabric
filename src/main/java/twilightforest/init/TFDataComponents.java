package twilightforest.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Unit;
import net.minecraft.core.UUIDUtil;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.GogglesContents;
import twilightforest.components.item.PotionFlaskComponent;

import java.util.UUID;

public final class TFDataComponents {
    public static final DataComponentType<Unit> TRANSLATABLE_BOOK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("translatable_book"),
            DataComponentType.<Unit>builder().persistent(Unit.CODEC).build()
    );

    /**
     * Q32: registered for {@code BrittleFlaskItem}/{@code GreaterFlaskItem}.
     * Carries the brewed potion contents, dose counter, and breakage counter so
     * vanilla brewing recipes (when wired in a later batch) can write into the
     * stack and the flask drink-flow can read it back. The default for an
     * unset stack is {@link PotionFlaskComponent#EMPTY}.
     */
    public static final DataComponentType<PotionFlaskComponent> POTION_FLASK_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("potion_flask_contents"),
            DataComponentType.<PotionFlaskComponent>builder()
                .persistent(PotionFlaskComponent.CODEC)
                .networkSynchronized(PotionFlaskComponent.STREAM_CODEC)
                .build()
    );

    /**
     * Q38: registered for {@code TravellersGogglesItem} bundle storage.
     * Carries up to 4 stacks (typically maps) inside the goggles. Right-click
     * the goggles in inventory with another item to swap in/out.
     */
    public static final DataComponentType<GogglesContents> GOGGLES_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("goggles_contents"),
            DataComponentType.<GogglesContents>builder()
                .persistent(GogglesContents.CODEC)
                .networkSynchronized(GogglesContents.STREAM_CODEC)
                .build()
    );

    public static final DataComponentType<UUID> THROWN_PROJECTILE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("thrown_projectile"),
            DataComponentType.<UUID>builder()
                .persistent(UUIDUtil.CODEC)
                .networkSynchronized(UUIDUtil.STREAM_CODEC)
                .build()
    );

    /**
     * Aliases under upstream TF ids — referenced by advancement / item
     * predicates that we copied verbatim from upstream. Without these the
     * advancement loader rejects {@code modify_travellers_gear} /
     * {@code full_mettle_alchemist} / {@code experiment_115_self_replenishing}
     * with «No component with type: ...».
     */
    public static final DataComponentType<Unit> WATER_WALK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("water_walk"),
            DataComponentType.<Unit>builder()
                .persistent(Codec.unit(Unit.INSTANCE))
                .build()
    );

    /**
     * Upstream-named alias of {@link #POTION_FLASK_CONTENTS}. Same codec/stream-codec.
     */
    public static final DataComponentType<twilightforest.components.item.PotionFlaskComponent> FLASK_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("flask_contents"),
            DataComponentType.<twilightforest.components.item.PotionFlaskComponent>builder()
                .persistent(twilightforest.components.item.PotionFlaskComponent.CODEC)
                .networkSynchronized(twilightforest.components.item.PotionFlaskComponent.STREAM_CODEC)
                .build()
    );

    public static final DataComponentType<String> E115_VARIANT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("e115_variant"),
            DataComponentType.<String>builder()
                .persistent(Codec.STRING)
                .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                .build()
    );

    /** Q: stamps which item is the lid of a glass jar (firefly/cicada). */
    public static final DataComponentType<twilightforest.components.item.JarLid> JAR_LID = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("jar_lid"),
            DataComponentType.<twilightforest.components.item.JarLid>builder()
                .persistent(twilightforest.components.item.JarLid.CODEC)
                .build()
    );

    /**
     * P2.d: registered for {@link twilightforest.block.CandelabraBlock} pickup-as-itemstack
     * (Silk Touch / clone item). Carries the three candle slots so when a player breaks the
     * candelabra and re-places it, the original arrangement persists.
     */
    public static final DataComponentType<twilightforest.components.item.CandelabraData> CANDELABRA_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            TwilightForestMod.prefix("candelabra_data"),
            DataComponentType.<twilightforest.components.item.CandelabraData>builder()
                .persistent(twilightforest.components.item.CandelabraData.CODEC)
                .networkSynchronized(twilightforest.components.item.CandelabraData.STREAM_CODEC)
                .build()
    );

    private TFDataComponents() {
    }
}
