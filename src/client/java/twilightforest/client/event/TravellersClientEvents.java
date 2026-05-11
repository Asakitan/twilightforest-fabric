package twilightforest.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFKeyBinds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.network.CycleMapSlotPacket;
import twilightforest.network.GogglesZoomPacket;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.PerformDoubleJumpPacket;
import twilightforest.network.PerformSidestepPacket;
import twilightforest.network.SwapHotbarPacket;

public final class TravellersClientEvents {
	private static boolean wasJumpKeyDown;

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
				TFDataAttachments.set(player, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION, !TFDataAttachments.get(player, TFDataAttachments.TRAVELLERS_GOGGLES_RED_THREAD_VISION));
			}
		}
		handleDoubleJump(client, player);
		handleSidestep(player);
		updateZoom(player);
		updateGradualGlide(player);
	}

	private static void handleDoubleJump(Minecraft client, LocalPlayer player) {
		boolean jumpKeyDown = client.options.keyJump.isDown();
		if (jumpKeyDown && !wasJumpKeyDown) {
			int lastJumpKeyPressTime = TFDataAttachments.get(player, TFDataAttachments.LAST_JUMP_KEY_PRESS_TIME);
			TFDataAttachments.set(player, TFDataAttachments.LAST_JUMP_KEY_PRESS_TIME, player.tickCount);
			boolean avoidCreativeFly = player.getAbilities().mayfly && player.tickCount - lastJumpKeyPressTime <= 6;
			if (!avoidCreativeFly && TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.DOUBLE_JUMP_MODIFIER) && TravellersGearLogic.performDoubleJump(player)) {
				ClientPlayNetworking.send(PerformDoubleJumpPacket.INSTANCE);
			}
		}
		wasJumpKeyDown = jumpKeyDown;
	}

	private static void handleSidestep(LocalPlayer player) {
		if (!player.onGround()) {
			return;
		}
		Input input = player.input;
		float leftImpulse = input.leftImpulse;
		boolean lastImpulseZero = TFDataAttachments.get(player, TFDataAttachments.LAST_HORIZONTAL_IMPULSE) == 0.0F;
		boolean sameImpulseDirection = Math.signum(TFDataAttachments.get(player, TFDataAttachments.LAST_NON_ZERO_HORIZONTAL_IMPULSE)) == Math.signum(leftImpulse);
		int currentTime = player.tickCount;
		int lastWalkingTime = TFDataAttachments.get(player, TFDataAttachments.LAST_HORIZONTAL_WALKING_TIME);
		boolean hasDoubleTapped = currentTime - lastWalkingTime < 4;

		if (lastImpulseZero && sameImpulseDirection && hasDoubleTapped && leftImpulse != 0.0F) {
			boolean leftSidestep = leftImpulse > 0.0F;
			if (TravellersGearLogic.tryPerformSidestep(player, leftSidestep)) {
				ClientPlayNetworking.send(new PerformSidestepPacket(leftSidestep));
			}
		}

		TFDataAttachments.set(player, TFDataAttachments.LAST_HORIZONTAL_IMPULSE, leftImpulse);
		if (leftImpulse != 0.0F) {
			TFDataAttachments.set(player, TFDataAttachments.LAST_HORIZONTAL_WALKING_TIME, currentTime);
			TFDataAttachments.set(player, TFDataAttachments.LAST_NON_ZERO_HORIZONTAL_IMPULSE, leftImpulse);
		}
	}

	private static void updateZoom(LocalPlayer player) {
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		Float zoomModifier = headStack.get(twilightforest.init.TFDataComponents.ZOOM_ABILITY_MODIFIER);
		boolean usingZoom = TFKeyBinds.ZOOM_KEY.isDown() && !player.isScoping() && zoomModifier != null && TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY);
		if (usingZoom != TFDataAttachments.get(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER)) {
			TFDataAttachments.set(player, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER, usingZoom);
			ClientPlayNetworking.send(new GogglesZoomPacket(usingZoom, player.getUUID()));
		}
	}

	private static void updateGradualGlide(LocalPlayer player) {
		boolean gradual = TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER)
			&& twilightforest.config.TFConfig.manualTravellersWingsGradualGlideDefault == player.isShiftKeyDown()
			&& player.getKnownMovement().y() < 0.0D
			&& !player.onGround();
		if (gradual != TFDataAttachments.get(player, TFDataAttachments.IS_GRADUALLY_GLIDING)) {
			TFDataAttachments.set(player, TFDataAttachments.IS_GRADUALLY_GLIDING, gradual);
			ClientPlayNetworking.send(new GradualGlidePacket(gradual, player.getUUID()));
		}
	}
}
