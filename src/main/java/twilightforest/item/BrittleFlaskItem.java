package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import twilightforest.components.item.PotionFlaskComponent;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFSounds;

import java.util.function.Consumer;

/**
 * Q30 → Q32 promoted port of TF {@code BrittleFlaskItem} / {@code GreaterFlaskItem}.
 *
 * <p>Q30 shipped a placeholder that always granted Regen + Absorption. Q32 wires
 * up the real {@link PotionFlaskComponent} data component so the flask now:</p>
 * <ul>
 *   <li>Carries up to {@code DOSES} (3 brittle, 4 greater) of one potion type.</li>
 *   <li>Right-click on a Potion stack (secondary click in inventory) → fills one
 *       dose, returns a glass bottle, plays {@link TFSounds#FLASK_FILL}.</li>
 *   <li>Right-click in world → drinks one dose, applies the potion's effects,
 *       decrements doses, increments breakage. Breakage = DOSES → flask shatters
 *       ({@link TFSounds#BRITTLE_FLASK_BREAK}); else cracks ({@link TFSounds#BRITTLE_FLASK_CRACK}).</li>
 *   <li>Greater flask is unbreakable (set via constructor) so its breakage
 *       counter is ignored.</li>
 *   <li>Empty flask (no potion stored) falls back to the Q30 hardcoded
 *       Regen + Absorption effect so legacy stacks/Mythic drops still drink.</li>
 * </ul>
 *
 * <p>Brewing-stand recipe (vanilla potion → flask in slot) is not wired here —
 * users can fill via the inventory click flow above, /give with an NBT
 * {@code potion_flask_contents} component, or Mythic item drops with the
 * component pre-set.</p>
 */
public class BrittleFlaskItem extends CodexItem {

    private final boolean greater;
    private final int maxDoses;

    public BrittleFlaskItem(Properties properties, Item fallback, boolean greater) {
        super(properties, fallback, -1);
        this.greater = greater;
        this.maxDoses = greater ? 4 : 3;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(TFDataComponents.POTION_FLASK_CONTENTS,
                this.greater ? PotionFlaskComponent.EMPTY_UNBREAKABLE : PotionFlaskComponent.EMPTY);
        return stack;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return tryFill(stack, other, action, player);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return tryFill(stack, slot.getItem(), action, player);
    }

    private boolean tryFill(ItemStack flaskStack, ItemStack other, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        PotionContents bottlePotion = other.get(DataComponents.POTION_CONTENTS);
        if (bottlePotion == null) return false;

        PotionFlaskComponent flask = flaskStack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS,
                this.greater ? PotionFlaskComponent.EMPTY_UNBREAKABLE : PotionFlaskComponent.EMPTY);

        boolean canMix = flask.potion().potion().isEmpty() || flask.potion().equals(bottlePotion);
        boolean hasRoom = flask.doses() < this.maxDoses - flask.breakage();
        if (!canMix || !hasRoom) return false;

        if (!player.getAbilities().instabuild) {
            other.shrink(1);
            if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        changeAndConsumeFlask(flaskStack, player, st ->
                st.set(TFDataComponents.POTION_FLASK_CONTENTS, flask.tryAddDose(bottlePotion)));
        player.level().playSound(null, player, TFSounds.FLASK_FILL, SoundSource.PLAYERS,
                (flask.doses() + 1) * 0.25F, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        PotionFlaskComponent flask = stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS,
                this.greater ? PotionFlaskComponent.EMPTY_UNBREAKABLE : PotionFlaskComponent.EMPTY);

        // Real potion path: requires doses > 0 and a stored potion.
        if (flask.potion() != PotionContents.EMPTY && flask.doses() > 0) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }

        // Legacy fallback (Q30 behaviour) — empty flask still drinks for Regen + Absorption.
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    TFSounds.FLASK_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            int regenAmplifier = greater ? 1 : 0;
            int absorptionAmplifier = greater ? 1 : 0;
            int regenDuration = greater ? 200 : 160;
            int absorptionDuration = greater ? 1200 : 600;
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDuration, regenAmplifier));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, absorptionDuration, absorptionAmplifier));
            if (!player.getAbilities().instabuild && player instanceof ServerPlayer sp && level instanceof net.minecraft.server.level.ServerLevel sl) {
                stack.hurtAndBreak(1, sl, sp, removed -> {});
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        PotionFlaskComponent flask = stack.getOrDefault(TFDataComponents.POTION_FLASK_CONTENTS,
                this.greater ? PotionFlaskComponent.EMPTY_UNBREAKABLE : PotionFlaskComponent.EMPTY);
        if (flask.potion() == PotionContents.EMPTY || !(entity instanceof Player player)) {
            return super.finishUsingItem(stack, level, entity);
        }

        if (!level.isClientSide()) {
            for (MobEffectInstance effect : flask.potion().getAllEffects()) {
                if (effect.getEffect().value().isInstantenous()) {
                    effect.getEffect().value().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0D);
                } else {
                    player.addEffect(new MobEffectInstance(effect));
                }
            }
            level.playSound(null, player, TFSounds.FLASK_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (!player.getAbilities().instabuild) {
            changeAndConsumeFlask(stack, player, st -> {
                PotionFlaskComponent next = flask.removeDose();
                st.set(TFDataComponents.POTION_FLASK_CONTENTS, next);
                if (next.breakable() && next.breakage() >= this.maxDoses) {
                    st.shrink(1);
                    level.playSound(null, player, TFSounds.BRITTLE_FLASK_BREAK, SoundSource.PLAYERS, 1.5F, 0.7F);
                } else if (next.breakable()) {
                    level.playSound(null, player, TFSounds.BRITTLE_FLASK_CRACK, SoundSource.PLAYERS, 1.5F, 2.0F);
                }
            });
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private void changeAndConsumeFlask(ItemStack stack, Player player, Consumer<ItemStack> mutator) {
        if (stack.getCount() > 1) {
            ItemStack copy = stack.copyWithCount(1);
            stack.shrink(1);
            mutator.accept(copy);
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        } else {
            mutator.accept(stack);
        }
    }
}
