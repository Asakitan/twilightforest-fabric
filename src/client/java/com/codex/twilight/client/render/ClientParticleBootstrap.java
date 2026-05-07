package com.codex.twilight.client.render;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import twilightforest.client.particle.MagicEffectParticle;
import twilightforest.init.TFParticleTypes;

/**
 * F2.6 — paired client-side particle providers for TF custom particles.
 *
 * <p>Most TF {@link SimpleParticleType} ids are bound to vanilla
 * {@link SuspendedTownParticle.Provider}: a small 2D sprite-based particle
 * with gentle drift physics. The provider receives the {@code SpriteSet}
 * auto-loaded by the vanilla particle atlas from
 * {@code assets/twilightforest/textures/particle/<id>.png} (shipped inside
 * the codex-twilight mod jar after F2.1a's asset migration), so when a TF
 * particle id reaches the client, it renders with the genuine upstream
 * Twilight Forest texture.
 *
 * <p>The motion is intentionally generic; upstream's per-particle motion
 * logic (firefly buzzing, leaf-rune spiral, boss-tear gravity) would need
 * an IP-clean adaptation of upstream particle classes, which is deferred to
 * a future F2.6e iteration. For now, sprite-correct stationary particles
 * are visually identifiable and correctly distinguishable from vanilla.
 *
 * <p>Runtime atmosphere reaches these providers through normal client-side
 * biome and particle rendering. Server-side ambient particle payloads and
 * vanilla fallback substitutions have been removed for paired-client mode.
 */
public final class ClientParticleBootstrap {

    private ClientParticleBootstrap() {
    }

    public static void bootstrap() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        register(registry, TFParticleTypes.LARGE_FLAME);
        register(registry, TFParticleTypes.LEAF_RUNE);
        register(registry, TFParticleTypes.BOSS_TEAR);
        register(registry, TFParticleTypes.GHAST_TRAP);
        register(registry, TFParticleTypes.PROTECTION);
        register(registry, TFParticleTypes.SNOW);
        register(registry, TFParticleTypes.SNOW_WARNING);
        register(registry, TFParticleTypes.EXTENDED_SNOW_WARNING);
        register(registry, TFParticleTypes.SNOW_GUARDIAN);
        register(registry, TFParticleTypes.ICE_BEAM);
        register(registry, TFParticleTypes.ANNIHILATE);
        register(registry, TFParticleTypes.PERFECT_DODGE);
        register(registry, TFParticleTypes.DOUBLE_JUMP);
        register(registry, TFParticleTypes.HUGE_SMOKE);
        register(registry, TFParticleTypes.FIREFLY);
        register(registry, TFParticleTypes.WANDERING_FIREFLY);
        register(registry, TFParticleTypes.PARTICLE_SPAWNER_FIREFLY);
        register(registry, TFParticleTypes.FALLEN_LEAF);
        register(registry, TFParticleTypes.DIM_FLAME);
        register(registry, TFParticleTypes.OMINOUS_FLAME);
        register(registry, TFParticleTypes.SORTING_PARTICLE);
        register(registry, TFParticleTypes.TRANSFORMATION_PARTICLE);
        register(registry, TFParticleTypes.LOG_CORE_PARTICLE);
        register(registry, TFParticleTypes.CLOUD_PUFF);
        registerMagicEffect(registry, TFParticleTypes.MAGIC_EFFECT);
        register(registry, TFParticleTypes.ANGRY_LICH);
        register(registry, TFParticleTypes.TWILIGHT_ORB);
        register(registry, TFParticleTypes.SHIELD_BREAK);
        register(registry, TFParticleTypes.DRYING_RACK);
    }

    private static void register(ParticleFactoryRegistry registry, SimpleParticleType type) {
        if (type == null) return;
        try {
            registry.register(type, SuspendedTownParticle.Provider::new);
        } catch (Throwable ignored) {
            // Defensive — never let one bad provider abort the bootstrap.
        }
    }

    private static void registerMagicEffect(ParticleFactoryRegistry registry, ParticleType<ColorParticleOption> type) {
        if (type == null) return;
        try {
            registry.register(type, MagicEffectParticle.Factory::new);
        } catch (Throwable ignored) {
            // Defensive — never let one bad provider abort the bootstrap.
        }
    }
}
