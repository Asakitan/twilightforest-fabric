package com.codex.twilight.network;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * F2.8 — central wiring for codex-twilight S2C packets.
 *
 * <p>{@link #bootstrapServer()} runs once during the main mod initializer
 * (server side) and registers payload codecs + the damage event hook that
 * broadcasts {@link CodexHitFlashPayload} when a Twilight Forest entity
 * takes damage. The client-side mirror lives in
 * {@code com.codex.twilight.client.CodexTwilightClient}.
 *
 * <p>Detection rule: any living entity whose registered type id belongs to the
 * {@code twilightforest} namespace is treated as a TF mob. Vanilla mobs are not
 * matched because paired-client mode no longer uses server-side disguise types.
 */
public final class CodexNetworking {

    private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/network");

    private CodexNetworking() {
    }

    public static void bootstrapServer() {
        // Register payload type once — must run before any send / receive.
        PayloadTypeRegistry.playS2C().register(CodexHitFlashPayload.TYPE, CodexHitFlashPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(CodexGogglesSurveyPayload.TYPE, CodexGogglesSurveyPayload.STREAM_CODEC);

        // Server-side hit hook: fabric-entity-events-v1 fires AFTER_DAMAGE per damage event.
        // We broadcast the hit-flash payload to every tracking player so paired clients can
        // spawn vanilla CRIT particles at the impact position.
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (LivingEntity entity, DamageSource source, float baseDamage, float damageTaken, boolean blocked) -> {
                    ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    if (typeId == null || !"twilightforest".equals(typeId.getNamespace())) return;
                    if (entity.level().isClientSide()) return;
                    if (!(entity.level() instanceof ServerLevel)) return;
                    if (damageTaken <= 0.0F || blocked) return;

                    CodexHitFlashPayload payload = new CodexHitFlashPayload(
                            entity.getX(),
                            entity.getY() + entity.getBbHeight() * 0.5,
                            entity.getZ());
                    for (ServerPlayer viewer : PlayerLookup.tracking(entity)) {
                        try {
                            ServerPlayNetworking.send(viewer, payload);
                        } catch (Throwable ignored) {
                            // Defensive — never let a bad recipient stall damage processing.
                        }
                    }
                    if (source.getEntity() instanceof Player attacker && attacker instanceof ServerPlayer self) {
                        try {
                            ServerPlayNetworking.send(self, payload);
                        } catch (Throwable ignored) {
                        }
                    }
                });
        LOGGER.info("CodexNetworking bootstrap complete (hit flash and goggles survey payloads registered).");
    }

    public static boolean canSendPairedClientPayload(ServerPlayer player) {
        try {
            return ServerPlayNetworking.canSend(player, CodexHitFlashPayload.TYPE)
                    || ServerPlayNetworking.canSend(player, CodexGogglesSurveyPayload.TYPE);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void sendGogglesSurvey(ServerPlayer player,
                                         double x, double y, double z,
                                         float yaw, float pitch) {
        try {
            if (!ServerPlayNetworking.canSend(player, CodexGogglesSurveyPayload.TYPE)) return;
            ServerPlayNetworking.send(player, new CodexGogglesSurveyPayload(x, y, z, yaw, pitch));
        } catch (Throwable ignored) {
        }
    }

    /**
     * Run on server stop / startup events that need to flush state. Currently
     * a no-op; reserved so the entry point exists for future packet kinds.
     */
    public static void registerLifecycle() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {});
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {});
    }
}
