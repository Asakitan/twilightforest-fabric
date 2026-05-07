package twilightforest.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Q32 simplified port of TF traveller-armor pieces. The original suite has
 * deep custom logic per piece (bundle-style goggles inventory, map ticking,
 * modifier manager, ender mask check, etc.). Rather than port the whole
 * {@code travellers_gear} package + dynamic registry, Q32 ships a thin layer
 * that grants a passive {@link MobEffect} while the piece is worn in its
 * canonical slot:
 *
 * <ul>
 *   <li>Goggles  → {@code MobEffects.NIGHT_VISION} (HEAD)</li>
 *   <li>Vest     → {@code MobEffects.DAMAGE_RESISTANCE} I (CHEST)</li>
 *   <li>Gloves   → {@code MobEffects.DIG_SPEED} I (CHEST, second slot — see TFItems)</li>
 *   <li>Wings    → {@code MobEffects.SLOW_FALLING} (LEGS, applied only mid-air)</li>
 *   <li>Belt     → {@code MobEffects.SATURATION} (LEGS, second slot)</li>
 *   <li>Boots    → {@code MobEffects.MOVEMENT_SPEED} I (FEET)</li>
 * </ul>
 *
 * <p>The buff renews every 40 ticks while equipped; setting the effect
 * {@code visible=false} keeps the player's HUD clean (no swirl), {@code
 * showIcon=true} still shows it in the inventory effect bar.</p>
 */
public class TravellersArmorPieceItem extends CodexArmorItem {

    private final Holder<MobEffect> wornEffect;
    private final int amplifier;
    private final EquipmentSlot expectedSlot;
    private final boolean onlyInAir;

    public TravellersArmorPieceItem(Holder<ArmorMaterial> material, ArmorItem.Type type,
                                    Properties properties, Item fallback, int cmd,
                                    Holder<MobEffect> wornEffect, int amplifier,
                                    EquipmentSlot expectedSlot, boolean onlyInAir) {
        super(material, type, properties, fallback, cmd);
        this.wornEffect = wornEffect;
        this.amplifier = amplifier;
        this.expectedSlot = expectedSlot;
        this.onlyInAir = onlyInAir;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        int targetSlot = Inventory.INVENTORY_SIZE + this.expectedSlot.getIndex();
        if (slotId != targetSlot) return;
        if (player.tickCount % 40 != 0) return;
        if (this.onlyInAir && (player.onGround() || player.getDeltaMovement().y >= 0.0D)) return;
        player.addEffect(new MobEffectInstance(this.wornEffect, 100, this.amplifier, true, false, true));
    }
}
