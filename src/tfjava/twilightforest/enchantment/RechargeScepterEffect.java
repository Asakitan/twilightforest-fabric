package twilightforest.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/**
 * Fabric port — codec 1:1 with upstream. Runtime simplified to no-op because
 * upstream relies on {@code ScepterRepairRecipe} (custom recipe type that we
 * have not ported). The codec still parses {@code fire_react.json} so that
 * enchantment loads; the recharge effect just doesn't fire until a deeper port
 * brings the recipe back.
 */
public record RechargeScepterEffect() implements EnchantmentEntityEffect {

    public static final MapCodec<RechargeScepterEffect> CODEC = MapCodec.unit(RechargeScepterEffect::new);

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 vec3) {
        // No-op until ScepterRepairRecipe is ported.
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
