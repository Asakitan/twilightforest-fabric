package twilightforest.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Fabric port — codec is 1:1 with upstream so {@code destruction.json} parses
 * cleanly. Runtime simplified: removes NeoForge {@code BlockEvent.BreakEvent}
 * dispatch, the per-entity {@code TFDataAttachments.SMASH_BLOCKS} counter, and
 * {@code ChainBlock.canBreakBlockAt}; instead just iterates the radius and
 * destroys non-immune blocks the player is allowed to mine.
 */
public record SmashBlocksEffect(LevelBasedValue maxSmash, LevelBasedValue radius,
                                Optional<HolderSet<Block>> immuneBlocks,
                                Optional<HolderSet<Block>> vulnerableBlocks,
                                Optional<Holder<SoundEvent>> smashSound) implements EnchantmentEntityEffect {

    public static final MapCodec<SmashBlocksEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("max_smash").forGetter(SmashBlocksEffect::maxSmash),
            LevelBasedValue.CODEC.fieldOf("radius").forGetter(SmashBlocksEffect::radius),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("immune_blocks").forGetter(SmashBlocksEffect::immuneBlocks),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("vulnerable_blocks").forGetter(SmashBlocksEffect::vulnerableBlocks),
            SoundEvent.CODEC.optionalFieldOf("smash_sound").forGetter(SmashBlocksEffect::smashSound))
        .apply(instance, SmashBlocksEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        if (!(item.owner() instanceof ServerPlayer player)) return;

        int maxSmash = Math.round(this.maxSmash.calculate(enchantmentLevel));
        int radius = Math.round(this.radius.calculate(enchantmentLevel));
        if (maxSmash <= 0 || radius <= 0) return;

        BlockPos start = BlockPos.containing(position);
        int smashed = 0;

        for (BlockPos pos : BlockPos.betweenClosed(start.offset(-radius, 0, -radius), start.offset(radius, 0, radius))) {
            if (smashed >= maxSmash) break;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (this.immuneBlocks.isPresent() && this.immuneBlocks.get().contains(state.getBlockHolder())) continue;
            if (state.getDestroySpeed(level, pos) < 0) continue;
            level.destroyBlock(pos, !player.isCreative(), player);
            if (this.smashSound.isPresent()) {
                level.playSound(null, pos, this.smashSound.get().value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            smashed++;
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
