package twilightforest.mixin.client;

import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.translations.BiomeNamesClientStore;

import java.util.Map;

/**
 * Overlay server-pushed {@code biome.*} translations onto the client language
 * table for keys the client doesn't already know.
 *
 * <p>Resource-pack translations always win — we only return the overlay value
 * when {@link #storage} has no entry for the key. So a properly-loaded
 * I18nUpdateMod resource pack continues to display its own translations; the
 * overlay only fills the gap for biomes whose mod jar shipped no lang entries
 * (terralith, candycraftce, yggdrasil, ...) and players whose i18n download
 * silently failed.
 *
 * <p>Scope is limited to the {@code biome.} key prefix to keep overhead off
 * the hot path of every translation lookup.
 */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {

    @Shadow
    @Final
    private Map<String, String> storage;

    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    private void codex$overlayBiomeName(String key, String fallback, CallbackInfoReturnable<String> cir) {
        if (key == null || !key.startsWith("biome.")) return;
        if (this.storage.containsKey(key)) return;
        String pushed = BiomeNamesClientStore.get(key);
        if (pushed != null) cir.setReturnValue(pushed);
    }

    @Inject(method = "has", at = @At("HEAD"), cancellable = true)
    private void codex$overlayBiomeHas(String key, CallbackInfoReturnable<Boolean> cir) {
        if (key == null || !key.startsWith("biome.")) return;
        if (this.storage.containsKey(key)) return;
        if (BiomeNamesClientStore.get(key) != null) cir.setReturnValue(true);
    }
}
