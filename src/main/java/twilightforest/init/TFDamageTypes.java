package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.util.entities.EntityExcludedDamageSource;

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
    public static final ResourceKey<DamageType> KNIGHTMETAL = create("knightmetal");
    public static final ResourceKey<DamageType> OMINOUS_FIRE = create("ominous_fire");
    public static final ResourceKey<DamageType> REACTOR = create("reactor");
    public static final ResourceKey<DamageType> THROWN_PICKAXE = create("thrown_pickaxe");
    public static final ResourceKey<DamageType> THROWN_AXE = create("thrown_axe");
    public static final ResourceKey<DamageType> STALE_SANDWICH = create("stale_sandwich");
    public static final ResourceKey<DamageType> TWILIGHT_SCEPTER = create("twilight_scepter");
    public static final ResourceKey<DamageType> LIFEDRAIN = create("lifedrain");
    public static final ResourceKey<DamageType> MOONWORM = create("moonworm");
    public static final ResourceKey<DamageType> FAILED_CHALLENGE = create("failed_challenge");
    public static final ResourceKey<DamageType> ACID_RAIN = create("acid_rain");

    private TFDamageTypes() {
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TwilightForestMod.prefix(name));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return source(level, type, new EntityType<?>[0]);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, EntityType<?>... toIgnore) {
        var holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
        return toIgnore.length > 0 ? new EntityExcludedDamageSource(holder, toIgnore) : new DamageSource(holder);
    }

    public static DamageSource entitySource(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker) {
        return entitySource(level, type, attacker, new EntityType<?>[0]);
    }

    public static DamageSource entitySource(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker, EntityType<?>... toIgnore) {
        return indirectSource(level, type, attacker, attacker, toIgnore);
    }

    public static DamageSource indirectSource(Level level, ResourceKey<DamageType> type, @Nullable Entity direct, @Nullable Entity cause) {
        return indirectSource(level, type, direct, cause, new EntityType<?>[0]);
    }

    public static DamageSource indirectSource(Level level, ResourceKey<DamageType> type, @Nullable Entity direct, @Nullable Entity cause, EntityType<?>... toIgnore) {
        var holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
        return toIgnore.length > 0 ? new EntityExcludedDamageSource(holder, direct, cause, toIgnore) : new DamageSource(holder, direct, cause);
    }
}
