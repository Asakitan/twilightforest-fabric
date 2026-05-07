package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Blocks;
import twilightforest.init.TFSounds;

/**
 * Q37 simplified port of TF {@code GlassSwordItem}: a one-hit, high-damage
 * sword that shatters on use. We extend {@link CodexSwordItem} (real
 * sword stats / legacy fallback) and override {@link #hurtEnemy} to:
 *   <ul>
 *     <li>Spawn 20 white-stained-glass break particles around the target.</li>
 *     <li>Play {@link TFSounds#GLASS_SWORD_BREAK}.</li>
 *     <li>Force the stack to break by setting damage = maxDamage.</li>
 *   </ul>
 *
 * <p>Skipped: TF's {@code TFDataComponents.INFINITE_GLASS_SWORD} bypass component
 * (creative-mode ignores breakage anyway) and {@code TFAdvancements.BROKE_GLASS_SWORD}
 * trigger (no advancement wired).</p>
 */
public class GlassSwordItem extends CodexSwordItem {

    private static final BlockParticleOption GLASS_PARTICLE =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

    public GlassSwordItem(Tier tier, Properties properties, Item fallback, int cmd) {
        super(tier, properties, fallback, cmd);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel sl) {
            for (int i = 0; i < 20; i++) {
                double dx = target.getX() + target.getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth();
                double dy = target.getY() + target.getRandom().nextFloat() * target.getBbHeight();
                double dz = target.getZ() + target.getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth();
                sl.sendParticles(GLASS_PARTICLE, dx, dy, dz, 1, 0, 0, 0, 0);
            }
            sl.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    TFSounds.GLASS_SWORD_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);

            if (!(attacker instanceof ServerPlayer sp) || !sp.getAbilities().instabuild) {
                stack.setDamageValue(stack.getMaxDamage());
                stack.shrink(1);
            }
        }
        return true;
    }
}
