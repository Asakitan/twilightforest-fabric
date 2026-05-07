package twilightforest.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import twilightforest.TwilightForestMod;

public final class CodexTwilightDataGenerators implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		// Providers are migrated into this isolated source set incrementally.
	}

	@Override
	public String getEffectiveModId() {
		return TwilightForestMod.ID;
	}

}
