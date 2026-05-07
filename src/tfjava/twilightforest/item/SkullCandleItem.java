package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.components.item.SkullCandles;
import twilightforest.init.TFDataComponents;

import java.util.List;
import java.util.Locale;

public class SkullCandleItem extends StandingAndWallBlockItem implements Equipable {
	public SkullCandleItem(AbstractSkullCandleBlock floor, AbstractSkullCandleBlock wall, Properties properties) {
		super(floor, wall, properties, Direction.DOWN);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		SkullCandles skullCandles = stack.get(TFDataComponents.SKULL_CANDLES);
		if (skullCandles != null) {
			tooltip.add(Component.translatable(skullCandles.count() > 1
					? "item.twilightforest.skull_candle.desc.multiple"
					: "item.twilightforest.skull_candle.desc",
				String.valueOf(skullCandles.count()),
				capitalizeWords(AbstractSkullCandleBlock.CandleColors.colorFromInt(skullCandles.color()).getSerializedName().replace("\"", "").replace("_", " ")))
				.withStyle(ChatFormatting.GRAY));
		}
	}

	private static String capitalizeWords(String text) {
		String[] words = text.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (!words[i].isEmpty()) {
				words[i] = words[i].substring(0, 1).toUpperCase(Locale.ROOT) + words[i].substring(1);
			}
		}
		return String.join(" ", words);
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.HEAD;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return this.swapWithEquipmentSlot(this, level, player, hand);
	}

	@Override
	public Component getName(ItemStack stack) {
		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		return profile != null && profile.name().isPresent()
			? Component.translatable(this.getDescriptionId() + ".named", profile.name().get())
			: super.getName(stack);
	}

	@Override
	public void verifyComponentsAfterLoad(ItemStack stack) {
		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		if (profile != null && !profile.isResolved()) {
			profile.resolve().thenAcceptAsync(resolved -> stack.set(DataComponents.PROFILE, resolved), SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
		}
	}
}
