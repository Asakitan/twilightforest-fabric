package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity.WobbleStyle;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.init.TFBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JarRenderer<T extends JarBlockEntity> implements BlockEntityRenderer<T> {

	public static final Map<Item, BakedModel> LIDS = new HashMap<>();

	public record LidResource(Item lid, ResourceLocation resourceLocation, @Nullable String customPath) {
		public LidResource(Item item, ResourceLocation id) {
			this(item, id, null);
		}

		public LidResource(Item item, String vanillaPath) {
			this(item, ResourceLocation.fromNamespaceAndPath("minecraft", vanillaPath), null);
		}

		public LidResource(Item item, String vanillaPath, String customPath) {
			this(item, ResourceLocation.fromNamespaceAndPath("minecraft", vanillaPath), customPath);
		}
	}

	private static List<LidResource> CACHED_LID_LIST;

	public static List<LidResource> lidLocationList() {
		if (CACHED_LID_LIST != null) return CACHED_LID_LIST;
		List<LidResource> list = new ArrayList<>();
		list.add(new LidResource(TFBlocks.MANGROVE_LOG.get().asItem(), TwilightForestMod.prefix("mangrove_log")));
		list.add(new LidResource(TFBlocks.CANOPY_LOG.get().asItem(), TwilightForestMod.prefix("canopy_log")));
		list.add(new LidResource(TFBlocks.DARK_LOG.get().asItem(), TwilightForestMod.prefix("dark_log")));
		list.add(new LidResource(TFBlocks.MINING_LOG.get().asItem(), TwilightForestMod.prefix("mining_log")));
		list.add(new LidResource(TFBlocks.SORTING_LOG.get().asItem(), TwilightForestMod.prefix("sorting_log")));
		list.add(new LidResource(TFBlocks.TIME_LOG.get().asItem(), TwilightForestMod.prefix("time_log")));
		list.add(new LidResource(TFBlocks.TRANSFORMATION_LOG.get().asItem(), TwilightForestMod.prefix("transformation_log")));
		list.add(new LidResource(TFBlocks.TWILIGHT_OAK_LOG.get().asItem(), TwilightForestMod.prefix("twilight_oak_log")));
		list.add(new LidResource(Items.ACACIA_LOG, "acacia_log"));
		list.add(new LidResource(Items.BIRCH_LOG, "birch_log"));
		list.add(new LidResource(Items.CHERRY_LOG, "cherry_log"));
		list.add(new LidResource(Items.DARK_OAK_LOG, "dark_oak_log"));
		list.add(new LidResource(Items.JUNGLE_LOG, "jungle_log"));
		list.add(new LidResource(Items.MANGROVE_LOG, "mangrove_log", "vanilla_mangrove_log"));
		list.add(new LidResource(Items.OAK_LOG, "oak_log"));
		list.add(new LidResource(Items.SPRUCE_LOG, "spruce_log"));
		list.add(new LidResource(Items.CRIMSON_STEM, "crimson_stem"));
		list.add(new LidResource(Items.WARPED_STEM, "warped_stem"));
		list.add(new LidResource(TFBlocks.STRIPPED_MANGROVE_LOG.get().asItem(), TwilightForestMod.prefix("stripped_mangrove_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_CANOPY_LOG.get().asItem(), TwilightForestMod.prefix("stripped_canopy_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_DARK_LOG.get().asItem(), TwilightForestMod.prefix("stripped_dark_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_MINING_LOG.get().asItem(), TwilightForestMod.prefix("stripped_mining_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_SORTING_LOG.get().asItem(), TwilightForestMod.prefix("stripped_sorting_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_TIME_LOG.get().asItem(), TwilightForestMod.prefix("stripped_time_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_TRANSFORMATION_LOG.get().asItem(), TwilightForestMod.prefix("stripped_transformation_log")));
		list.add(new LidResource(TFBlocks.STRIPPED_TWILIGHT_OAK_LOG.get().asItem(), TwilightForestMod.prefix("stripped_twilight_oak_log")));
		list.add(new LidResource(Items.STRIPPED_ACACIA_LOG, "stripped_acacia_log"));
		list.add(new LidResource(Items.STRIPPED_BIRCH_LOG, "stripped_birch_log"));
		list.add(new LidResource(Items.STRIPPED_CHERRY_LOG, "stripped_cherry_log"));
		list.add(new LidResource(Items.STRIPPED_DARK_OAK_LOG, "stripped_dark_oak_log"));
		list.add(new LidResource(Items.STRIPPED_JUNGLE_LOG, "stripped_jungle_log"));
		list.add(new LidResource(Items.STRIPPED_MANGROVE_LOG, "stripped_mangrove_log", "vanilla_stripped_mangrove_log"));
		list.add(new LidResource(Items.STRIPPED_OAK_LOG, "stripped_oak_log"));
		list.add(new LidResource(Items.STRIPPED_SPRUCE_LOG, "stripped_spruce_log"));
		list.add(new LidResource(Items.STRIPPED_CRIMSON_STEM, "stripped_crimson_stem"));
		list.add(new LidResource(Items.STRIPPED_WARPED_STEM, "stripped_warped_stem"));
		list.add(new LidResource(TFBlocks.CINDER_LOG.get().asItem(), TwilightForestMod.prefix("cinder_log")));
		list.add(new LidResource(Items.PUMPKIN, "pumpkin"));
		list.add(new LidResource(Items.BAMBOO_BLOCK, "bamboo_block"));
		list.add(new LidResource(Items.STRIPPED_BAMBOO_BLOCK, "stripped_bamboo_block"));
		CACHED_LID_LIST = list;
		return list;
	}

	public static String lidPath(LidResource lid) {
		return lid.customPath() != null ? lid.customPath() : lid.resourceLocation().getPath();
	}

	public static ModelResourceLocation lidModelResource(LidResource lid) {
		return new ModelResourceLocation(TwilightForestMod.prefix("block/lid/" + lidPath(lid)), "standalone");
	}

	protected final BlockRenderDispatcher blockRenderer;
	protected static final float WOBBLE_AMPLITUDE = 0.125F;

	public JarRenderer(BlockEntityRendererProvider.Context context) {
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.0, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate(-0.5, 0.0, -0.5);
		WobbleStyle wobbleStyle = blockEntity.lastWobbleStyle;

		if (wobbleStyle != null && blockEntity.getLevel() != null) {
			float f = ((float) (blockEntity.getLevel().getGameTime() - blockEntity.wobbleStartedAtTick) + partialTick) / (float) wobbleStyle.duration;
			if (f >= 0.0F && f <= 1.0F) {
				if (wobbleStyle == WobbleStyle.POSITIVE) {
					float f1 = 0.015625F;
					float f2 = f * (float) (Math.PI * 2);
					float f3 = -1.5F * (Mth.cos(f2) + 0.5F) * Mth.sin(f2 / 2.0F);
					poseStack.rotateAround(Axis.XP.rotation(f3 * f1), 0.5F, 0.0F, 0.5F);
					float f4 = Mth.sin(f2);
					poseStack.rotateAround(Axis.ZP.rotation(f4 * f1), 0.5F, 0.0F, 0.5F);
				} else {
					float f5 = Mth.sin(-f * 3.0F * (float) Math.PI) * WOBBLE_AMPLITUDE;
					float f6 = 1.0F - f;
					poseStack.rotateAround(Axis.YP.rotation(f5 * f6), 0.5F, 0.0F, 0.5F);
				}
			}
		}

		BlockState state = blockEntity.getBlockState();
		BakedModel lidModel = LIDS.get(blockEntity.getLid());
		if (lidModel != null) {
			renderModel(lidModel, state, this.blockRenderer, poseStack, buffer, packedLight, packedOverlay);
		}
		// Jar body is rendered by the vanilla static-model pipeline (JarBlock.getRenderShape() = MODEL).
		// BER only contributes the lid + contents on top. Wobble therefore only animates lid + contents.
		this.renderContents(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);

		poseStack.popPose();
	}

	public static void renderJarModel(BlockState blockState, BlockRenderDispatcher blockRenderer, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		BakedModel bakedModel = blockRenderer.getBlockModel(blockState);
		renderModel(bakedModel, blockState, blockRenderer, stack, buffer, packedLight, packedOverlay);
	}

	public static void renderModel(BakedModel bakedModel, BlockState blockState, BlockRenderDispatcher blockRenderer, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		int color = Minecraft.getInstance().getBlockColors().getColor(blockState, null, null, 0);
		float r = (float) (color >> 16 & 0xFF) / 255.0F;
		float g = (float) (color >> 8 & 0xFF) / 255.0F;
		float b = (float) (color & 0xFF) / 255.0F;
		blockRenderer.getModelRenderer()
			.renderModel(
				stack.last(),
				buffer.getBuffer(RenderType.cutout()),
				blockState,
				bakedModel,
				r, g, b,
				packedLight,
				packedOverlay
			);
	}

	public void renderContents(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
	}

	public static class MasonJarRenderer extends JarRenderer<MasonJarBlockEntity> {
		protected final ItemRenderer itemRenderer;

		public MasonJarRenderer(BlockEntityRendererProvider.Context context) {
			super(context);
			this.itemRenderer = context.getItemRenderer();
		}

		@Override
		public void renderContents(MasonJarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
			ItemStack stack = blockEntity.getItemHandler().getItem();
			if (stack.isEmpty()) {
				return;
			}
			poseStack.pushPose();
			poseStack.translate(0.5D, 0.4375D, 0.5D);
			poseStack.mulPose(Axis.YN.rotationDegrees(RotationSegment.convertToDegrees(blockEntity.getItemRotation())));
			poseStack.scale(0.5F, 0.5F, 0.5F);
			this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
			poseStack.popPose();
		}
	}
}
