package twilightforest.init;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.block.entity.TrophyBlockEntity;
import twilightforest.components.block.ChiseledCanopyBookshelfWrapper;

/**
 * Q33 minimal {@link BlockEntityType} registry. Currently only carries the
 * {@link MasonJarBlockEntity} type. Add new types here as paired-client-block-entities
 * land. Uses Fabric's {@code FabricBlockEntityTypeBuilder} to dodge the
 * Mojang-mappings access restriction on {@code BlockEntitySupplier}.
 */
public final class TFBlockEntities {

    public static final BlockEntityType<MasonJarBlockEntity> MASON_JAR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("mason_jar"),
            FabricBlockEntityTypeBuilder.create(MasonJarBlockEntity::new, TFBlocks.MASON_JAR.get()).build());

    public static final BlockEntityType<DryingRackBlockEntity> DRYING_RACK = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("drying_rack"),
            FabricBlockEntityTypeBuilder.create(DryingRackBlockEntity::new,
                    TFBlocks.OAK_DRYING_RACK.get(),
                    TFBlocks.SPRUCE_DRYING_RACK.get(),
                    TFBlocks.BIRCH_DRYING_RACK.get(),
                    TFBlocks.JUNGLE_DRYING_RACK.get(),
                    TFBlocks.ACACIA_DRYING_RACK.get(),
                    TFBlocks.DARK_OAK_DRYING_RACK.get(),
                    TFBlocks.MANGROVE_DRYING_RACK.get(),
                    TFBlocks.CHERRY_DRYING_RACK.get(),
                    TFBlocks.BAMBOO_DRYING_RACK.get(),
                    TFBlocks.CRIMSON_DRYING_RACK.get(),
                    TFBlocks.WARPED_DRYING_RACK.get(),
                    TFBlocks.TWILIGHT_OAK_DRYING_RACK.get(),
                    TFBlocks.CANOPY_DRYING_RACK.get(),
                    TFBlocks.DARK_DRYING_RACK.get())
                    .build());

    public static final BlockEntityType<ChiseledCanopyShelfBlockEntity> CHISELED_CANOPY_BOOKSHELF = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("chiseled_canopy_bookshelf"),
            FabricBlockEntityTypeBuilder.create(ChiseledCanopyShelfBlockEntity::new,
                    TFBlocks.CHISELED_CANOPY_BOOKSHELF.get())
                    .build());

    public static final BlockEntityType<TrophyBlockEntity> TROPHY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("trophy"),
            FabricBlockEntityTypeBuilder.create(TrophyBlockEntity::new,
                    TFBlocks.NAGA_TROPHY.get(),
                    TFBlocks.LICH_TROPHY.get(),
                    TFBlocks.MINOSHROOM_TROPHY.get(),
                    TFBlocks.HYDRA_TROPHY.get(),
                    TFBlocks.KNIGHT_PHANTOM_TROPHY.get(),
                    TFBlocks.UR_GHAST_TROPHY.get(),
                    TFBlocks.ALPHA_YETI_TROPHY.get(),
                    TFBlocks.SNOW_QUEEN_TROPHY.get(),
                    TFBlocks.QUEST_RAM_TROPHY.get(),
                    TFBlocks.NAGA_WALL_TROPHY.get(),
                    TFBlocks.LICH_WALL_TROPHY.get(),
                    TFBlocks.MINOSHROOM_WALL_TROPHY.get(),
                    TFBlocks.HYDRA_WALL_TROPHY.get(),
                    TFBlocks.KNIGHT_PHANTOM_WALL_TROPHY.get(),
                    TFBlocks.UR_GHAST_WALL_TROPHY.get(),
                    TFBlocks.ALPHA_YETI_WALL_TROPHY.get(),
                    TFBlocks.SNOW_QUEEN_WALL_TROPHY.get(),
                    TFBlocks.QUEST_RAM_WALL_TROPHY.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.FireJetBlockEntity> FLAME_JET = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("flame_jet"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.FireJetBlockEntity::new,
                    TFBlocks.FIRE_JET.get(),
                    TFBlocks.ENCASED_FIRE_JET.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.TFSmokerBlockEntity> SMOKER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("smoker"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.TFSmokerBlockEntity::new,
                    TFBlocks.SMOKER.get(),
                    TFBlocks.ENCASED_SMOKER.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.CandelabraBlockEntity> CANDELABRA = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("candelabra"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.CandelabraBlockEntity::new,
                    TFBlocks.CANDELABRA.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.BrazierBlockEntity> BRAZIER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("brazier"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.BrazierBlockEntity::new,
                    TFBlocks.BRAZIER.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.CarminiteReactorBlockEntity> CARMINITE_REACTOR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("carminite_reactor"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.CarminiteReactorBlockEntity::new,
                    TFBlocks.CARMINITE_REACTOR.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.ReactorDebrisBlockEntity> REACTOR_DEBRIS = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("reactor_debris"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.ReactorDebrisBlockEntity::new,
                    TFBlocks.REACTOR_DEBRIS.get())
                    .build());

    public static final BlockEntityType<twilightforest.block.entity.SkullCandleBlockEntity> SKULL_CANDLE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            TwilightForestMod.prefix("skull_candle"),
            FabricBlockEntityTypeBuilder.create(twilightforest.block.entity.SkullCandleBlockEntity::new,
                    TFBlocks.CREEPER_SKULL_CANDLE.get(),
                    TFBlocks.CREEPER_WALL_SKULL_CANDLE.get(),
                    TFBlocks.SKELETON_SKULL_CANDLE.get(),
                    TFBlocks.SKELETON_WALL_SKULL_CANDLE.get(),
                    TFBlocks.WITHER_SKELETON_SKULL_CANDLE.get(),
                    TFBlocks.WITHER_SKELETON_WALL_SKULL_CANDLE.get(),
                    TFBlocks.ZOMBIE_SKULL_CANDLE.get(),
                    TFBlocks.ZOMBIE_WALL_SKULL_CANDLE.get(),
                    TFBlocks.PLAYER_SKULL_CANDLE.get(),
                    TFBlocks.PLAYER_WALL_SKULL_CANDLE.get(),
                    TFBlocks.PIGLIN_SKULL_CANDLE.get(),
                    TFBlocks.PIGLIN_WALL_SKULL_CANDLE.get())
                    .build());

    private TFBlockEntities() {}

    public static void bootstrap() {
        includeVanillaSpawnerBlocks(
                TFBlocks.ALPHA_YETI_BOSS_SPAWNER.get(),
                TFBlocks.HYDRA_BOSS_SPAWNER.get(),
                TFBlocks.KNIGHT_PHANTOM_BOSS_SPAWNER.get(),
                TFBlocks.LICH_BOSS_SPAWNER.get(),
                TFBlocks.MINOSHROOM_BOSS_SPAWNER.get(),
                TFBlocks.NAGA_BOSS_SPAWNER.get(),
                TFBlocks.SNOW_QUEEN_BOSS_SPAWNER.get(),
                TFBlocks.UR_GHAST_BOSS_SPAWNER.get(),
                TFBlocks.FINAL_BOSS_BOSS_SPAWNER.get(),
                TFBlocks.NAGA_SPAWNER.get(),
                TFBlocks.LICH_SPAWNER.get(),
                TFBlocks.SINISTER_SPAWNER.get());
        ChiseledCanopyBookshelfWrapper.register();
    }

    private static void includeVanillaSpawnerBlocks(Block... blocks) {
        Set<Block> validBlocks = new HashSet<>(BlockEntityType.MOB_SPAWNER.validBlocks);
        boolean changed = false;
        for (Block block : blocks) {
            changed |= validBlocks.add(block);
        }
        if (changed) {
            BlockEntityType.MOB_SPAWNER.validBlocks = Set.copyOf(validBlocks);
        }
    }
}
