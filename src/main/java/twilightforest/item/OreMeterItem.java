package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import twilightforest.init.TFSounds;

import java.util.HashMap;
import java.util.Map;

/**
 * Q37 simplified port of TF {@code OreMeterItem}.
 *
 * <p>TF original is a stateful 50-tick scan that stores chunked results in a
 * custom {@code OreScannerComponent} data component, supports range toggle, and
 * renders a tooltip readout. The Fabric port collapses this into an *instant
 * synchronous scan* of the current chunk + 8 neighbouring chunks (3×3 around the
 * player), counting blocks per ore tag and printing a multi-line chat readout.
 * No DataComponents, no progress bar, no per-stack persistence.</p>
 *
 * <p>Cost: 1 durability per use, 100-tick cooldown.</p>
 */
public class OreMeterItem extends CodexItem {

    public OreMeterItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        ServerLevel serverLevel = (ServerLevel) level;
        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;

        Map<String, Integer> counts = new HashMap<>();
        int blocksScanned = 0;

        for (int cx = chunkX - 1; cx <= chunkX + 1; cx++) {
            for (int cz = chunkZ - 1; cz <= chunkZ + 1; cz++) {
                LevelChunk chunk = serverLevel.getChunk(cx, cz);
                int minY = serverLevel.getMinBuildHeight();
                int maxY = serverLevel.getMaxBuildHeight();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = minY; y < maxY; y++) {
                            BlockState state = chunk.getBlockState(new BlockPos(cx * 16 + x, y, cz * 16 + z));
                            if (state.isAir()) continue;
                            blocksScanned++;
                            categorise(state, counts);
                        }
                    }
                }
            }
        }

        player.displayClientMessage(
            Component.translatable("message.codex_twilight.ore_meter.header", blocksScanned).withStyle(ChatFormatting.GOLD), false);
        if (counts.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.codex_twilight.ore_meter.no_ores").withStyle(ChatFormatting.GRAY), false);
        } else {
            counts.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .forEach(entry -> player.displayClientMessage(
                    Component.translatable("message.codex_twilight.ore_meter.entry",
                            Component.translatable("message.codex_twilight.ore_meter.category." + entry.getKey()),
                            entry.getValue())
                                    .withStyle(ChatFormatting.AQUA), false));
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                TFSounds.ORE_METER_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (player instanceof net.minecraft.server.level.ServerPlayer sp && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, serverLevel, sp, removed -> {});
        }
        player.getCooldowns().addCooldown(this, 100);
        return InteractionResultHolder.success(stack);
    }

    private static void categorise(BlockState state, Map<String, Integer> counts) {
        addIfMatches(state, BlockTags.COAL_ORES, "coal_ores", counts);
        addIfMatches(state, BlockTags.IRON_ORES, "iron_ores", counts);
        addIfMatches(state, BlockTags.GOLD_ORES, "gold_ores", counts);
        addIfMatches(state, BlockTags.DIAMOND_ORES, "diamond_ores", counts);
        addIfMatches(state, BlockTags.EMERALD_ORES, "emerald_ores", counts);
        addIfMatches(state, BlockTags.LAPIS_ORES, "lapis_ores", counts);
        addIfMatches(state, BlockTags.REDSTONE_ORES, "redstone_ores", counts);
        addIfMatches(state, BlockTags.COPPER_ORES, "copper_ores", counts);
    }

    private static void addIfMatches(BlockState state, TagKey<Block> tag, String key, Map<String, Integer> counts) {
        if (state.is(tag)) counts.merge(key, 1, Integer::sum);
    }
}
