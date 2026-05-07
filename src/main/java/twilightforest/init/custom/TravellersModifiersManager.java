package twilightforest.init.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.TFItems;

/**
 * Q34 simplified replacement for TF original {@code TravellersModifiersManager}.
 *
 * <p>Original drives a 24-modifier-per-piece dynamic registry with insertable /
 * transferable modifier components. Codex-twilight ships hard-coded "wear the
 * piece → get the modifier" mappings — which piece grants which behaviour is
 * baked into the static helpers below. Mixins consult these checks via the
 * {@code is*Active} methods.</p>
 */
public final class TravellersModifiersManager {

    private TravellersModifiersManager() {}

    private static boolean wears(LivingEntity entity, Item required) {
        for (ItemStack worn : entity.getArmorSlots()) {
            if (worn.is(required)) return true;
        }
        return false;
    }

    /** Wings → one bonus mid-air jump per ground reset. */
    public static boolean isDoubleJumpActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_WINGS.get());
    }

    /** Boots → walk on water (top water blocks become solid). */
    public static boolean isWaterWalkActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_BOOTS.get());
    }

    /** Boots → 1.0625-block step-up (vs vanilla 0.6). */
    public static boolean isStepUpActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_BOOTS.get());
    }

    /** Vest → 25% reduced fall damage on top of the Resistance I from Q32. */
    public static boolean isCushionActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_VEST.get());
    }

    /** Belt → faster swim speed in water. */
    public static boolean isSwiftSwimActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_BELT.get());
    }

    /** Goggles → arrows fired by the wearer subtly home to the nearest target. */
    public static boolean isArrowMagnetismActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_GOGGLES.get());
    }

    /** Vest → 12% chance to nullify an incoming hit. */
    public static boolean isPerfectDodgeActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_VEST.get());
    }

    /** Boots → fall damage fully nullified. */
    public static boolean isSlimySolesActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_BOOTS.get());
    }

    /** Goggles → nearby living entities glow. */
    public static boolean isRedThreadVisionActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_GOGGLES.get());
    }

    /** Gloves → mining underwater is unaffected by the water-mining penalty. */
    public static boolean isUnrestrainedActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_GLOVES.get());
    }

    /** Vest → worn travellers pieces auto-repair 1 durability every 5 seconds. */
    public static boolean isAutoRepairActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_VEST.get());
    }

    /** Belt → eaten food restores +1 extra hunger and +0.4 saturation. */
    public static boolean isEfficientEaterActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_BELT.get());
    }

    /** Wings → falling Y-velocity is clamped to a soft maximum (gradual glide). */
    public static boolean isGradualGlideActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_WINGS.get());
    }

    /** Goggles → reduced detection range (Invisibility-lite each tick). */
    public static boolean isStealthActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_GOGGLES.get());
    }

    /** Boots → sprinting on flat ground gains an additional Speed I tick. */
    public static boolean isStraightAheadActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_BOOTS.get());
    }

    /** Gloves → ranged shots fired while sprinting/walking are more accurate. */
    public static boolean isAgileRangerActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_GLOVES.get());
    }

    /** Wings → 1.5× jump impulse while sneak-jumping (high jump). */
    public static boolean isHighJumpActive(LivingEntity entity) {
        return wears(entity, TFItems.TRAVELLERS_WINGS.get());
    }
}
