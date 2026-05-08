package twilightforest.util;

import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import twilightforest.block.CodexSixWayBlock;

/**
 * Utility class for Huge Mushroom blocks. Contains presets
 */
public class HugeMushroomUtil {
	public static BlockState getState(HugeMushroomType type, BlockState base) {
		BlockState state = base;
		state = setValueIfPresent(state, HugeMushroomBlock.UP, type.top);
		state = setValueIfPresent(state, HugeMushroomBlock.DOWN, type.bottom);
		state = setValueIfPresent(state, HugeMushroomBlock.NORTH, type.north);
		state = setValueIfPresent(state, HugeMushroomBlock.SOUTH, type.south);
		state = setValueIfPresent(state, HugeMushroomBlock.EAST, type.east);
		state = setValueIfPresent(state, HugeMushroomBlock.WEST, type.west);
		state = setValueIfPresent(state, CodexSixWayBlock.UP, type.top);
		state = setValueIfPresent(state, CodexSixWayBlock.DOWN, type.bottom);
		state = setValueIfPresent(state, CodexSixWayBlock.NORTH, type.north);
		state = setValueIfPresent(state, CodexSixWayBlock.SOUTH, type.south);
		state = setValueIfPresent(state, CodexSixWayBlock.EAST, type.east);
		return setValueIfPresent(state, CodexSixWayBlock.WEST, type.west);
	}

	private static BlockState setValueIfPresent(BlockState state, BooleanProperty property, boolean value) {
		return state.hasProperty(property) ? state.setValue(property, value) : state;
	}

	public enum HugeMushroomType {
		CENTER(true, false, false, false, false, false),
		NORTH(true, false, true, false, false, false),
		SOUTH(true, false, false, true, false, false),
		EAST(true, false, false, false, true, false),
		WEST(true, false, false, false, false, true),
		NORTH_WEST(true, false, true, false, false, true),
		NORTH_EAST(true, false, true, false, true, false),
		SOUTH_WEST(true, false, false, true, false, true),
		SOUTH_EAST(true, false, false, true, true, false);

		private final boolean top;
		private final boolean bottom;
		private final boolean north;
		private final boolean south;
		private final boolean east;
		private final boolean west;

		HugeMushroomType(boolean t, boolean b, boolean n, boolean s, boolean e, boolean w) {
			this.top = t;
			this.bottom = b;
			this.north = n;
			this.south = s;
			this.east = e;
			this.west = w;
		}
	}
}
