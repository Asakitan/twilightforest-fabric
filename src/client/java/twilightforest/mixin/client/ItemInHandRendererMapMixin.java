package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.item.MagicMapItem;
import twilightforest.item.MazeMapItem;
import twilightforest.item.mapdata.TFMagicMapData;
import twilightforest.item.mapdata.TFMazeMapData;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMapMixin {
	@Unique
	private ItemStack twilightforest$renderingMapStack = ItemStack.EMPTY;

	@Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
	private boolean twilightforest$renderCustomFilledMapsAsMaps(ItemStack stack, Item item) {
		return stack.is(item) || stack.getItem() instanceof MagicMapItem || stack.getItem() instanceof MazeMapItem;
	}

	@Inject(method = "renderMap", at = @At("HEAD"))
	private void twilightforest$captureMapStack(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack, CallbackInfo ci) {
		this.twilightforest$renderingMapStack = stack;
	}

	@Redirect(method = "renderMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
	private MapItemSavedData twilightforest$getCustomMapDataForFirstPerson(MapId mapId, Level level) {
		if (mapId != null) {
			if (this.twilightforest$renderingMapStack.getItem() instanceof MagicMapItem) {
				return TFMagicMapData.getClientMagicMapData(MagicMapItem.getMapName(mapId.id()));
			}
			if (this.twilightforest$renderingMapStack.getItem() instanceof MazeMapItem) {
				return TFMazeMapData.getClientMagicMapData(MazeMapItem.getMapName(mapId.id()));
			}
		}
		return MapItem.getSavedData(mapId, level);
	}

	@Inject(method = "renderMap", at = @At("RETURN"))
	private void twilightforest$clearMapStack(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack, CallbackInfo ci) {
		this.twilightforest$renderingMapStack = ItemStack.EMPTY;
	}
}
