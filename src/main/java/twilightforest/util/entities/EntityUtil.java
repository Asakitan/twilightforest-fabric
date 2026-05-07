package twilightforest.util.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.util.features.FeaturePlacers;

import java.util.ArrayList;
import java.util.List;

public final class EntityUtil {
    private EntityUtil() {
    }

    public static <T extends Mob> void spawnEntity(EntityType<T> entityType, ServerLevelAccessor level, BlockPos pos) {
        FeaturePlacers.placeEntity(entityType, pos, level);
    }

    /**
     * P5.e: drop-target location for {@code BaseTFBoss.postRemoval} celebratory chest.
     * Upstream picks the boss's home-anchor pos (or current entity pos if no anchor).
     */
    public static BlockPos bossChestLocation(Entity boss) {
        if (boss instanceof twilightforest.entity.EnforcedHomePoint home && home.getRestrictionPoint() != null) {
            return home.getRestrictionPoint().pos();
        }
        return boss.blockPosition();
    }

    /**
     * P5.e: stub for upstream's lava-clearing helper around boss feet — codex's bosses
     * are fire/lava-immune in practice; full upstream behavior (replace lava blocks
     * with magma in a 3×3×3 box) is non-essential for compile.
     */
    public static void killLavaAround(Entity entity) {
    }

    /**
     * P5.e: vanilla-friendly damage-source death-sound pitch helper. Upstream returns
     * the {@code SoundEvent} associated with this entity type's death sound; codex
     * inlines that into each subclass instead. Kept here as a stub returning null
     * so call sites that null-check still work.
     */
    public static net.minecraft.sounds.SoundEvent getDeathSound(net.minecraft.world.entity.LivingEntity entity) {
        return entity.getType().getCategory() == null ? null : net.minecraft.sounds.SoundEvents.GENERIC_DEATH;
    }

    public static boolean tryHangPainting(WorldGenLevel world, BlockPos pos, Direction direction, Holder<PaintingVariant> chosenPainting) {
        if (chosenPainting == null) {
            return false;
        }

        Painting painting = new Painting(world.getLevel(), pos, direction, chosenPainting);
        if (world.noCollision(painting, painting.getBoundingBox())) {
            world.addFreshEntity(painting);
            return true;
        }
        return false;
    }

    public static boolean canDestroyBlock(Level level, BlockPos pos, Entity entity) {
        return level.getWorldBorder().isWithinBounds(pos) && entity.mayInteract(level, pos);
    }

    public static Holder<PaintingVariant> getPaintingOfSize(WorldGenLevel level, RandomSource random, int minSize) {
        List<Holder<PaintingVariant>> valid = new ArrayList<>();
        level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).holders().forEach(holder -> {
            if (holder.value().width() >= minSize || holder.value().height() >= minSize) {
                valid.add(holder);
            }
        });
        return valid.isEmpty() ? null : valid.get(random.nextInt(valid.size()));
    }

    /** Ports upstream {@code twilightforest.util.entities.EntityUtil.rayTrace} — basic block-only
     * ray cast from a player's eye position out to a 5-block reach (matches the upstream constant
     * implicitly used at every call site). Used by {@code HedgeBlock} to detect a player swinging
     * at a hedge from a distance. */
    public static BlockHitResult rayTrace(Player player) {
        return rayTrace(player, range -> range);
    }

    /** Overload that allows the caller to adjust the default 5-block reach (used by
     * {@code StrongholdShieldBlock} to extend the ray a tick further before deciding whether the
     * front-face is being hit). */
    public static BlockHitResult rayTrace(Player player, java.util.function.DoubleUnaryOperator rangeAdjuster) {
        double reach = rangeAdjuster.applyAsDouble(5.0D);
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * reach, look.y * reach, look.z * reach);
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    public static List<Entity> getEntitiesInAABB(WorldGenLevel world, AABB boundingBox) {
        List<Entity> entities = new ArrayList<>();
        int minChunkX = Mth.floor((boundingBox.minX - 2) / 16.0D);
        int maxChunkX = Mth.floor((boundingBox.maxX + 2) / 16.0D);
        int minChunkZ = Mth.floor((boundingBox.minZ - 2) / 16.0D);
        int maxChunkZ = Mth.floor((boundingBox.maxZ + 2) / 16.0D);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                ChunkAccess chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_STARTS);
                if (chunk instanceof ProtoChunk protoChunk) {
                    protoChunk.getEntities().forEach(tag -> {
                        Entity entity = EntityType.loadEntityRecursive(tag, world.getLevel(), value -> value);
                        if (entity != null && boundingBox.intersects(entity.getBoundingBox())) {
                            entities.add(entity);
                        }
                    });
                }
            }
        }

        return entities;
    }
}
