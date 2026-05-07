package twilightforest.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

/**
 * 1:1 upstream trophy item shape: one item id places the standing trophy on floors
 * and the matching wall trophy on walls.
 */
public class TrophyItem extends StandingAndWallBlockItem {

	public TrophyItem(Block floorBlock, Block wallBlock, Item.Properties properties) {
		super(floorBlock, wallBlock, properties, Direction.DOWN);
	}
}
