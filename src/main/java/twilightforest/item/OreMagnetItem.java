package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFSounds;
import twilightforest.util.iterators.VoxelBresenhamIterator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Q37 simplified port of TF {@code OreMagnetItem}: hold + release fires a
 * 32-block raycast cone in the look direction; up to 8 ore blocks within the
 * cone are pulled out (replaced with their post-mining state) and converted to
 * floating item entities pulled toward the player. Each ore pulled costs 1
 * durability.
 *
 * <p>Skipped from TF original: the line-of-sight Voxel Bresenham iterator
 * (replaced with a simple stepped raycast), per-ore-type replacement maps
 * (we use {@link Block#getCloneItemStack} for the dropped item and replace the
 * mined block with stone), the wiggle-cone retry-with-offset pass.</p>
 */
public class OreMagnetItem extends CodexItem {

    private static final double RANGE = 32.0D;
    private static final int MAX_PULLS = 8;
    private static final int FIRING_TIME = 10;

    public OreMagnetItem(Properties properties, Item fallback) {
        super(properties, fallback, -1);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int useRemaining) {
        int useTime = this.getUseDuration(stack, living) - useRemaining;
        if (level.isClientSide() || useTime < FIRING_TIME) return;
        if (!(living instanceof Player player)) return;
        if (stack.getDamageValue() >= stack.getMaxDamage() && !player.getAbilities().instabuild) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        int pulls = 0;

        // Step every 1 block along the raycast; check a 1-block radius cylinder around each step.
        for (int step = 1; step <= RANGE && pulls < MAX_PULLS; step++) {
            Vec3 here = eye.add(look.scale(step));
            BlockPos centre = BlockPos.containing(here);
            for (int dx = -1; dx <= 1 && pulls < MAX_PULLS; dx++) {
                for (int dy = -1; dy <= 1 && pulls < MAX_PULLS; dy++) {
                    for (int dz = -1; dz <= 1 && pulls < MAX_PULLS; dz++) {
                        BlockPos at = centre.offset(dx, dy, dz);
                        BlockState state = serverLevel.getBlockState(at);
                        if (!state.is(BlockTags.IRON_ORES) && !state.is(BlockTags.GOLD_ORES)
                                && !state.is(BlockTags.DIAMOND_ORES) && !state.is(BlockTags.EMERALD_ORES)
                                && !state.is(BlockTags.LAPIS_ORES) && !state.is(BlockTags.REDSTONE_ORES)
                                && !state.is(BlockTags.COAL_ORES) && !state.is(BlockTags.COPPER_ORES)) {
                            continue;
                        }
                        ItemStack drop = new ItemStack(state.getBlock().asItem());
                        if (drop.isEmpty()) drop = state.getBlock().getCloneItemStack(serverLevel, at, state);
                        if (drop.isEmpty()) continue;

                        // Mine to deepslate or stone backfill depending on neighbour
                        BlockState fill = state.toString().contains("deepslate")
                                ? net.minecraft.world.level.block.Blocks.DEEPSLATE.defaultBlockState()
                                : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
                        serverLevel.destroyBlock(at, false);
                        serverLevel.setBlockAndUpdate(at, fill);

                        ItemEntity entity = new ItemEntity(serverLevel,
                                at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D,
                                drop);
                        Vec3 pull = player.position().add(0, 1, 0).subtract(at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D).normalize().scale(0.6D);
                        entity.setDeltaMovement(pull);
                        entity.setNoPickUpDelay();
                        serverLevel.addFreshEntity(entity);

                        serverLevel.sendParticles(ParticleTypes.PORTAL,
                                at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D,
                                12, 0.3, 0.3, 0.3, 0.05);
                        pulls++;
                    }
                }
            }
        }

        if (pulls > 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    TFSounds.ORE_MAGNET_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (player instanceof ServerPlayer sp && !player.getAbilities().instabuild) {
                stack.hurtAndBreak(pulls, serverLevel, sp, removed -> {});
            }
        }
        player.getCooldowns().addCooldown(this, 20);
    }

    // -------------------------------------------------------------------------
    // Static ore-magnet effect used by MineLogCoreBlock
    // -------------------------------------------------------------------------

    /**
     * Scans the line from {@code usePos} to {@code destPos} using a Bresenham
     * voxel iterator, finds the first ore block and the first replaceable block,
     * then teleports the whole ore vein to the replaceable block position.
     *
     * @return number of blocks moved (0 if nothing found or nothing to replace)
     */
    public static int doMagnet(Level level, BlockPos usePos, BlockPos destPos) {
        return doMagnet(level, usePos, destPos, false);
    }

    public static int doMagnet(ServerLevel level, BlockPos usePos, BlockPos destPos, boolean sourceIsMineCore) {
        return doMagnet((Level) level, usePos, destPos, sourceIsMineCore);
    }

    public static int doMagnet(Level level, BlockPos usePos, BlockPos destPos, boolean sourceIsMineCore) {
        initOre2BlockMap();

        int blocksMoved = 0;
        BlockState attractedOreBlock = Blocks.AIR.defaultBlockState();
        BlockState replacementBlock = Blocks.AIR.defaultBlockState();
        BlockPos foundPos = null;
        BlockPos basePos = null;

        for (BlockPos coord : new VoxelBresenhamIterator(usePos, destPos)) {
            BlockState searchState = level.getBlockState(coord);
            if (basePos == null) {
                if (isReplaceable(searchState)) {
                    basePos = coord;
                }
            } else if (foundPos == null && !searchState.isAir() && isOre(searchState.getBlock())
                    && level.getBlockEntity(coord) == null) {
                attractedOreBlock = searchState;
                replacementBlock = ORE_TO_BLOCK_REPLACEMENTS
                        .getOrDefault(attractedOreBlock.getBlock(), Blocks.STONE)
                        .defaultBlockState();
                foundPos = coord;
            }
        }

        if (basePos != null && foundPos != null && !attractedOreBlock.isAir()) {
            Set<BlockPos> veinBlocks = new HashSet<>();
            findVein(level, foundPos, attractedOreBlock, veinBlocks);

            int offX = basePos.getX() - foundPos.getX();
            int offY = basePos.getY() - foundPos.getY();
            int offZ = basePos.getZ() - foundPos.getZ();

            for (BlockPos coord : veinBlocks) {
                BlockPos replacePos = coord.offset(offX, offY, offZ);
                BlockState replaceState = level.getBlockState(replacePos);
                if (isReplaceable(replaceState) || replaceState.canBeReplaced() || replaceState.isAir()) {
                    level.setBlock(coord, replacementBlock, 2);
                    level.setBlock(replacePos, attractedOreBlock, 2);
                    if (sourceIsMineCore && level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(twilightforest.init.TFParticleType.LOG_CORE_PARTICLE,
                                replacePos.getX() + 0.5D, replacePos.getY() + 0.5D, replacePos.getZ() + 0.5D,
                                2, 0.25D, 0.25D, 0.25D, 0.0D);
                    }
                    blocksMoved++;
                }
            }
        }

        return blocksMoved;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.is(BlockTagGenerator.ORE_MAGNET_SAFE_REPLACE_BLOCK);
    }

    private static boolean isOre(Block ore) {
        return ORE_TO_BLOCK_REPLACEMENTS.containsKey(ore);
    }

    private static void findVein(Level level, BlockPos here, BlockState oreState, Set<BlockPos> veinBlocks) {
        if (veinBlocks.contains(here) || veinBlocks.size() >= 24) return;
        if (level.getBlockState(here) == oreState) {
            veinBlocks.add(here);
            for (Direction dir : Direction.values()) {
                findVein(level, here.relative(dir), oreState, veinBlocks);
            }
        }
    }

    private static boolean oreCacheNeedsBuild = true;
    private static final HashMap<Block, Block> ORE_TO_BLOCK_REPLACEMENTS = new HashMap<>();

    private static void initOre2BlockMap() {
        if (!oreCacheNeedsBuild) return;
        List<TagKey<Block>> tags = BuiltInRegistries.BLOCK.getTagNames()
                .filter(t -> t.location().getNamespace().equals("c"))
                .collect(Collectors.toList());
        for (TagKey<Block> tag : tags) {
            String path = tag.location().getPath();
            if (!path.startsWith("ores_in_ground/")) continue;
            String ground = path.substring("ores_in_ground/".length());
            TagKey<Block> groundTag = TagKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath("c", "ore_bearing_ground/" + ground));
            if (tags.stream().anyMatch(t -> t.location().equals(groundTag.location()))) {
                BuiltInRegistries.BLOCK.getTag(groundTag).ifPresent(groundHolders ->
                        BuiltInRegistries.BLOCK.getTag(tag).ifPresent(oreHolders ->
                                groundHolders.forEach(groundHolder ->
                                        oreHolders.forEach(oreHolder ->
                                                ORE_TO_BLOCK_REPLACEMENTS.put(
                                                        oreHolder.value(), groundHolder.value())))));
            }
        }
        oreCacheNeedsBuild = false;
    }
}
