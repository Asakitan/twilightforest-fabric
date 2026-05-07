package twilightforest.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.init.TFSounds;

/**
 * Q31 simplified port of TF {@code EnderBowItem}. Original tagged each arrow
 * with persistent NBT and used a NeoForge event handler to teleport the shooter
 * on hit. The Fabric port replaces the standard arrow with a vanilla
 * {@link ThrownEnderpearl} thrown from the bow at release — the pearl's vanilla
 * teleport-on-impact behaviour gives the same "ender bow" feel without needing
 * a custom arrow entity or hit hook.
 *
 * <p>One pearl per draw, scaled by draw power. Costs 1 arrow (or no ammo with
 * Infinity / creative) and 1 durability per shot.</p>
 */
public class EnderBowItem extends CodexBowItem {

    public EnderBowItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) return;
        ItemStack arrowStack = player.getProjectile(stack);
        boolean infinite = player.getAbilities().instabuild
                || (arrowStack.is(Items.ARROW) && player.getAbilities().instabuild);
        if (arrowStack.isEmpty() && !infinite) return;
        int useTicks = this.getUseDuration(stack, player) - timeLeft;
        if (useTicks < 0) return;
        float power = getPowerForTime(useTicks);
        if (power < 0.1F) return;

        if (level instanceof ServerLevel serverLevel) {
            ThrownEnderpearl pearl = new ThrownEnderpearl(serverLevel, player);
            pearl.setItem(new ItemStack(Items.ENDER_PEARL));
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
            serverLevel.addFreshEntity(pearl);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    TFSounds.ENDER_BOW_FIRED, SoundSource.PLAYERS, 1.0F,
                    1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

            if (!player.getAbilities().instabuild) {
                arrowStack.shrink(1);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
