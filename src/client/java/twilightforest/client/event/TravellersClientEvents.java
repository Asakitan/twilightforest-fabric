package twilightforest.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFKeyBinds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.network.CycleMapSlotPacket;
import twilightforest.network.GogglesZoomPacket;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.SwapHotbarPacket;

public final class TravellersClientEvents {
	private TravellersClientEvents() {
	}

	public static void bootstrap() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
	}

	private static void tick(Minecraft client) {
		if (!(client.player instanceof LocalPlayer player)) {
			return;
		}
		while (TFKeyBinds.ITEM_DISPLAY_MAP_CYCLE_KEY.consumeClick()) {
			ClientPlayNetworking.send(CycleMapSlotPacket.INSTANCE);
		}
		while (TFKeyBinds.SWAP_HOTBAR_KEY.consumeClick()) {
			ClientPlayNetworking.send(SwapHotbarPacket.INSTANCE);
		}
		while (TFKeyBinds.RED_THREAD_VISION_KEY.consumeClick()) {
			if (TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.RED_THREAD_VISION_MODIFIER)) {
				player.setData(TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION, !player.getData(TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION));
			}
		}
		updateZoom(player);
		updateGradualGlide(player);
	}

	private static void updateZoom(LocalPlayer player) {
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		Float zoomModifier = headStack.get(twilightforest.init.TFDataComponents.ZOOM_ABILITY_MODIFIER);
		boolean usingZoom = TFKeyBinds.ZOOM_KEY.isDown() && !player.isScoping() && zoomModifier != null && TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY);
		if (usingZoom != player.getData(TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER)) {
			player.setData(TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER, usingZoom);
			ClientPlayNetworking.send(new GogglesZoomPacket(usingZoom, player.getUUID()));
		}
	}

	private static void updateGradualGlide(LocalPlayer player) {
		boolean gradual = twilightforest.config.TFConfig.manualTravellersWingsGradualGlideDefault == player.isShiftKeyDown() && player.getKnownMovement().y() < 0.0D && !player.onGround();
		if (gradual != player.getData(TFDataAttachments.IS_GRADUALLY_GLIDING)) {
			player.setData(TFDataAttachments.IS_GRADUALLY_GLIDING, gradual);
			ClientPlayNetworking.send(new GradualGlidePacket(gradual, player.getUUID()));
		}
	}
}
