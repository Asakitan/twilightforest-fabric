package twilightforest.init;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.MasonJarBlockEntity;

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
