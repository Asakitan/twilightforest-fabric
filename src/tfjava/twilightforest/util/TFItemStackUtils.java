package twilightforest.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class TFItemStackUtils {
	private TFItemStackUtils() {
	}

	public static boolean hasInfoTag(ItemStack stack, String key) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData != null && customData.contains(key);
	}

	public static void addInfoTag(ItemStack stack, String key) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = customData == null ? new CompoundTag() : customData.copyTag();
		tag.putBoolean(key, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void clearInfoTag(ItemStack stack, String key) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return;
		}
		CompoundTag tag = customData.copyTag();
		tag.remove(key);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
