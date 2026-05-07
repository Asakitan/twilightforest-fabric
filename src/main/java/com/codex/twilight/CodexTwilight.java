package com.codex.twilight;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import twilightforest.TFRegistries;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFDataSerializers;
import twilightforest.init.TFDensityFunctions;
import twilightforest.init.TFEntities;
import twilightforest.init.TFCaveCarvers;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFFeatureModifiers;
import twilightforest.init.TFMapDecorations;
import twilightforest.init.TFParticleTypes;
import twilightforest.init.TFSounds;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.init.TFStructurePlacementTypes;
import twilightforest.init.TFStructureProcessors;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.StructureSpeleothemConfig;

/**
 * Codex Twilight entry point.
 *
 * Scope (first iteration):
 *   - Dimension/noise tweaks live in resources/data/catty (carried over from the
 *     legacy catty_twilight_realm datapack; phase F will fold the rest in).
 *   - Worldgen content (configured/placed features, structures) lives under
 *     resources/data/codex_twilight; sub-bootstraps register Java-side hooks
 *     where datapack JSON is not enough.
 *   - Custom items, blocks, entities, sounds, and particles sync as real
 *     Twilight registry ids. The paired client loads the bundled assets from
 *     this same mod jar.
 *
 * Twilight entity migration is incremental: official registry IDs are owned by
 * Java EntityTypes here, with individual classes promoted from compatibility
 * stand-ins as their server-side dependency chains are ported.
 */
public final class CodexTwilight implements ModInitializer {

    public static final String MOD_ID = "codex_twilight";

    public static String id(String path) {
        return MOD_ID + ":" + path;
    }

    @Override
    public void onInitialize() {
        DynamicRegistries.register(TFRegistries.Keys.STRUCTURE_SPELEOTHEM_SETTINGS, StructureSpeleothemConfig.CODEC);
        // TINY_BIRD_VARIANT and DWARF_RABBIT_VARIANT are referenced by TFDataSerializers via
        // ByteBufCodecs.holderRegistry(...) in entity SynchedEntityData. The registry must therefore
        // exist on the client too — register them as SYNCED so fabric-registry-sync ships the registry
        // (with all data-pack entries) to the connecting client during play handshake.
        DynamicRegistries.registerSynced(TFRegistries.Keys.TINY_BIRD_VARIANT,
            twilightforest.entity.passive.TinyBirdVariant.DIRECT_CODEC);
        DynamicRegistries.registerSynced(TFRegistries.Keys.DWARF_RABBIT_VARIANT,
            twilightforest.entity.passive.DwarfRabbitVariant.DIRECT_CODEC);
        TFDataSerializers.bootstrap();
        // F2.8 — register S2C payload type early so it's available before any TF mob hits arrive.
        com.codex.twilight.network.CodexNetworking.bootstrapServer();
        TFEntities.ARMORED_GIANT.get();
        TFEntities.addEntityAttributes();
        twilightforest.init.TFRecipes.bootstrap();
        twilightforest.init.TFMenuTypes.bootstrap();
        twilightforest.init.TFAdvancements.bootstrap();
        twilightforest.init.TFStats.bootstrap();
        twilightforest.init.TFItemSubPredicates.bootstrap();
        twilightforest.init.TFLoot.bootstrap();
        twilightforest.init.TFMobEffects.bootstrap();
        twilightforest.init.TFEnchantmentEffects.bootstrap();
        TFDataComponents.TRANSLATABLE_BOOK.toString();
        TFMapDecorations.AURORA_PALACE.value();
        TFSounds.bootstrap();
        twilightforest.init.TFItems.bootstrap();
        twilightforest.init.TFBlockEntities.bootstrap();
        TFParticleTypes.bootstrap();
        TFFeatureModifiers.bootstrap();
        TFDensityFunctions.bootstrap();
        TFCaveCarvers.bootstrap();
        TFStructurePlacementTypes.bootstrap();
        TFStructureTypes.bootstrap();
        TFStructurePieceTypes.bootstrap();
        TFStructureProcessors.bootstrap();

        // ===== LANE_A_BOOTSTRAP (Claude — terrain/density/layer system) =====
        // Add density router registrations, datapack layer-stack registry, etc. here.
        // Owned per AGENTS.md. Do NOT modify if you are Lane B.
        twilightforest.init.custom.BiomeLayerTypes.bootstrap();
        twilightforest.init.TFBiomeSources.bootstrap();
        twilightforest.init.TFFeatures.bootstrap();
        DynamicRegistries.register(TFRegistries.Keys.BIOME_STACK,
                twilightforest.init.custom.BiomeLayerStack.DISPATCH_CODEC);
        DynamicRegistries.register(TFRegistries.Keys.BIOME_TERRAIN_DATA,
                twilightforest.world.components.layer.BiomeDensitySource.CODEC);
        // ===== END LANE_A_BOOTSTRAP =====

        // ===== Q22 charm event hooks =====
        // CharmOfLife: revive player on death; CharmOfKeeping: preserve inventory.
        // Registered globally; charm presence is checked per death.
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH.register(
                (entity, source, amount) -> {
                    if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return true;
                    net.minecraft.world.item.ItemStack charm = twilightforest.item.CharmOfLifeItem.findCharm(player);
                    if (charm.isEmpty()) return true;
                    return !twilightforest.item.CharmOfLifeItem.tryRevive(player, source, charm);
                });

        // ===== LANE_B_BOOTSTRAP (Copilot — entities + paired-client renderers) =====
        // Add SpawnPlacements.register() calls and entity wiring here.
        // Owned per AGENTS.md. Do NOT modify if you are Lane A.
        TFBlocks.CICADA.get();
        TFBlocks.FIREFLY.get();
        // ===== END LANE_B_BOOTSTRAP =====

        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleHooks::setCurrentServer);
        // Phase F1.4 — /codex ops command
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                twilightforest.command.CodexCommand::register);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerLifecycleHooks.setCurrentServer(null));

        // Q35 — Travellers gear extra tick effects (red-thread-vision goggles + dolphin-grace belt
        // swift-swim). Keeps these out of the per-piece TravellersArmorPieceItem.inventoryTick to
        // avoid one-effect-per-piece collision with the Q32 base buffs.
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                // Per-tick: stealth (invisibility-lite), gradual glide clamp.
                if (twilightforest.init.custom.TravellersModifiersManager.isStealthActive(player)
                        && player.isCrouching()) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.INVISIBILITY, 40, 0, true, false, true));
                }
                if (twilightforest.init.custom.TravellersModifiersManager.isGradualGlideActive(player)
                        && !player.onGround() && !player.isInWater() && !player.onClimbable()
                        && !player.isFallFlying() && player.getDeltaMovement().y < -0.4D) {
                    net.minecraft.world.phys.Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, -0.4D, m.z);
                    player.fallDistance = 0.0F;
                }
                if (twilightforest.init.custom.TravellersModifiersManager.isStraightAheadActive(player)
                        && player.isSprinting() && player.onGround()) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 0, true, false, true));
                }

                // water_walk — frost-walker-style: turn water under the wearer's feet to FROSTED_ICE.
                // Throttled to every 4 ticks (~5 Hz) to keep block-state writes off the hot path.
                if (server.getTickCount() % 4 == 0
                        && twilightforest.init.custom.TravellersModifiersManager.isWaterWalkActive(player)
                        && !player.isCrouching()) {
                    net.minecraft.core.BlockPos under = player.blockPosition().below();
                    int radius = 1;
                    net.minecraft.world.level.block.state.BlockState frosted = net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState();
                    net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            cursor.set(under.getX() + dx, under.getY(), under.getZ() + dz);
                            net.minecraft.world.level.block.state.BlockState here = player.serverLevel().getBlockState(cursor);
                            if (here.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
                                    && here.getBlock() == net.minecraft.world.level.block.Blocks.WATER
                                    && here.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) == 0
                                    && frosted.canSurvive(player.serverLevel(), cursor)
                                    && player.serverLevel().isUnobstructed(frosted, cursor, net.minecraft.world.phys.shapes.CollisionContext.empty())) {
                                player.serverLevel().setBlockAndUpdate(cursor, frosted);
                                player.serverLevel().scheduleTick(cursor.immutable(),
                                        net.minecraft.world.level.block.Blocks.FROSTED_ICE,
                                        Math.max(60, player.getRandom().nextInt(40) + 60));
                            }
                        }
                    }
                }

                // Every 1 second: water-aware passives.
                if (server.getTickCount() % 20 == 0) {
                    if (twilightforest.init.custom.TravellersModifiersManager.isRedThreadVisionActive(player)) {
                        net.minecraft.world.phys.Vec3 here = player.position();
                        net.minecraft.world.phys.AABB scan = new net.minecraft.world.phys.AABB(here, here).inflate(16.0D);
                        for (net.minecraft.world.entity.LivingEntity target : player.serverLevel().getEntitiesOfClass(
                                net.minecraft.world.entity.LivingEntity.class, scan,
                                e -> e != player && e.isAlive())) {
                            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.GLOWING, 60, 0, true, false, false));
                        }
                    }
                    if (twilightforest.init.custom.TravellersModifiersManager.isSwiftSwimActive(player)
                            && player.isInWater()) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, 60, 0, true, false, true));
                    }
                    if (twilightforest.init.custom.TravellersModifiersManager.isUnrestrainedActive(player)
                            && player.isInWater()) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.WATER_BREATHING, 60, 0, true, false, true));
                    }
                }

                // Every 5 seconds: auto-repair worn TF travellers gear.
                if (server.getTickCount() % 100 == 0
                        && twilightforest.init.custom.TravellersModifiersManager.isAutoRepairActive(player)) {
                    for (net.minecraft.world.item.ItemStack worn : player.getArmorSlots()) {
                        if (worn.isDamaged()
                                && (worn.is(twilightforest.init.TFItems.TRAVELLERS_GOGGLES.get())
                                || worn.is(twilightforest.init.TFItems.TRAVELLERS_VEST.get())
                                || worn.is(twilightforest.init.TFItems.TRAVELLERS_GLOVES.get())
                                || worn.is(twilightforest.init.TFItems.TRAVELLERS_WINGS.get())
                                || worn.is(twilightforest.init.TFItems.TRAVELLERS_BELT.get())
                                || worn.is(twilightforest.init.TFItems.TRAVELLERS_BOOTS.get()))) {
                            worn.setDamageValue(Math.max(0, worn.getDamageValue() - 1));
                        }
                    }
                }
            }
        });

        // Q36: efficient_eater — bonus hunger on food consumption while wearing belt.
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, level, hand) -> {
            net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
            if (!stack.has(net.minecraft.core.component.DataComponents.FOOD)) return net.minecraft.world.InteractionResultHolder.pass(stack);
            if (level.isClientSide()) return net.minecraft.world.InteractionResultHolder.pass(stack);
            if (twilightforest.init.custom.TravellersModifiersManager.isEfficientEaterActive(player)) {
                player.getFoodData().eat(1, 0.4F);
            }
            return net.minecraft.world.InteractionResultHolder.pass(stack);
        });
        System.out.println("[CodexTwilight] initialized (v0.1.0 scaffold)");
    }
}
