package twilightforest.util.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.config.TFConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Per-{@link ServerLevel} record of TF bosses that have been killed but should respawn
 * after a configurable number of in-world days.
 *
 * <h2>Design constraints (per user)</h2>
 * <ul>
 *   <li><b>Do NOT touch first-kill progression chain.</b> Advancements, quest
 *       triggers, conquered structure flag, magic-map red-X markers, etc. all
 *       stay {@code true} forever after the first kill. This service ONLY puts
 *       the boss spawner BLOCK back at the recorded position; everything else
 *       is left untouched.</li>
 *   <li>Subsequent kills go through the same {@link twilightforest.entity.boss.BaseTFBoss#die}
 *       path so loot drops, but the {@code conquered=true} marker already set
 *       on first kill means {@code LandmarkUtil.markStructureConquered} is a
 *       no-op, and {@link net.minecraft.advancements.CriteriaTriggers#trigger}
 *       calls are idempotent on already-granted advancements.</li>
 *   <li>Per-spawner granularity: each Hydra Lair / Naga Courtyard / Lich Tower
 *       independently respawns based on when its specific boss was killed.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Boss dies → {@link BaseTFBoss.die} → success-path
 *       {@link #recordKill} captures spawner block id + spawner pos (= boss's
 *       restriction point pre-discard) + current dayTime/24000.</li>
 *   <li>Server tick → {@link #tickRespawn} walks pending entries; if
 *       {@code currentDay - killedDay >= respawnDelayDays}, attempts to
 *       re-place the spawner block at the saved position. Removes the entry
 *       on success.</li>
 *   <li>{@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents#LOAD} /
 *       {@code SAVE} via {@code ServerLevel.getDataStorage()} handles
 *       persistence.</li>
 * </ol>
 */
public class DailyBossRespawnState extends SavedData {

    public static final String DATA_NAME = "codex_twilight_daily_boss_respawn";
    private static final Logger LOGGER = LoggerFactory.getLogger("CodexTwilight/daily-boss");

    /** Single pending respawn record. */
    public record Entry(ResourceLocation spawnerBlockId, BlockPos spawnerPos, long killedAtDay) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("spawner_block").forGetter(Entry::spawnerBlockId),
            BlockPos.CODEC.fieldOf("pos").forGetter(Entry::spawnerPos),
            Codec.LONG.fieldOf("killed_at_day").forGetter(Entry::killedAtDay)
        ).apply(instance, Entry::new));
    }

    private final List<Entry> pending = new ArrayList<>();

    /** Vanilla {@link SavedData.Factory} entry point. Empty defaults are safe. */
    public static SavedData.Factory<DailyBossRespawnState> factory() {
        return new SavedData.Factory<>(
            DailyBossRespawnState::new,
            DailyBossRespawnState::load,
            DataFixTypes.LEVEL  // closest vanilla fix-type for a per-level container
        );
    }

    public static DailyBossRespawnState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    private DailyBossRespawnState() {
    }

    private static DailyBossRespawnState load(CompoundTag tag, HolderLookup.Provider provider) {
        DailyBossRespawnState state = new DailyBossRespawnState();
        if (tag.contains("entries", Tag.TAG_LIST)) {
            Entry.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("entries"))
                .resultOrPartial(err -> LOGGER.warn("Failed to load daily boss respawn entries: {}", err))
                .ifPresent(list -> state.pending.addAll(list));
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        Entry.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.pending)
            .resultOrPartial(err -> LOGGER.warn("Failed to save daily boss respawn entries: {}", err))
            .ifPresent(encoded -> tag.put("entries", encoded));
        return tag;
    }

    /**
     * Record a boss death for later respawn. Called from
     * {@link twilightforest.entity.boss.BaseTFBoss#die} after successful postmortem.
     *
     * @param spawnerBlock the boss spawner block (from {@code BaseTFBoss#getBossSpawner})
     * @param spawnerPos   the recorded spawner position (from the boss's restriction point)
     * @param level        the level the boss died in
     */
    public void recordKill(Block spawnerBlock, BlockPos spawnerPos, ServerLevel level) {
        if (!TFConfig.dailyBossRespawn) return;
        if (spawnerBlock == null) return;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(spawnerBlock);
        long day = level.getDayTime() / 24000L;
        // Deduplicate: if already pending for this exact pos+block, refresh the day
        // (e.g. boss respawned + killed again same day before previous entry processed).
        for (int i = 0; i < this.pending.size(); i++) {
            Entry e = this.pending.get(i);
            if (e.spawnerPos.equals(spawnerPos) && e.spawnerBlockId.equals(blockId)) {
                this.pending.set(i, new Entry(blockId, spawnerPos, day));
                this.setDirty();
                LOGGER.debug("Refreshed daily respawn entry for {} at {} (day {})", blockId, spawnerPos, day);
                return;
            }
        }
        this.pending.add(new Entry(blockId, spawnerPos, day));
        this.setDirty();
        LOGGER.info("Recorded boss kill for daily respawn: {} at {} (day {})", blockId, spawnerPos, day);
    }

    /**
     * Server tick entry point. Iterates pending entries; for each whose age in days
     * meets or exceeds {@link TFConfig#dailyBossRespawnDelayDays}, restores the
     * spawner block if the chunk is loaded and the position is replaceable.
     *
     * <p>Entries for unloaded chunks are left in the queue and retried next tick.
     * This is cheap because the queue is small (one entry per killed boss).
     */
    public void tickRespawn(ServerLevel level) {
        if (!TFConfig.dailyBossRespawn) return;
        if (this.pending.isEmpty()) return;

        long currentDay = level.getDayTime() / 24000L;
        long delayDays = Math.max(1L, TFConfig.dailyBossRespawnDelayDays);

        Iterator<Entry> it = this.pending.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (currentDay - entry.killedAtDay < delayDays) continue;

            BlockPos pos = entry.spawnerPos;
            if (!level.isLoaded(pos)) continue;  // wait for chunk to load

            Block spawnerBlock = BuiltInRegistries.BLOCK.get(entry.spawnerBlockId);
            if (spawnerBlock == null) {
                LOGGER.warn("Daily respawn: spawner block {} not in registry; dropping entry at {}",
                    entry.spawnerBlockId, pos);
                it.remove();
                this.setDirty();
                continue;
            }

            // If a spawner block is already at the position (e.g. server crashed between
            // respawn and kill, or another mod restored it), skip without erroring.
            if (level.getBlockState(pos).is(spawnerBlock)) {
                LOGGER.debug("Daily respawn: spawner already at {}; clearing pending entry", pos);
                it.remove();
                this.setDirty();
                continue;
            }

            try {
                // Force the placement even if there's currently another block there.
                // Boss spawner blocks are central to the structure, and any junk that
                // ended up at the position (loot chest, debris) shouldn't block respawn.
                level.setBlockAndUpdate(pos, spawnerBlock.defaultBlockState());
                LOGGER.info("Daily respawn placed {} at {} (killed day {}, today {})",
                    entry.spawnerBlockId, pos, entry.killedAtDay, currentDay);
                it.remove();
                this.setDirty();
            } catch (Throwable t) {
                LOGGER.warn("Daily respawn placement failed at {}: {}", pos, t.toString());
                // Leave in queue, retry next tick.
            }
        }
    }

    /** Diagnostic / command helpers. */
    public List<Entry> snapshot() {
        return List.copyOf(this.pending);
    }

    public void clearAll() {
        this.pending.clear();
        this.setDirty();
    }
}
