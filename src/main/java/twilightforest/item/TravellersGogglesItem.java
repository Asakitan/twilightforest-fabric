package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import com.codex.twilight.network.CodexNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.components.item.GogglesContents;
import twilightforest.init.TFDataComponents;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.Comparator;

/**
 * Q38 promotion of Q32 {@link TravellersArmorPieceItem} for the Travellers
 * Goggles slot: in addition to the worn-effect (NIGHT_VISION) and Q34/Q35/Q36
 * mixin-driven modifiers (arrow magnetism / red-thread vision / stealth), the
 * goggles now act as a 4-slot bundle.
 *
 * <p>Inventory secondary-click on the goggles → swaps one stored stack with
 * the clicked-on stack. Inventory secondary-click while holding the goggles
 * over another stack → inserts the held stack into the next free goggles slot
 * (or removes one if held stack is empty). Persists via
 * {@link TFDataComponents#GOGGLES_CONTENTS}.</p>
 */
public class TravellersGogglesItem extends TravellersArmorPieceItem {

    private static final int SURVEY_COOLDOWN_TICKS = 40;
    private static final int SIDE_STEP_COOLDOWN_TICKS = 30;

    public TravellersGogglesItem(Holder<ArmorMaterial> material, ArmorItem.Type type,
                                 Properties properties, Item fallback, int cmd,
                                 Holder<MobEffect> wornEffect, int amplifier,
                                 EquipmentSlot expectedSlot, boolean onlyInAir) {
        super(material, type, properties, fallback, cmd, wornEffect, amplifier, expectedSlot, onlyInAir);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        GogglesContents contents = stack.getOrDefault(TFDataComponents.GOGGLES_CONTENTS, GogglesContents.EMPTY);
        if (player.isShiftKeyDown()
                && player.getOffhandItem().isEmpty()
                && contents.first().isEmpty()
                && TravellersModifiersManager.isStepUpActive(player)) {
            triggerSideStep(serverPlayer, stack);
            return InteractionResultHolder.success(stack);
        }
        if (player.isShiftKeyDown()) {
            cycleStoredStack(serverPlayer, stack, contents);
            return InteractionResultHolder.success(stack);
        }
        showSurvey(serverPlayer, stack);
        return InteractionResultHolder.success(stack);
    }

    private static void showSurvey(ServerPlayer player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB scan = new AABB(player.blockPosition()).inflate(48.0D, 24.0D, 48.0D);
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, scan,
                        entity -> entity != player && entity.isAlive() && player.hasLineOfSight(entity))
                .stream()
                .max(Comparator.comparingDouble(entity -> look.normalize().dot(entity.position().subtract(eye).normalize())))
                .filter(entity -> look.normalize().dot(entity.position().subtract(eye).normalize()) > 0.92D)
                .orElse(null);
        BlockPos pos = player.blockPosition();
        Component targetText = target == null
            ? Component.translatable("message.codex_twilight.travellers_goggles.no_target")
            : target.getType().getDescription();
        Component biome = level.getBiome(pos).unwrapKey()
            .map(key -> biomeName(key.location()))
            .orElse(Component.translatable("message.codex_twilight.travellers_goggles.unknown_biome"));
        player.displayClientMessage(Component.translatable("message.codex_twilight.travellers_goggles.survey", targetText, biome, pos.getY()), true);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, true));
        spawnSurveyPanel(level, player, look);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        player.getCooldowns().addCooldown(stack.getItem(), SURVEY_COOLDOWN_TICKS);
    }

    private static void spawnSurveyPanel(ServerLevel level, ServerPlayer player, Vec3 look) {
        Vec3 position = player.getEyePosition().add(look.normalize().scale(1.45D)).add(0.0D, -0.22D, 0.0D);
        CodexNetworking.sendGogglesSurvey(player, position.x(), position.y(), position.z(), player.getYRot(), player.getXRot());
    }

    private static void cycleStoredStack(ServerPlayer player, ItemStack goggles, GogglesContents contents) {
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            if (!offhand.getItem().canFitInsideContainerItems() || !contents.canFit()) {
                player.displayClientMessage(Component.translatable("message.codex_twilight.travellers_goggles.pockets_full"), true);
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.6F, 0.75F);
                return;
            }
            goggles.set(TFDataComponents.GOGGLES_CONTENTS, contents.withInserted(offhand.copy()));
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            playInsert(player);
            player.displayClientMessage(Component.translatable("message.codex_twilight.travellers_goggles.stored_offhand"), true);
            return;
        }
        ItemStack pulled = contents.first();
        if (pulled.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.codex_twilight.travellers_goggles.pockets_empty"), true);
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.6F, 0.75F);
            return;
        }
        goggles.set(TFDataComponents.GOGGLES_CONTENTS, contents.withRemovedFirst());
        player.setItemInHand(InteractionHand.OFF_HAND, pulled.copy());
        playRemove(player);
        player.displayClientMessage(Component.translatable("message.codex_twilight.travellers_goggles.loaded_offhand"), true);
    }

    private static void triggerSideStep(ServerPlayer player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();
        double sign = player.getRandom().nextBoolean() ? 1.0D : -1.0D;
        Vec3 motion = right.scale(0.85D * sign).add(0.0D, player.onGround() ? 0.12D : 0.02D, 0.0D);
        player.setDeltaMovement(player.getDeltaMovement().add(motion));
        player.hurtMarked = true;
        player.resetFallDistance();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.7F, 1.35F);
        player.displayClientMessage(Component.translatable("message.codex_twilight.travellers_goggles.side_step"), true);
        player.getCooldowns().addCooldown(stack.getItem(), SIDE_STEP_COOLDOWN_TICKS);
    }

    private static Component biomeName(ResourceLocation id) {
        return Component.translatable("biome." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || stack.getCount() != 1) return false;
        GogglesContents contents = stack.getOrDefault(TFDataComponents.GOGGLES_CONTENTS, GogglesContents.EMPTY);
        ItemStack other = slot.getItem();
        if (other.isEmpty()) {
            ItemStack pulled = contents.first();
            if (pulled.isEmpty()) return false;
            slot.safeInsert(pulled);
            stack.set(TFDataComponents.GOGGLES_CONTENTS, contents.withRemovedFirst());
            playRemove(player);
            return true;
        }
        if (!other.getItem().canFitInsideContainerItems()) return false;
        if (!contents.canFit()) return false;
        ItemStack inserted = slot.safeTake(other.getCount(), other.getCount(), player);
        stack.set(TFDataComponents.GOGGLES_CONTENTS, contents.withInserted(inserted));
        playInsert(player);
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || stack.getCount() != 1) return false;
        if (!slot.allowModification(player)) return false;
        GogglesContents contents = stack.getOrDefault(TFDataComponents.GOGGLES_CONTENTS, GogglesContents.EMPTY);
        if (other.isEmpty()) {
            ItemStack pulled = contents.first();
            if (pulled.isEmpty()) return false;
            access.set(pulled);
            stack.set(TFDataComponents.GOGGLES_CONTENTS, contents.withRemovedFirst());
            playRemove(player);
            return true;
        }
        if (!other.getItem().canFitInsideContainerItems()) return false;
        if (!contents.canFit()) return false;
        stack.set(TFDataComponents.GOGGLES_CONTENTS, contents.withInserted(other));
        access.set(ItemStack.EMPTY);
        playInsert(player);
        return true;
    }

    private static void playInsert(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);
    }

    private static void playRemove(Player player) {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
    }
}
