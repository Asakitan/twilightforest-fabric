package com.codex.twilight.client;

import com.codex.twilight.client.render.ClientBlockEntityRendererBootstrap;
import com.codex.twilight.client.render.ClientParticleBootstrap;
import com.codex.twilight.client.render.CodexModelLayers;
import com.codex.twilight.client.render.entity.KoboldEntityRenderer;
import com.codex.twilight.network.CodexGogglesSurveyPayload;
import com.codex.twilight.network.CodexHitFlashPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.client.JappaPackReloadListener;
import twilightforest.client.BakedMultiPartRenderers;
import twilightforest.init.TFEntities;
import twilightforest.init.TFMenuTypes;
import twilightforest.client.model.entity.AlphaYetiModel;
import twilightforest.client.model.entity.BighornModel;
import twilightforest.client.model.entity.BoarModel;
import twilightforest.client.model.entity.BlockChainGoblinModel;
import twilightforest.client.model.entity.BunnyModel;
import twilightforest.client.model.entity.LoyalZombieModel;
import twilightforest.client.model.entity.DeerModel;
import twilightforest.client.model.entity.DeathTomeModel;
import twilightforest.client.model.entity.FireBeetleModel;
import twilightforest.client.model.entity.HarbingerCubeModel;
import twilightforest.client.model.entity.HydraModel;
import twilightforest.client.model.entity.KnightPhantomModel;
import twilightforest.client.model.entity.LichModel;
import twilightforest.client.model.entity.LichMinionModel;
import twilightforest.client.model.entity.MinotaurModel;
import twilightforest.client.model.entity.MinoshroomModel;
import twilightforest.client.model.entity.NagaModel;
import twilightforest.client.model.entity.LowerGoblinKnightModel;
import twilightforest.client.model.entity.NoopModel;
import twilightforest.client.model.entity.PenguinModel;
import twilightforest.client.model.entity.PinchBeetleModel;
import twilightforest.client.model.entity.QuestRamModel;
import twilightforest.client.model.entity.RavenModel;
import twilightforest.client.model.entity.RedcapModel;
import twilightforest.client.model.entity.SkeletonDruidModel;
import twilightforest.client.model.entity.SnowQueenModel;
import twilightforest.client.model.entity.StableIceCoreModel;
import twilightforest.client.model.entity.SquirrelModel;
import twilightforest.client.model.entity.TFGhastModel;
import twilightforest.client.model.entity.TinyBirdModel;
import twilightforest.client.model.entity.UnstableIceCoreModel;
import twilightforest.client.model.entity.UrGhastModel;
import twilightforest.client.model.entity.UpperGoblinKnightModel;
import twilightforest.client.model.entity.WraithModel;
import twilightforest.client.model.entity.YetiModel;
import twilightforest.client.renderer.entity.BoarRenderer;
import twilightforest.client.renderer.entity.BlockChainGoblinRenderer;
import twilightforest.client.renderer.entity.BunnyRenderer;
import twilightforest.client.renderer.entity.AdherentRenderer;
import twilightforest.client.renderer.entity.BighornRenderer;
import twilightforest.client.renderer.entity.BirdRenderer;
import twilightforest.client.renderer.entity.CarminiteGhastRenderer;
import twilightforest.client.renderer.entity.CarminiteGolemRenderer;
import twilightforest.client.renderer.entity.CubeOfAnnihilationRenderer;
import twilightforest.client.renderer.entity.CustomProjectileTextureRenderer;
import twilightforest.client.renderer.entity.FallingIceRenderer;
import twilightforest.client.renderer.entity.HelmetCrabRenderer;
import twilightforest.client.renderer.entity.HostileWolfRenderer;
import twilightforest.client.renderer.entity.HydraMortarRenderer;
import twilightforest.client.renderer.entity.HydraRenderer;
import twilightforest.client.renderer.entity.IceCrystalRenderer;
import twilightforest.client.renderer.entity.KnightPhantomRenderer;
import twilightforest.client.renderer.entity.LichRenderer;
import twilightforest.client.renderer.entity.MazeSlimeRenderer;
import twilightforest.client.renderer.entity.MistWolfRenderer;
import twilightforest.client.renderer.entity.MinoshroomRenderer;
import twilightforest.client.renderer.entity.MoonwormShotRenderer;
import twilightforest.client.renderer.entity.MosquitoSwarmRenderer;
import twilightforest.client.renderer.entity.NagaRenderer;
import twilightforest.client.renderer.entity.QuestRamRenderer;
import twilightforest.client.renderer.entity.RovingCubeRenderer;
import twilightforest.client.renderer.entity.RisingZombieRenderer;
import twilightforest.client.renderer.entity.SlideBlockRenderer;
import twilightforest.client.renderer.entity.SlimeBeetleRenderer;
import twilightforest.client.renderer.entity.SnowGuardianRenderer;
import twilightforest.client.renderer.entity.SnowQueenRenderer;
import twilightforest.client.renderer.entity.StableIceCoreRenderer;
import twilightforest.client.renderer.entity.TFBipedRenderer;
import twilightforest.client.renderer.entity.TFGiantRenderer;
import twilightforest.client.renderer.entity.TFGhastRenderer;
import twilightforest.client.renderer.entity.TFGenericMobRenderer;
import twilightforest.client.renderer.entity.TFSpiderRenderer;
import twilightforest.client.renderer.entity.TinyBirdRenderer;
import twilightforest.client.renderer.entity.ThrownBlockRenderer;
import twilightforest.client.renderer.entity.ThrownIceRenderer;
import twilightforest.client.renderer.entity.TrollRenderer;
import twilightforest.client.renderer.entity.UnstableIceCoreRenderer;
import twilightforest.client.renderer.entity.UrGhastRenderer;
import twilightforest.client.renderer.entity.UpperGoblinKnightRenderer;
import twilightforest.client.renderer.entity.WraithRenderer;
import twilightforest.client.renderer.entity.WinterWolfRenderer;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFParticleType;
import twilightforest.item.MagicMapItem;
import twilightforest.item.MazeMapItem;
import twilightforest.item.mapdata.TFMagicMapData;
import twilightforest.item.mapdata.TFMazeMapData;
import twilightforest.network.GogglesZoomPacket;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.LifedrainParticlePacket;
import twilightforest.network.MagicMapPacket;
import twilightforest.network.MazeMapPacket;
import twilightforest.network.MovePlayerPacket;
import twilightforest.network.ParticlePacket;
import twilightforest.network.SetMasonJarItemPacket;
import twilightforest.network.TravellersWingsStatePacket;
import twilightforest.network.UpdateThrownPacket;

/**
 * Phase F2 — paired client renderer entry point.
 *
 * <p>This class is loaded only on Fabric clients (Loom {@code splitEnvironmentSourceSets()}
 * keeps {@code src/client/} out of the dedicated-server classloader), and registers the
 * EntityRenderers / BlockEntityRenderers / ParticleProviders that bring the upstream
 * NeoForge Twilight Forest visuals to life on Fabric. The matching server logic
 * (legacy-disguised entity types, networking handshake, etc.) ships in
 * {@link com.codex.twilight.CodexTwilight} and is a hard runtime peer.
 *
 * <p>F2.1b — first real registration: the Kobold pilot. Vanilla clients
 * (no codex-twilight installed) see a stock zombie disguise via paired-client; clients
 * that have this jar receive the raw TF entity id and dispatch through
 * the paired client renderer registered below. The rest of the TF roster lands in F2.4.
 */
public final class CodexTwilightClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Codex Twilight client init (F2.4 — batch renderer registration for TF mob roster).");
        MenuScreens.register(TFMenuTypes.UNCRAFTING, twilightforest.client.UncraftingScreen::new);
        // F2.1b — Kobold pilot keeps its dedicated renderer with explicit armor layers.
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(JappaPackReloadListener.INSTANCE);
        JappaPackReloadListener.clientSetup();
        twilightforest.init.TFKeyBinds.bootstrap();
        CodexModelLayers.bootstrap();
        registerNoiseVaryingModels();
        EntityRendererRegistry.register(TFEntities.KOBOLD.get(), KoboldEntityRenderer::new);
        EntityRendererRegistry.register(TFEntities.BOAR.get(), ctx ->
            new BoarRenderer<>(ctx, new BoarModel<>(ctx.bakeLayer(CodexModelLayers.BOAR))));
        EntityRendererRegistry.register(TFEntities.DWARF_RABBIT.get(), ctx ->
            new BunnyRenderer(ctx, new BunnyModel(ctx.bakeLayer(CodexModelLayers.BUNNY)), 0.3F));
        EntityRendererRegistry.register(TFEntities.DEER.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new DeerModel(ctx.bakeLayer(CodexModelLayers.DEER)), 0.7F, "wilddeer.png"));
        EntityRendererRegistry.register(TFEntities.PENGUIN.get(), ctx ->
            new BirdRenderer<>(ctx, new PenguinModel(ctx.bakeLayer(CodexModelLayers.PENGUIN)), 0.375F, "penguin.png"));
        EntityRendererRegistry.register(TFEntities.SQUIRREL.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new SquirrelModel(ctx.bakeLayer(CodexModelLayers.SQUIRREL)), 0.3F, "squirrel2.png"));
        EntityRendererRegistry.register(TFEntities.SKELETON_DRUID.get(), ctx ->
            new TFBipedRenderer<>(ctx, new SkeletonDruidModel(ctx.bakeLayer(CodexModelLayers.SKELETON_DRUID)), 0.5F, "skeletondruid.png"));
        EntityRendererRegistry.register(TFEntities.REDCAP.get(), ctx ->
            new TFBipedRenderer<>(ctx,
                new RedcapModel<>(ctx.bakeLayer(CodexModelLayers.REDCAP)),
                new RedcapModel<>(ctx.bakeLayer(CodexModelLayers.REDCAP_ARMOR_INNER)),
                new RedcapModel<>(ctx.bakeLayer(CodexModelLayers.REDCAP_ARMOR_OUTER)),
                0.4F,
                "redcap.png"));
        EntityRendererRegistry.register(TFEntities.REDCAP_SAPPER.get(), ctx ->
            new TFBipedRenderer<>(ctx,
                new RedcapModel<>(ctx.bakeLayer(CodexModelLayers.REDCAP)),
                new RedcapModel<>(ctx.bakeLayer(CodexModelLayers.REDCAP_ARMOR_INNER)),
                new RedcapModel<>(ctx.bakeLayer(CodexModelLayers.REDCAP_ARMOR_OUTER)),
                0.4F,
                "redcapsapper.png"));
        EntityRendererRegistry.register(TFEntities.DEATH_TOME.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new DeathTomeModel(ctx.bakeLayer(CodexModelLayers.DEATH_TOME)), 0.3F, "textures/entity/enchanting_table_book.png"));
        EntityRendererRegistry.register(TFEntities.FIRE_BEETLE.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new FireBeetleModel(ctx.bakeLayer(CodexModelLayers.FIRE_BEETLE)), 0.8F, "firebeetle.png"));
        EntityRendererRegistry.register(TFEntities.PINCH_BEETLE.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new PinchBeetleModel(ctx.bakeLayer(CodexModelLayers.PINCH_BEETLE)), 0.6F, "pinchbeetle.png"));
        EntityRendererRegistry.register(TFEntities.HELMET_CRAB.get(), HelmetCrabRenderer::new);
        EntityRendererRegistry.register(TFEntities.SLIME_BEETLE.get(), ctx ->
            new SlimeBeetleRenderer<>(ctx,
                new twilightforest.client.model.entity.SlimeBeetleModel<>(ctx.bakeLayer(CodexModelLayers.SLIME_BEETLE)),
                ctx.bakeLayer(CodexModelLayers.SLIME_BEETLE_TAIL),
                0.6F));
        EntityRendererRegistry.register(TFEntities.MAZE_SLIME.get(), ctx -> new MazeSlimeRenderer(ctx, 0.625F));
        EntityRendererRegistry.register(TFEntities.HARBINGER_CUBE.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new HarbingerCubeModel<>(ctx.bakeLayer(CodexModelLayers.HARBINGER_CUBE)), 1.0F, "apocalypse2.png"));
        EntityRendererRegistry.register(TFEntities.ROVING_CUBE.get(), RovingCubeRenderer::new);
        EntityRendererRegistry.register(TFEntities.CUBE_OF_ANNIHILATION.get(), CubeOfAnnihilationRenderer::new);
        EntityRendererRegistry.register(TFEntities.HOSTILE_WOLF.get(), HostileWolfRenderer::new);
        EntityRendererRegistry.register(TFEntities.MIST_WOLF.get(), MistWolfRenderer::new);
        EntityRendererRegistry.register(TFEntities.WINTER_WOLF.get(), WinterWolfRenderer::new);
        EntityRendererRegistry.register(TFEntities.SWARM_SPIDER.get(), ctx -> new TFSpiderRenderer<>(ctx, 0.25F, "swarmspider.png", 0.5F));
        EntityRendererRegistry.register(TFEntities.KING_SPIDER.get(), ctx -> new TFSpiderRenderer<>(ctx, 1.25F, "kingspider.png", 1.9F));
        EntityRendererRegistry.register(TFEntities.CARMINITE_BROODLING.get(), ctx -> new TFSpiderRenderer<>(ctx, 0.6F, "towerbroodling.png", 0.7F));
        EntityRendererRegistry.register(TFEntities.HEDGE_SPIDER.get(), ctx -> new TFSpiderRenderer<>(ctx, 0.8F, "hedgespider.png", 1.0F));
        EntityRendererRegistry.register(TFEntities.SNOW_GUARDIAN.get(), ctx ->
            new SnowGuardianRenderer(ctx, new NoopModel<>(ctx.bakeLayer(CodexModelLayers.NOOP))));
        EntityRendererRegistry.register(TFEntities.STABLE_ICE_CORE.get(), ctx ->
            new StableIceCoreRenderer(ctx, new StableIceCoreModel(ctx.bakeLayer(CodexModelLayers.STABLE_ICE_CORE))));
        EntityRendererRegistry.register(TFEntities.UNSTABLE_ICE_CORE.get(), ctx ->
            new UnstableIceCoreRenderer<>(ctx, new UnstableIceCoreModel<>(ctx.bakeLayer(CodexModelLayers.UNSTABLE_ICE_CORE))));
        EntityRendererRegistry.register(TFEntities.ICE_CRYSTAL.get(), IceCrystalRenderer::new);
        EntityRendererRegistry.register(TFEntities.TOWERWOOD_BORER.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new SilverfishModel<>(ctx.bakeLayer(CodexModelLayers.TOWERWOOD_BORER)), 0.3F, "towertermite.png"));
        EntityRendererRegistry.register(TFEntities.MOSQUITO_SWARM.get(), MosquitoSwarmRenderer::new);
        EntityRendererRegistry.register(TFEntities.WRAITH.get(), ctx ->
            new WraithRenderer(ctx, new WraithModel(ctx.bakeLayer(CodexModelLayers.WRAITH)), 0.5F));
        EntityRendererRegistry.register(TFEntities.CARMINITE_GHASTLING.get(), ctx ->
            new TFGhastRenderer<>(ctx, new TFGhastModel<>(ctx.bakeLayer(CodexModelLayers.CARMINITE_GHASTLING)), 0.625F));
        EntityRendererRegistry.register(TFEntities.CARMINITE_GHASTGUARD.get(), ctx ->
            new CarminiteGhastRenderer<>(ctx, new TFGhastModel<>(ctx.bakeLayer(CodexModelLayers.CARMINITE_GHASTGUARD)), 3.0F));
        EntityRendererRegistry.register(TFEntities.CARMINITE_GOLEM.get(), ctx ->
            new CarminiteGolemRenderer<>(ctx, new twilightforest.client.model.entity.CarminiteGolemModel<>(ctx.bakeLayer(CodexModelLayers.CARMINITE_GOLEM)), 0.75F));
        EntityRendererRegistry.register(TFEntities.MINOTAUR.get(), ctx ->
            new TFBipedRenderer<>(ctx, new MinotaurModel(ctx.bakeLayer(CodexModelLayers.MINOTAUR)), 0.625F, "minotaur.png"));
        EntityRendererRegistry.register(TFEntities.MINOSHROOM.get(), ctx ->
            new MinoshroomRenderer<>(ctx, new MinoshroomModel<>(ctx.bakeLayer(CodexModelLayers.MINOSHROOM)), 0.625F));
        EntityRendererRegistry.register(TFEntities.BLOCKCHAIN_GOBLIN.get(), ctx ->
            new BlockChainGoblinRenderer<>(ctx, new BlockChainGoblinModel<>(ctx.bakeLayer(CodexModelLayers.BLOCKCHAIN_GOBLIN)), 0.4F));
        EntityRendererRegistry.register(TFEntities.LOYAL_ZOMBIE.get(), ctx ->
            new TFBipedRenderer<>(ctx, new LoyalZombieModel(ctx.bakeLayer(CodexModelLayers.LOYAL_ZOMBIE)), new LoyalZombieModel(ctx.bakeLayer(net.minecraft.client.model.geom.ModelLayers.ZOMBIE_INNER_ARMOR)), new LoyalZombieModel(ctx.bakeLayer(net.minecraft.client.model.geom.ModelLayers.ZOMBIE_OUTER_ARMOR)), 0.5F, "textures/entity/zombie/zombie.png"));
        EntityRendererRegistry.register(TFEntities.RISING_ZOMBIE.get(), RisingZombieRenderer::new);
        EntityRendererRegistry.register(TFEntities.ADHERENT.get(), AdherentRenderer::new);
        EntityRendererRegistry.register(TFEntities.YETI.get(), ctx ->
            new TFBipedRenderer<>(ctx, new YetiModel<>(ctx.bakeLayer(CodexModelLayers.YETI)), 0.625F, "yeti2.png"));
        EntityRendererRegistry.register(TFEntities.ALPHA_YETI.get(), ctx ->
            new TFBipedRenderer<>(ctx, new AlphaYetiModel(ctx.bakeLayer(CodexModelLayers.ALPHA_YETI)), 1.75F, "yetialpha.png"));
        EntityRendererRegistry.register(TFEntities.KNIGHT_PHANTOM.get(), ctx ->
            new KnightPhantomRenderer(ctx, new KnightPhantomModel(ctx.bakeLayer(CodexModelLayers.KNIGHT_PHANTOM)), 0.625F));
        EntityRendererRegistry.register(TFEntities.SNOW_QUEEN.get(), ctx -> {
            BakedMultiPartRenderers.bakeMultiPartRenderers(ctx);
            return new SnowQueenRenderer<>(ctx, new SnowQueenModel(ctx.bakeLayer(CodexModelLayers.SNOW_QUEEN)));
        });
        EntityRendererRegistry.register(TFEntities.LICH.get(), ctx ->
            new LichRenderer<>(ctx, new LichModel<>(ctx.bakeLayer(CodexModelLayers.LICH)), 0.6F));
        EntityRendererRegistry.register(TFEntities.NAGA.get(), ctx ->
            {
                BakedMultiPartRenderers.bakeMultiPartRenderers(ctx);
                return new NagaRenderer<>(ctx, new NagaModel<>(ctx.bakeLayer(CodexModelLayers.NAGA)), 1.45F);
            });
        EntityRendererRegistry.register(TFEntities.PLATEAU_BOSS.get(), ctx ->
            new TFGenericMobRenderer<>(ctx, new NoopModel<>(ctx.bakeLayer(CodexModelLayers.NOOP)), 0.5F, "textures/entity/iron_golem/iron_golem.png"));
        EntityRendererRegistry.register(TFEntities.HYDRA.get(), ctx -> {
            BakedMultiPartRenderers.bakeMultiPartRenderers(ctx);
            return new HydraRenderer<>(ctx, new HydraModel(ctx.bakeLayer(CodexModelLayers.HYDRA)), 4.0F);
        });
        EntityRendererRegistry.register(TFEntities.UR_GHAST.get(), ctx ->
            new UrGhastRenderer<>(ctx, new UrGhastModel(ctx.bakeLayer(CodexModelLayers.UR_GHAST)), 8.0F, 24.0F));
        EntityRendererRegistry.register(TFEntities.TROLL.get(), TrollRenderer::new);
        EntityRendererRegistry.register(TFEntities.LOWER_GOBLIN_KNIGHT.get(), ctx ->
            new TFBipedRenderer<>(ctx, new LowerGoblinKnightModel(ctx.bakeLayer(CodexModelLayers.LOWER_GOBLIN_KNIGHT)), 0.625F, "doublegoblin.png"));
        EntityRendererRegistry.register(TFEntities.UPPER_GOBLIN_KNIGHT.get(), ctx ->
            new UpperGoblinKnightRenderer<>(ctx, new UpperGoblinKnightModel(ctx.bakeLayer(CodexModelLayers.UPPER_GOBLIN_KNIGHT)), 0.625F));
        EntityRendererRegistry.register(TFEntities.LICH_MINION.get(), ctx ->
            new TFBipedRenderer<>(ctx, new LichMinionModel(ctx.bakeLayer(CodexModelLayers.LICH_MINION)), new LichMinionModel(ctx.bakeLayer(net.minecraft.client.model.geom.ModelLayers.ZOMBIE_INNER_ARMOR)), new LichMinionModel(ctx.bakeLayer(net.minecraft.client.model.geom.ModelLayers.ZOMBIE_OUTER_ARMOR)), 0.5F, "textures/entity/zombie/zombie.png"));
        EntityRendererRegistry.register(TFEntities.GIANT_MINER.get(), TFGiantRenderer::new);
        EntityRendererRegistry.register(TFEntities.ARMORED_GIANT.get(), TFGiantRenderer::new);
        EntityRendererRegistry.register(TFEntities.BIGHORN_SHEEP.get(), ctx ->
            new BighornRenderer(ctx, new BighornModel<>(ctx.bakeLayer(CodexModelLayers.BIGHORN_SHEEP)), 0.7F));
        EntityRendererRegistry.register(TFEntities.QUEST_RAM.get(), ctx ->
            new QuestRamRenderer<>(ctx, new QuestRamModel<>(ctx.bakeLayer(CodexModelLayers.QUEST_RAM))));
        EntityRendererRegistry.register(TFEntities.RAVEN.get(), ctx ->
            new BirdRenderer<>(ctx, new RavenModel(ctx.bakeLayer(CodexModelLayers.RAVEN)), 0.3F, "raven.png"));
        EntityRendererRegistry.register(TFEntities.TINY_BIRD.get(), ctx ->
            new TinyBirdRenderer<>(ctx, new TinyBirdModel(ctx.bakeLayer(CodexModelLayers.TINY_BIRD)), 0.3F));
        EntityRendererRegistry.register(TFEntities.HYDRA_MORTAR.get(), HydraMortarRenderer::new);
        EntityRendererRegistry.register(TFEntities.NATURE_BOLT.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(TFEntities.LICH_BOLT.get(), ctx -> new CustomProjectileTextureRenderer<>(ctx, TwilightForestMod.prefix("textures/particle/twilight_orb.png"), 1.0F, true, false));
        EntityRendererRegistry.register(TFEntities.WAND_BOLT.get(), ctx -> new CustomProjectileTextureRenderer<>(ctx, TwilightForestMod.prefix("textures/particle/twilight_orb.png"), 1.0F, true, false));
        EntityRendererRegistry.register(TFEntities.LICH_BOMB.get(), ctx -> new CustomProjectileTextureRenderer<>(ctx, ResourceLocation.withDefaultNamespace("textures/item/magma_cream.png"), 1.0F, true, true));
        EntityRendererRegistry.register(TFEntities.TOME_BOLT.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(TFEntities.SLIME_BLOB.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(TFEntities.MOONWORM_SHOT.get(), MoonwormShotRenderer::new);
        EntityRendererRegistry.register(TFEntities.FALLING_ICE.get(), FallingIceRenderer::new);
        EntityRendererRegistry.register(TFEntities.SLIDER.get(), SlideBlockRenderer::new);
        EntityRendererRegistry.register(TFEntities.THROWN_ICE.get(), ThrownIceRenderer::new);
        EntityRendererRegistry.register(TFEntities.THROWN_BLOCK.get(), ThrownBlockRenderer::new);
        EntityRendererRegistry.register(TFEntities.ICE_SNOWBALL.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(TFEntities.UR_GHAST_FIREBALL.get(), ctx -> new CustomProjectileTextureRenderer<>(ctx, ResourceLocation.withDefaultNamespace("textures/item/fire_charge.png"), 2.0F, true, false));
        ClientBlockEntityRendererBootstrap.bootstrap();
        // F2.6 — paired ParticleProvider registrations using vanilla SuspendedTownParticle
        // factories with auto-loaded TF sprite atlas. Dormant under current F0 codec
        // swap; activates once F2.6e per-player encoding lands.
        ClientParticleBootstrap.bootstrap();

        // F2.8 — receive the server's hit-flash payload and spawn vanilla CRIT particles
        // around the impact position. The payload type is registered server-side via
        // CodexNetworking.bootstrapServer(); the same registration runs client-side
        // because CodexTwilight#onInitialize is environment="*".
        ClientPlayNetworking.registerGlobalReceiver(CodexHitFlashPayload.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            if (mc.level == null) return;
            mc.execute(() -> {
                if (mc.level == null) return;
                java.util.Random rng = new java.util.Random();
                for (int i = 0; i < 14; i++) {
                    double dx = (rng.nextDouble() - 0.5) * 0.8;
                    double dy = (rng.nextDouble() - 0.5) * 0.8;
                    double dz = (rng.nextDouble() - 0.5) * 0.8;
                    mc.level.addParticle(
                            ParticleTypes.CRIT,
                            payload.x() + dx, payload.y() + dy, payload.z() + dz,
                            dx * 0.3, dy * 0.3 + 0.05, dz * 0.3);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CodexGogglesSurveyPayload.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                spawnGogglesSurveyFlash(mc, payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(LifedrainParticlePacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                spawnLifedrainTrail(mc, payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ParticlePacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                for (ParticlePacket.QueuedParticle particle : payload.queuedParticles()) {
                    if (particle.b()) {
                        mc.level.addAlwaysVisibleParticle(particle.particleOptions(), particle.x(), particle.y(), particle.z(), particle.x2(), particle.y2(), particle.z2());
                    } else {
                        mc.level.addParticle(particle.particleOptions(), particle.x(), particle.y(), particle.z(), particle.x2(), particle.y2(), particle.z2());
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MovePlayerPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.push(payload.motionX(), payload.motionY(), payload.motionZ());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateThrownPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                net.minecraft.world.entity.Entity entity = mc.level.getEntity(payload.entityID());
                if (entity instanceof net.minecraft.world.entity.player.Player player) {
                    var attachment = TFDataAttachments.get(player, TFDataAttachments.YETI_THROWING);
                    net.minecraft.world.entity.Entity throwerEntity = payload.throwerID() != 0 ? mc.level.getEntity(payload.throwerID()) : null;
                    attachment.setThrown(player, payload.thrown(), throwerEntity instanceof net.minecraft.world.entity.LivingEntity living ? living : null);
                    attachment.setThrowCooldown(player, payload.throwCooldown());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GradualGlidePacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(payload.playerUUID());
                if (player != null) {
                    TFDataAttachments.set(player, TFDataAttachments.IS_GRADUALLY_GLIDING, payload.isGraduallyGliding());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GogglesZoomPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(payload.playerUUID());
                if (player != null) {
                    TFDataAttachments.set(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER, payload.isUsingZoom());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TravellersWingsStatePacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                net.minecraft.world.entity.Entity entity = mc.level.getEntity(payload.entityId());
                if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                    TravellersWingsAttachment attachment = TFDataAttachments.get(livingEntity, TFDataAttachments.TRAVELLERS_WINGS);
                    attachment.state = payload.state();
                    attachment.sidestepLeft = payload.sidestepLeft();
                    attachment.doubleJumpTimer = payload.doubleJumpTimer();
                    attachment.sidestepTimer = payload.sidestepTimer();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SetMasonJarItemPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                if (mc.level.getBlockEntity(payload.pos()) instanceof MasonJarBlockEntity jar) {
                    jar.getItemHandler().setItem(payload.stack());
                    jar.setItemRotation(payload.rotation());
                    jar.setChanged();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MagicMapPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                MapRenderer renderer = mc.gameRenderer.getMapRenderer();
                String name = MagicMapItem.getMapName(payload.inner().mapId().id());
                TFMagicMapData data = TFMagicMapData.getMagicMapData(mc.level, name);
                if (data == null) {
                    data = new TFMagicMapData(0, 0, payload.inner().scale(), false, false, payload.inner().locked(), mc.level.dimension());
                    TFMagicMapData.registerMagicMapData(mc.level, data, name);
                }
                payload.inner().applyToMap(data);
                data.conqueredStructures.clear();
                data.conqueredStructures.addAll(payload.conqueredStructures());
                renderer.update(payload.inner().mapId(), data);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MazeMapPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null) return;
                MapRenderer renderer = mc.gameRenderer.getMapRenderer();
                String name = MazeMapItem.getMapName(payload.inner().mapId().id());
                TFMazeMapData data = TFMazeMapData.getMazeMapData(mc.level, name);
                if (data == null) {
                    data = new TFMazeMapData(0, 0, payload.inner().scale(), false, false, payload.inner().locked(), mc.level.dimension());
                    TFMazeMapData.registerMazeMapData(mc.level, data, name);
                }
                data.ore = payload.ore();
                data.yCenter = payload.yCenter();
                payload.inner().applyToMap(data);
                renderer.update(payload.inner().mapId(), data);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(twilightforest.network.UpdateTFMultipartPacket.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            mc.execute(() -> {
                if (mc.level == null || payload.data() == null) return;
                net.minecraft.world.entity.Entity entity = mc.level.getEntity(payload.entityId());
                if (!(entity instanceof twilightforest.entity.TFPart.Owner owner)) return;
                twilightforest.entity.TFPart<?>[] parts = owner.getParts();
                if (parts == null) return;
                for (twilightforest.entity.TFPart<?> part : parts) {
                    twilightforest.network.UpdateTFMultipartPacket.PartDataHolder data = payload.data().get(part.getId());
                    if (data != null) {
                        part.readData(data);
                    }
                }
            });
        });

    }

    /**
     * B5 — register Fabric ModelLoadingPlugin overrides for the seven NeoForge
     * custom model loaders (currently {@code twilightforest:noise_varying}; other
     * loaders land in subsequent batches). Each block id known to use the loader
     * gets its UnbakedModel programmatically swapped, so Fabric's vanilla JSON
     * parser never has to recognise the {@code "loader"} field.
     */
    private static void registerNoiseVaryingModels() {
        // Hardcoded variant lists per block id — each list mirrors the variants array
        // in the upstream NeoForge model JSON exactly. Keeping these in code (rather
        // than re-parsing the JSON we'd otherwise discard) is consistent with how the
        // NeoForge IGeometryLoader feeds variant lists into UnbakedNoiseVaryingModel.
        java.util.List<net.minecraft.resources.ResourceLocation> auroraVariants = new java.util.ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            auroraVariants.add(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("twilightforest", "block/aurora_block_" + i));
        }
        net.minecraft.resources.ResourceLocation auroraBlockModel =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("twilightforest", "block/aurora_block");
        net.minecraft.resources.ResourceLocation auroraBlockModelCodex =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("codex_twilight", "block/aurora_block");

        ModelLoadingPlugin.register(plugin -> {
            plugin.resolveModel().register(context -> {
                net.minecraft.resources.ResourceLocation id = context.id();
                if (id.equals(auroraBlockModel) || id.equals(auroraBlockModelCodex)) {
                    return new twilightforest.client.model.block.aurorablock.NoiseVaryingUnbakedModel(auroraVariants);
                }
                return null;
            });
        });

        LOGGER.info("Codex Twilight client: noise_varying model resolver registered for aurora_block.");
    }

    private static void spawnGogglesSurveyFlash(Minecraft mc, CodexGogglesSurveyPayload payload) {
        Vec3 look = Vec3.directionFromRotation(payload.pitch(), payload.yaw());
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(look).normalize();
        Vec3 center = new Vec3(payload.x(), payload.y(), payload.z());
        for (int ix = -3; ix <= 3; ix++) {
            for (int iy = -1; iy <= 1; iy++) {
                Vec3 p = center.add(right.scale(ix * 0.12D)).add(up.scale(iy * 0.10D));
                mc.level.addParticle(ParticleTypes.END_ROD, p.x, p.y, p.z,
                        look.x * 0.015D, look.y * 0.015D, look.z * 0.015D);
            }
        }
        for (int i = 0; i < 8; i++) {
            double jitter = (i - 3.5D) * 0.025D;
            Vec3 p = center.add(right.scale(jitter)).add(up.scale(Math.sin(i) * 0.025D));
            mc.level.addParticle(ParticleTypes.ENCHANT, p.x, p.y, p.z,
                    right.x * 0.02D, 0.02D, right.z * 0.02D);
        }
    }

    private static void spawnLifedrainTrail(Minecraft mc, LifedrainParticlePacket payload) {
        net.minecraft.world.entity.Entity entity = mc.level.getEntity(payload.entityID());
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) {
            return;
        }

        Vec3 start = living.getEyePosition().subtract(0.0D, living.getBbHeight() * 0.35D, 0.0D);
        Vec3 end = payload.victimPos();
        double distance = start.distanceTo(end);
        int steps = Math.max(1, (int) (distance * 3.0D));

        for (int i = 0; i <= steps; i++) {
            Vec3 pos = start.lerp(end, i / (double) steps);
            mc.level.addParticle(ColorParticleOption.create(TFParticleType.MAGIC_EFFECT, 1.0F, 0.5F, 0.5F),
                    pos.x(), pos.y(), pos.z(), 0.0D, 0.0D, 0.0D);
        }
    }
}
