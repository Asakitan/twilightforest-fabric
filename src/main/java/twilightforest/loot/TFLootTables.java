package twilightforest.loot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import twilightforest.TwilightForestMod;

public final class TFLootTables {
    public static final int DEFAULT_PLACE_FLAG = Block.UPDATE_CLIENTS;

    public static final ResourceKey<LootTable> AURORA_CACHE = register("aurora_cache");
    public static final ResourceKey<LootTable> AURORA_ROOM = register("aurora_room");
    public static final ResourceKey<LootTable> BASEMENT = register("chests/basement");
    public static final ResourceKey<LootTable> DARKTOWER_CACHE = register("darktower_cache");
    public static final ResourceKey<LootTable> DARKTOWER_KEY = register("darktower_key");
    public static final ResourceKey<LootTable> FALLEN_TRUNK_LOOT = register("fallen_trunk_loot");
    public static final ResourceKey<LootTable> FANCY_WELL = register("fancy_well");
    public static final ResourceKey<LootTable> FOUNDATION_BASEMENT = register("foundation_basement");
    public static final ResourceKey<LootTable> GRAVEYARD = register("graveyard");
    public static final ResourceKey<LootTable> HEDGE_CLOTH = register("hedge_cloth");
    public static final ResourceKey<LootTable> HEDGE_MAZE = register("hedge_maze");
    public static final ResourceKey<LootTable> HUT_JUNK = register("chests/hut_junk");
    public static final ResourceKey<LootTable> LABYRINTH_DEAD_END = register("labyrinth_dead_end");
    public static final ResourceKey<LootTable> LABYRINTH_ROOM = register("labyrinth_room");
    public static final ResourceKey<LootTable> LABYRINTH_VAULT = register("labyrinth_vault");
    public static final ResourceKey<LootTable> LABYRINTH_VAULT_JACKPOT = register("labyrinth_vault_jackpot");
    public static final ResourceKey<LootTable> LARGE_HOLLOW_HILL = register("hill_3");
    public static final ResourceKey<LootTable> MEDIUM_HOLLOW_HILL = register("hill_2");
    public static final ResourceKey<LootTable> QUEST_GROVE = register("quest_grove");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_WHITE = register("entities/bighorn_sheep/white");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_ORANGE = register("entities/bighorn_sheep/orange");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_MAGENTA = register("entities/bighorn_sheep/magenta");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_LIGHT_BLUE = register("entities/bighorn_sheep/light_blue");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_YELLOW = register("entities/bighorn_sheep/yellow");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_LIME = register("entities/bighorn_sheep/lime");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_PINK = register("entities/bighorn_sheep/pink");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_GRAY = register("entities/bighorn_sheep/gray");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_LIGHT_GRAY = register("entities/bighorn_sheep/light_gray");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_CYAN = register("entities/bighorn_sheep/cyan");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_PURPLE = register("entities/bighorn_sheep/purple");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_BLUE = register("entities/bighorn_sheep/blue");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_BROWN = register("entities/bighorn_sheep/brown");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_GREEN = register("entities/bighorn_sheep/green");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_RED = register("entities/bighorn_sheep/red");
    public static final ResourceKey<LootTable> BIGHORN_SHEEP_BLACK = register("entities/bighorn_sheep/black");
    public static final ResourceKey<LootTable> QUESTING_RAM_REWARDS = register("entities/questing_ram_rewards");
    public static final ResourceKey<LootTable> SMALL_HOLLOW_HILL = register("hill_1");
    public static final ResourceKey<LootTable> STRONGHOLD_CACHE = register("stronghold_cache");
    public static final ResourceKey<LootTable> STRONGHOLD_ROOM = register("stronghold_room");
    public static final ResourceKey<LootTable> TOWER_LIBRARY = register("chests/tower_library");
    public static final ResourceKey<LootTable> TOWER_ROOM = register("chests/tower_room");
    public static final ResourceKey<LootTable> TREE_CACHE = register("tree_cache");
    public static final ResourceKey<LootTable> TROLL_GARDEN = register("troll_garden");
    public static final ResourceKey<LootTable> TROLL_VAULT = register("troll_vault");
    public static final ResourceKey<LootTable> TROLL_VAULT_WITH_LAMP = register("troll_vault_with_lamp");
    public static final ResourceKey<LootTable> WELL = register("well");

    private TFLootTables() {
    }

    public static void generateChest(WorldGenLevel world, BlockPos pos, Direction dir, boolean trapped, ResourceKey<LootTable> lootTable) {
        generateLootContainer(world, pos, (trapped ? Blocks.TRAPPED_CHEST : Blocks.CHEST).defaultBlockState().setValue(ChestBlock.FACING, dir), DEFAULT_PLACE_FLAG, lootTable);
    }

    public static void generateLootContainer(WorldGenLevel world, BlockPos pos, BlockState state, int flags, ResourceKey<LootTable> lootTable) {
        world.setBlock(pos, state, flags);
        generateChestContents(world, pos, lootTable);
    }

    public static void generateChestContents(WorldGenLevel level, BlockPos pos, ResourceKey<LootTable> lootTable) {
        generateChestContents(level, pos, level.getSeed() * pos.getX() + pos.getY() ^ pos.getZ(), lootTable);
    }

    public static void generateChestContents(LevelAccessor level, BlockPos pos, long seed, ResourceKey<LootTable> lootTable) {
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity lootContainer) {
            lootContainer.setLootTable(lootTable, seed);
        }
    }

    private static ResourceKey<LootTable> register(String id) {
        return ResourceKey.create(Registries.LOOT_TABLE, TwilightForestMod.prefix(id));
    }
}
