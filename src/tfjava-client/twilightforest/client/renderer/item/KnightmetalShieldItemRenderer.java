package twilightforest.client.renderer.item;

import com.codex.twilight.client.render.CodexModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.KnightmetalShieldModel;
import twilightforest.init.TFItems;

public final class KnightmetalShieldItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/KnightmetalShieldItemRenderer");
	private KnightmetalShieldModel shield;
	private boolean warnedMissingModel;

	private KnightmetalShieldItemRenderer() {
	}

	public static void bootstrap() {
		BuiltinItemRendererRegistry.INSTANCE.register(TFItems.KNIGHTMETAL_SHIELD.get(), new KnightmetalShieldItemRenderer());
	}

	@Override
	public void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
		KnightmetalShieldModel shieldModel = this.shield();
		if (shieldModel == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		Material material = new Material(Sheets.SHIELD_SHEET, TwilightForestMod.prefix("entity/knightmetal_shield"));
		VertexConsumer consumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(bufferSource, shieldModel.renderType(material.atlasLocation()), true, stack.hasFoil()));
		shieldModel.renderToBuffer(poseStack, consumer, light, overlay);
		poseStack.popPose();
	}

	private KnightmetalShieldModel shield() {
		if (this.shield != null) {
			return this.shield;
		}
		try {
			this.shield = new KnightmetalShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(CodexModelLayers.KNIGHTMETAL_SHIELD));
			return this.shield;
		} catch (IllegalArgumentException exception) {
			if (!this.warnedMissingModel) {
				this.warnedMissingModel = true;
				LOGGER.warn("Knightmetal shield model layer {} is not ready; item rendering will retry after the model set is available.", CodexModelLayers.KNIGHTMETAL_SHIELD, exception);
			}
			return null;
		}
	}
}
