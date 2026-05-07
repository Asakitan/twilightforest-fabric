package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

public final class TFDamageTypes {
    public static final ResourceKey<DamageType> EXPIRED = create("expired");
    public static final ResourceKey<DamageType> HAUNT = create("haunt");
    public static final ResourceKey<DamageType> SCORCHED = create("scorched");
    public static final ResourceKey<DamageType> SPIKED = create("spiked");
    public static final ResourceKey<DamageType> FROZEN = create("frozen");
    public static final ResourceKey<DamageType> FALLING_ICE = create("falling_ice");
    public static final ResourceKey<DamageType> GHAST_TEAR = create("ghast_tear");
    public static final ResourceKey<DamageType> SQUISH = create("squish");
    public static final ResourceKey<DamageType> AXING = create("axing");
    public static final ResourceKey<DamageType> CHILLING_BREATH = create("chilling_breath");
    public static final ResourceKey<DamageType> SLAM = create("slam");
    public static final ResourceKey<DamageType> THROWN_BLOCK = create("thrown_block");
    public static final ResourceKey<DamageType> HYDRA_MORTAR = create("hydra_mortar");
    public static final ResourceKey<DamageType> SNOWBALL_FIGHT = create("snowball_fight");
    public static final ResourceKey<DamageType> LEAF_BRAIN = create("leaf_brain");
    public static final ResourceKey<DamageType> LICH_BOLT = create("lich_bolt");
    public static final ResourceKey<DamageType> LICH_BOMB = create("lich_bomb");
    public static final ResourceKey<DamageType> LOST_WORDS = create("lost_words");
    public static final ResourceKey<DamageType> SCHOOLED = create("schooled");
    public static final ResourceKey<DamageType> HYDRA_BITE = create("hydra_bite");
    public static final ResourceKey<DamageType> HYDRA_FIRE = create("hydra_fire");
    public static final ResourceKey<DamageType> CLAMPED = create("clamped");
    public static final ResourceKey<DamageType> ANT = create("ant");
    public static final ResourceKey<DamageType> OREBERRY = create("oreberry");
    public static final ResourceKey<DamageType> THORNS = create("thorns");
    public static final ResourceKey<DamageType> FIRE_JET = create("fire_jet");
    public static final ResourceKey<DamageType> SLIDER = create("slider");
    public static final ResourceKey<DamageType> FIERY = create("fiery");

    private TFDamageTypes() {
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TwilightForestMod.prefix(name));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }

    public static DamageSource entitySource(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker) {
        return indirectSource(level, type, attacker, attacker);
    }

    public static DamageSource indirectSource(Level level, ResourceKey<DamageType> type, @Nullable Entity direct, @Nullable Entity cause) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), direct, cause);
    }
}
