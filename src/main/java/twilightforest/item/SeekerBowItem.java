package twilightforest.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Q31 simplified port of TF {@code SeekerBowItem}. Original spawns a custom
 * {@code SeekerArrow} entity that homes onto the nearest target. Fabric port
 * has no SeekerArrow projectile entity registered, so we tag the standard
 * vanilla arrow with extra base damage and a 1.5× momentum boost at spawn so
 * it flies "true and fast" — visually a vanilla arrow but harder to dodge.
 */
public class SeekerBowItem extends CodexBowItem {

    public SeekerBowItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        Projectile projectile = super.createProjectile(level, shooter, weapon, ammo, isCrit);
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() + 1.5D);
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(1.5D));
        }
        return projectile;
    }
}
