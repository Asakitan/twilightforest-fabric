package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.PlateauBossModel;
import twilightforest.entity.boss.PlateauBoss;

/**
 * Renderer for {@link PlateauBoss}. Uses the upstream Lich texture as a
 * placeholder skin and renders the model at 1.4x scale. Phase distinction
 * comes from particle emission driven server-side in
 * {@link PlateauBoss#customServerAiStep()}.
 */
public class PlateauBossRenderer<T extends PlateauBoss, M extends PlateauBossModel<T>> extends HumanoidMobRenderer<T, M> {

    private static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("twilightlich64.png");
    private static final float SCALE = 1.4F;

    public PlateauBossRenderer(EntityRendererProvider.Context context, M model, float shadowSize) {
        super(context, model, shadowSize);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        stack.pushPose();
        stack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, stack, buffer, packedLight);
        stack.popPose();
    }
}
