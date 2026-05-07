package twilightforest.init;

import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import twilightforest.TFRegistries;
import twilightforest.entity.passive.DwarfRabbitVariant;
import twilightforest.entity.passive.TinyBirdVariant;

public final class TFDataSerializers {
        private static boolean bootstrapped;

    public static final EntityDataSerializer<Holder<TinyBirdVariant>> TINY_BIRD_VARIANT =
            EntityDataSerializer.forValueType(ByteBufCodecs.holderRegistry(TFRegistries.Keys.TINY_BIRD_VARIANT));
    public static final EntityDataSerializer<Holder<DwarfRabbitVariant>> DWARF_RABBIT_VARIANT =
            EntityDataSerializer.forValueType(ByteBufCodecs.holderRegistry(TFRegistries.Keys.DWARF_RABBIT_VARIANT));

        public static void bootstrap() {
                if (bootstrapped) {
                        return;
                }
                EntityDataSerializers.registerSerializer(TINY_BIRD_VARIANT);
                EntityDataSerializers.registerSerializer(DWARF_RABBIT_VARIANT);
                bootstrapped = true;
        }

    private TFDataSerializers() {
    }
}