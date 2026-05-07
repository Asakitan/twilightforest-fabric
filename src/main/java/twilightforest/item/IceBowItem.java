package twilightforest.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Q31 simplified port of TF {@code IceBowItem}. Original spawns a custom
 * {@code IceArrow} entity that freezes the target on impact. Fabric port
 * pre-bakes Slowness II + Mining Fatigue II into the standard vanilla arrow
 * via tipped-arrow style {@link Arrow#addEffect(MobEffectInstance)} so the
 * "icy chill" applies on hit without a custom projectile entity.
 */
public class IceBowItem extends CodexBowItem {

    public IceBowItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        Projectile projectile = super.createProjectile(level, shooter, weapon, ammo, isCrit);
        if (projectile instanceof Arrow tipped) {
            tipped.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            tipped.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 1));
        }
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() + 0.5D);
        }
        return projectile;
    }
}
