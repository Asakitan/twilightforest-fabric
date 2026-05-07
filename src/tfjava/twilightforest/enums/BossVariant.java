package twilightforest.enums;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 1:1 port of upstream {@code twilightforest.enums.BossVariant} (NeoForge → Fabric:
 * upstream paired each variant with a {@code BossSpawnerBlockEntity} supplier used by
 * the spawner-block registry; codex-twilight has no ported BossSpawnerBlockEntity yet,
 * so the supplier slot is dropped here. Trophy code only consumes
 * {@code variant} / {@code getTrophyType()} / the CODEC, none of which need the
 * spawner-BE link.).
 */
public enum BossVariant implements StringRepresentable {

	NAGA(TrophyType.GOLD),
	LICH(TrophyType.GOLD),
	HYDRA(TrophyType.GOLD),
	UR_GHAST(TrophyType.GOLD),
	KNIGHT_PHANTOM(TrophyType.IRON),
	SNOW_QUEEN(TrophyType.GOLD),
	MINOSHROOM(TrophyType.IRON),
	ALPHA_YETI(TrophyType.IRON),
	QUEST_RAM(TrophyType.IRONWOOD),
	FINAL_BOSS(TrophyType.GOLD);

	public static final EnumCodec<BossVariant> CODEC = StringRepresentable.fromEnum(BossVariant::values);
	private final TrophyType trophyType;

	BossVariant(TrophyType trophyType) {
		this.trophyType = trophyType;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public TrophyType getTrophyType() {
		return this.trophyType;
	}

	public enum TrophyType {
		GOLD("trophy"),
		IRON("trophy_minor"),
		IRONWOOD("trophy_quest");

		private final String modelName;

		TrophyType(String modelName) {
			this.modelName = modelName;
		}

		public String getModelName() {
			return this.modelName;
		}
	}
}
