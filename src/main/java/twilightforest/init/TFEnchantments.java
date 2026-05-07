package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import twilightforest.TwilightForestMod;

public final class TFEnchantments {
    public static final ResourceKey<Enchantment> DESTRUCTION = key("destruction");
    public static final ResourceKey<Enchantment> RENEWAL = key("renewal");
    public static final ResourceKey<Enchantment> FIRE_REACT = key("fire_react");
    public static final ResourceKey<Enchantment> CHILL_AURA = key("chill_aura");

    private TFEnchantments() {
    }

    public static void bootstrap() {
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, TwilightForestMod.prefix(path));
    }
}
