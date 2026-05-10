package com.codex.twilight.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.block.AbstractTrophyBlock;
import twilightforest.block.CritterBlock;
import twilightforest.block.JarBlock;
import twilightforest.block.MasonJarBlock;
import twilightforest.client.model.entity.LichModel;
import twilightforest.client.model.entity.TrophyBlockModel;
import twilightforest.client.renderer.block.JarRenderer;
import twilightforest.client.renderer.block.SkullCandleRenderer;
import twilightforest.client.renderer.block.TrophyRenderer;
import twilightforest.client.renderer.entity.LichRenderer;
import twilightforest.client.event.ClientGameEvents;
import twilightforest.components.item.JarLid;
import twilightforest.components.item.SkullCandles;
import twilightforest.config.TFConfig;
import twilightforest.enums.BossVariant;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.item.MysticCrownItem;

import java.util.HashMap;
import java.util.Map;

/**
 * F2.6b — paired-client BEWLR-equivalent registration for items whose upstream
 * NeoForge model uses {@code parent: builtin/entity} and an
 * {@link net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer}.
 *
 * <p>Each branch mirrors the relevant case in upstream
 * {@code twilightforest.client.ISTER#renderByItem} so the inventory icon stays
 * 1:1 with the placed BER. Item-model JSONs for these items must use
 * {@code parent: minecraft:builtin/entity} so vanilla flags the BakedModel as
 * {@code isCustomRenderer() == true} — otherwise the registered renderer is
 * never invoked.
 *
 * <p>Coverage:
 * <ul>
 *   <li>{@link CritterBlock} (cicada / firefly / moonworm) — transient BE +
 *       BER pass-through.</li>
 *   <li>{@link JarBlock} (mason_jar / cicada_jar / firefly_jar) — static jar
 *       body via {@link JarRenderer#renderJarModel} + lid via
 *       {@link JarRenderer#LIDS}; {@link MasonJarBlock} additionally renders
 *       its {@link ItemContainerContents}.</li>
 *   <li>Simple BE blocks (brazier / candelabra / keepsake_casket /
 *       skull_chest) — transient BE + BER pass-through via
 *       {@link BlockEntityRenderDispatcher#renderItem}.</li>
 *   <li>TF chests (16 variants) — pre-built {@link ChestBlockEntity} per
 *       chest block + dispatcher.renderItem.</li>
 *   <li>Trophies (9 variants) — head-only render via
 *       {@link TrophyRenderer#render}; rotating in GUI per
 *       {@link TFConfig#rotateTrophyHeadsGui}.</li>
 *   <li>{@link MysticCrownItem} — Lich hat ModelPart (mirrors upstream).</li>
 *   <li>Skull candles (6 variants) — vanilla skull model + colored candle
 *       block stack from {@link SkullCandles} component.</li>
 * </ul>
 */
public final class ClientItemRendererBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/client/item-renderer");

    // ---- caches ------------------------------------------------------------

    private static final Map<CritterBlock, BlockEntity> CRITTER_CACHE = new HashMap<>();
    private static final Map<Block, BlockEntity> SIMPLE_BE_CACHE = new HashMap<>();
    private static final Map<Block, ChestBlockEntity> CHEST_CACHE = new HashMap<>();

    /** Lazily baked once EntityModelSet is loaded. */
    private static Map<BossVariant, TrophyBlockModel> trophiesCache;
    private static Map<SkullBlock.Type, SkullModelBase> skullsCache;
    private static boolean warnedTrophyMissing;
    private static boolean warnedSkullMissing;

    private ClientItemRendererBootstrap() {
    }

    public static void bootstrap() {
        // Critters
        registerCritter(TFBlocks.CICADA.get());
        registerCritter(TFBlocks.FIREFLY.get());
        registerCritter(TFBlocks.MOONWORM.get());

        // Jars
        registerJar(TFBlocks.MASON_JAR.get());
        registerJar(TFBlocks.CICADA_JAR.get());
        registerJar(TFBlocks.FIREFLY_JAR.get());

        // Simple block-entity pass-throughs (single block, single BER)
        registerSimpleBlockEntity(TFBlocks.BRAZIER.get());
        registerSimpleBlockEntity(TFBlocks.CANDELABRA.get());
        registerSimpleBlockEntity(TFBlocks.KEEPSAKE_CASKET.get());
        registerSimpleBlockEntity(TFBlocks.SKULL_CHEST.get());

        // TF chests (8 plain + 8 trapped)
        registerChest(TFBlocks.TWILIGHT_OAK_CHEST.get());
        registerChest(TFBlocks.CANOPY_CHEST.get());
        registerChest(TFBlocks.MANGROVE_CHEST.get());
        registerChest(TFBlocks.DARK_CHEST.get());
        registerChest(TFBlocks.MINING_CHEST.get());
        registerChest(TFBlocks.TIME_CHEST.get());
        registerChest(TFBlocks.TRANSFORMATION_CHEST.get());
        registerChest(TFBlocks.SORTING_CHEST.get());
        registerChest(TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST.get());
        registerChest(TFBlocks.CANOPY_TRAPPED_CHEST.get());
        registerChest(TFBlocks.MANGROVE_TRAPPED_CHEST.get());
        registerChest(TFBlocks.DARK_TRAPPED_CHEST.get());
        registerChest(TFBlocks.MINING_TRAPPED_CHEST.get());
        registerChest(TFBlocks.TIME_TRAPPED_CHEST.get());
        registerChest(TFBlocks.TRANSFORMATION_TRAPPED_CHEST.get());
        registerChest(TFBlocks.SORTING_TRAPPED_CHEST.get());

        // Trophies (boss heads only — not the plaque backdrop, which uses
        // NeoForge-specific BakedModel.applyTransform that has no Fabric
        // equivalent without a porter shim. Head-only matches placed render.)
        registerTrophy(TFBlocks.NAGA_TROPHY.get());
        registerTrophy(TFBlocks.LICH_TROPHY.get());
        registerTrophy(TFBlocks.HYDRA_TROPHY.get());
        registerTrophy(TFBlocks.UR_GHAST_TROPHY.get());
        registerTrophy(TFBlocks.KNIGHT_PHANTOM_TROPHY.get());
        registerTrophy(TFBlocks.SNOW_QUEEN_TROPHY.get());
        registerTrophy(TFBlocks.MINOSHROOM_TROPHY.get());
        registerTrophy(TFBlocks.ALPHA_YETI_TROPHY.get());
        registerTrophy(TFBlocks.QUEST_RAM_TROPHY.get());

        // Mystic Crown (Lich-hat-only render, mirroring upstream)
        registerMysticCrown(TFItems.MYSTIC_CROWN.get());

        // Skull candles (6 plain heads — wall variants are not items)
        registerSkullCandle(TFBlocks.CREEPER_SKULL_CANDLE.get());
        registerSkullCandle(TFBlocks.SKELETON_SKULL_CANDLE.get());
        registerSkullCandle(TFBlocks.WITHER_SKELETON_SKULL_CANDLE.get());
        registerSkullCandle(TFBlocks.ZOMBIE_SKULL_CANDLE.get());
        registerSkullCandle(TFBlocks.PLAYER_SKULL_CANDLE.get());
        registerSkullCandle(TFBlocks.PIGLIN_SKULL_CANDLE.get());

        LOGGER.info("ClientItemRendererBootstrap: BEWLR-equivalent renderers wired for 3 critters + 3 jars + 4 simple BE + 16 TF chests + 9 trophies + mystic_crown + 6 skull candles.");
    }

    // ---- critter (cicada / firefly / moonworm) -----------------------------

    private static void registerCritter(Block block) {
        if (!(block instanceof CritterBlock critter)) {
            LOGGER.warn("registerCritter skipped non-critter block: {}", block);
            return;
        }
        Item item = block.asItem();
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            BlockEntity be = CRITTER_CACHE.computeIfAbsent(critter,
                cb -> cb.newBlockEntity(BlockPos.ZERO, cb.defaultBlockState()));
            if (be == null) return;
            BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            @SuppressWarnings({"rawtypes", "unchecked"})
            BlockEntityRenderer renderer = dispatcher.getRenderer(be);
            if (renderer != null) {
                renderer.render(null, 0.0F, pose, buffers, light, overlay);
            }
        });
    }

    // ---- jars (mason_jar / cicada_jar / firefly_jar) -----------------------

    private static void registerJar(Block block) {
        if (!(block instanceof JarBlock jarBlock)) {
            LOGGER.warn("registerJar skipped non-jar block: {}", block);
            return;
        }
        Item item = block.asItem();
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            Minecraft minecraft = Minecraft.getInstance();
            BlockState state = jarBlock.defaultBlockState();
            JarRenderer.renderJarModel(state, minecraft.getBlockRenderer(), pose, buffers, light, overlay);
            JarLid jarLid = stack.get(TFDataComponents.JAR_LID);
            Item lidItem = (jarLid != null && JarRenderer.LIDS.containsKey(jarLid.lid()))
                ? jarLid.lid()
                : jarBlock.getDefaultLid();
            BakedModel lidModel = JarRenderer.LIDS.get(lidItem);
            if (lidModel != null) {
                JarRenderer.renderModel(lidModel, state, minecraft.getBlockRenderer(), pose, buffers, light, overlay);
            }
            if (jarBlock instanceof MasonJarBlock) {
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                if (contents != null) {
                    ItemStack inside = contents.copyOne();
                    if (!inside.isEmpty()) {
                        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
                        pose.pushPose();
                        pose.translate(0.5D, 0.4375D, 0.5D);
                        pose.scale(0.5F, 0.5F, 0.5F);
                        BakedModel insideModel = minecraft.getItemRenderer().getModel(inside, null, null, 1);
                        minecraft.getItemRenderer().render(inside, ItemDisplayContext.FIXED, false, pose, bufferSource, light, OverlayTexture.NO_OVERLAY, insideModel);
                        pose.popPose();
                        bufferSource.endBatch();
                    }
                }
            }
        });
    }

    // ---- simple BE pass-through (brazier / candelabra / casket / skull_chest)

    private static void registerSimpleBlockEntity(Block block) {
        Item item = block.asItem();
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            BlockEntity be = SIMPLE_BE_CACHE.computeIfAbsent(block, b -> {
                if (b instanceof net.minecraft.world.level.block.EntityBlock eb) {
                    return eb.newBlockEntity(BlockPos.ZERO, b.defaultBlockState());
                }
                return null;
            });
            if (be == null) return;
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(be, pose, buffers, light, overlay);
        });
    }

    // ---- TF chests ---------------------------------------------------------

    private static void registerChest(Block block) {
        Item item = block.asItem();
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            ChestBlockEntity be = CHEST_CACHE.computeIfAbsent(block, b -> {
                if (b instanceof net.minecraft.world.level.block.EntityBlock eb) {
                    BlockEntity made = eb.newBlockEntity(BlockPos.ZERO, b.defaultBlockState());
                    return made instanceof ChestBlockEntity chest ? chest : null;
                }
                return null;
            });
            if (be == null) return;
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(be, pose, buffers, light, overlay);
        });
    }

    // ---- trophies ----------------------------------------------------------

    private static void registerTrophy(Block block) {
        if (!(block instanceof AbstractTrophyBlock trophyBlock)) {
            LOGGER.warn("registerTrophy skipped non-trophy block: {}", block);
            return;
        }
        Item item = block.asItem();
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            Map<BossVariant, TrophyBlockModel> trophies = trophiesMap();
            if (trophies == null) return;
            BossVariant variant = trophyBlock.getVariant();
            TrophyBlockModel model = trophies.get(variant);
            if (model == null) return;
            Minecraft minecraft = Minecraft.getInstance();
            float anim = !minecraft.isPaused()
                ? ClientGameEvents.time + minecraft.getTimer().getRealtimeDeltaTicks()
                : 0.0F;
            if (mode == ItemDisplayContext.GUI) {
                pose.pushPose();
                pose.translate(0.5F, 0.5F, 0.5F);
                pose.mulPose(Axis.XP.rotationDegrees(30));
                pose.mulPose(Axis.YN.rotationDegrees(
                    TFConfig.rotateTrophyHeadsGui && !minecraft.isPaused()
                        ? ClientGameEvents.time % 360
                        : -45));
                pose.translate(-0.5F, -0.5F, -0.5F);
                pose.translate(0.0F, 0.25F, 0.0F);
                if (variant == BossVariant.UR_GHAST) pose.translate(0.0F, 0.5F, 0.0F);
                if (variant == BossVariant.ALPHA_YETI) pose.translate(0.0F, -0.15F, 0.0F);
                TrophyRenderer.render(null, 180.0F, model, variant, anim, pose, buffers, light, mode);
                pose.popPose();
            } else {
                TrophyRenderer.render(null, 180.0F, model, variant, anim, pose, buffers, light, mode);
            }
        });
    }

    // ---- mystic crown ------------------------------------------------------

    private static void registerMysticCrown(Item item) {
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            if (!(item instanceof MysticCrownItem)) return;
            Map<BossVariant, TrophyBlockModel> trophies = trophiesMap();
            if (trophies == null) return;
            if (!(trophies.get(BossVariant.LICH) instanceof LichModel<?> lichModel)) return;
            lichModel.hat.yRot = 0;
            pose.pushPose();
            pose.scale(1.0F, -1.0F, -1.0F);
            if (mode == ItemDisplayContext.GUI) {
                pose.translate(0.5F, -0.1F, 0.0F);
                pose.last().normal().rotateY((float) (Math.PI * 0.5));
                pose.mulPose(Axis.XP.rotationDegrees(30));
                pose.mulPose(Axis.YP.rotationDegrees(45));
            } else {
                pose.translate(0.5F, 0.0F, -0.5F);
                pose.scale(1.05F, 1.05F, 1.05F);
            }
            lichModel.hat.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(LichRenderer.TEXTURE)), light, overlay);
            pose.popPose();
        });
    }

    // ---- skull candles -----------------------------------------------------

    private static void registerSkullCandle(Block block) {
        if (!(block instanceof AbstractSkullCandleBlock candleBlock)) {
            LOGGER.warn("registerSkullCandle skipped non-skull-candle block: {}", block);
            return;
        }
        Item item = block.asItem();
        if (item == null) return;
        BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> {
            Minecraft minecraft = Minecraft.getInstance();
            ResolvableProfile profile = stack.get(DataComponents.PROFILE);
            if (profile != null && !profile.isResolved()) {
                stack.remove(DataComponents.PROFILE);
                profile.resolve().thenAcceptAsync(p -> stack.set(DataComponents.PROFILE, p), minecraft);
                return;
            }
            Map<SkullBlock.Type, SkullModelBase> skulls = skullsMap();
            if (skulls == null) return;
            SkullBlock.Type type = candleBlock.getType();
            SkullModelBase base = skulls.get(type);
            if (base == null) return;
            RenderType rt = SkullCandleRenderer.getRenderType(type, profile);
            SkullCandleRenderer.renderSkull(null, 180.0F, 0.0F, pose, buffers, light, base, rt);

            // Stack the candle on top of the skull
            pose.translate(0.0F, 0.5F, 0.0F);
            SkullCandles components = stack.getOrDefault(TFDataComponents.SKULL_CANDLES, SkullCandles.DEFAULT);
            Block candle = AbstractSkullCandleBlock.candleColorToCandle(
                AbstractSkullCandleBlock.CandleColors.colorFromInt(components.color()));
            BlockState candleState = candle.defaultBlockState().setValue(CandleBlock.CANDLES, components.count());
            minecraft.getBlockRenderer().renderSingleBlock(candleState, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        });
    }

    // ---- lazy model bakers --------------------------------------------------

    private static Map<BossVariant, TrophyBlockModel> trophiesMap() {
        if (trophiesCache != null) return trophiesCache;
        try {
            trophiesCache = TrophyRenderer.createTrophyRenderers(Minecraft.getInstance().getEntityModels());
            return trophiesCache;
        } catch (RuntimeException e) {
            if (!warnedTrophyMissing) {
                warnedTrophyMissing = true;
                LOGGER.warn("Trophy model bake not yet ready; trophy item rendering will retry next frame.", e);
            }
            return null;
        }
    }

    private static Map<SkullBlock.Type, SkullModelBase> skullsMap() {
        if (skullsCache != null) return skullsCache;
        try {
            skullsCache = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels());
            return skullsCache;
        } catch (RuntimeException e) {
            if (!warnedSkullMissing) {
                warnedSkullMissing = true;
                LOGGER.warn("Skull model bake not yet ready; skull-candle rendering will retry next frame.", e);
            }
            return null;
        }
    }
}
