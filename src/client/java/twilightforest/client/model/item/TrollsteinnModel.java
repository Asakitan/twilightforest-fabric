package twilightforest.client.model.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.TrollsteinnBlock;

import java.util.List;

public class TrollsteinnModel implements BakedModel {
	public static final ModelResourceLocation LIT_TROLLSTEINN = ModelResourceLocation.standalone(TwilightForestMod.prefix("item/trollsteinn_light"));

	@Nullable
	private BakedModel litTrollsteinnModel;
	private final BakedModel originalModel;
	private final ItemOverrides overrides = new ItemOverrides() {
		@Override
		public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
			if (TrollsteinnModel.this.litTrollsteinnModel == null) {
				TrollsteinnModel.this.litTrollsteinnModel = Minecraft.getInstance().getModelManager().getModel(LIT_TROLLSTEINN);
			}

			Entity itemEntity = entity == null ? stack.getEntityRepresentation() : entity;
			if (level == null || itemEntity == null) {
				return TrollsteinnModel.this.originalModel.getOverrides().resolve(TrollsteinnModel.this.originalModel, stack, level, entity, seed);
			}

			int brightness = level.getMaxLocalRawBrightness(itemEntity.blockPosition(), TrollsteinnBlock.calculateServerSkyDarken(level));
			BakedModel selected = brightness > TrollsteinnBlock.LIGHT_THRESHOLD ? TrollsteinnModel.this.litTrollsteinnModel : TrollsteinnModel.this.originalModel;
			return selected.getOverrides().resolve(selected, stack, level, entity, seed);
		}
	};

	public TrollsteinnModel(BakedModel originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
		return this.originalModel.getQuads(state, side, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.originalModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.originalModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.originalModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.originalModel.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.originalModel.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.originalModel.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.overrides;
	}
}
