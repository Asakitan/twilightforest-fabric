package twilightforest.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/**
 * Remap legacy NeoForge attribute ids to their vanilla 1.21.1 equivalents on the
 * read path. Old saved entity NBT (saved by NeoForge or earlier mappings) carries
 * {@code forge:swim_speed} attribute modifiers; vanilla {@code AttributeMap.load}
 * looks them up in the attribute registry, finds nothing, and logs
 * {@code "Ignoring unknown attribute 'forge:swim_speed'"}. The remote server log
 * accumulated 90 of these on cold-start chunk preload alone.
 *
 * <p>Vanilla 1.21.1 has the same semantic attribute as
 * {@code minecraft:generic.water_movement_efficiency}, so we silently rewrite the
 * id string before it hits the registry. Net result: the modifier is preserved on
 * the entity and the warning goes away.</p>
 *
 * <p>Server-only fix: this mixin lives in the common (non-client) source set so
 * dedicated servers and the integrated server both apply it. No new client mod
 * required.</p>
 */
@Mixin(AttributeMap.class)
public abstract class AttributeMapMixin {

    @org.spongepowered.asm.mixin.Unique
    private static final Map<String, String> CODEX$LEGACY_ATTRIBUTE_REMAP = Map.of(
        "forge:swim_speed", "minecraft:generic.water_movement_efficiency",
        "neoforge:swim_speed", "minecraft:generic.water_movement_efficiency"
    );

    @Redirect(method = "load",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;getString(Ljava/lang/String;)Ljava/lang/String;"))
    private String codex$remapLegacyAttribute(CompoundTag tag, String key) {
        String value = tag.getString(key);
        if ("id".equals(key)) {
            String remap = CODEX$LEGACY_ATTRIBUTE_REMAP.get(value);
            if (remap != null) return remap;
        }
        return value;
    }
}
