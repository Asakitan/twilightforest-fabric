package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import twilightforest.init.TFSounds;

/**
 * Q29 simplified port of EmptyMagicMapItem / MagicMapItem fusion: right-click
 * creates a vanilla filled map centered on the player's chunk, scaled at zoom 2
 * to cover ~256 blocks. The original TF version draws TF biome colors via
 * {@code TFDataMaps.MAGIC_MAP_BIOME_COLOR} — that requires NeoForge data-map
 * infrastructure not available on Fabric, so Q29 ships a vanilla-color version
 * that the player can use to scout TF biomes the same way they would the
 * Overworld.
 */
public class MagicMapItem extends CodexItem {

    public MagicMapItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        ServerLevel serverLevel = (ServerLevel) level;
        ItemStack filled = MapItem.create(serverLevel, player.getBlockX(), player.getBlockZ(), (byte) 2, true, false);
        MapItem.renderBiomePreviewMap(serverLevel, filled);
        if (player instanceof ServerPlayer sp) {
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    TFSounds.MAGIC_MAP_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            return InteractionResultHolder.success(filled);
        }
        if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
        return InteractionResultHolder.success(stack);
    }
}
