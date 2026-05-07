package twilightforest.asm;

import me.shedaniel.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.Biome;
import twilightforest.world.components.BiomeGrassColors;

import java.lang.reflect.Proxy;

/**
 * Manningham Mills {@code mm:early_risers} entrypoint that grafts five Twilight Forest
 * custom {@code BiomeSpecialEffects$GrassColorModifier} enum values into the JVM enum
 * BEFORE the class is initialized. Required so the codec accepts the
 * {@code "twilightforest:dark_forest"}-style strings that the TF biome JSONs reference.
 *
 * <p>Implementation notes:</p>
 * <ul>
 *   <li>{@code GrassColorModifier} in 1.21.1 has constructor {@code (String, ColorModifier)}.
 *       {@code ColorModifier} is a package-private SAM interface, so we synthesize it via
 *       {@link Proxy#newProxyInstance} keyed by the runtime-mapped intermediary class name
 *       and use a tiny inner-functional-interface {@link Modifier} for the actual math.</li>
 *   <li>Vanilla {@code GrassColorModifier.modifyColor(x, z, c)} delegates to the stored
 *       {@code ColorModifier}, so providing the modifier callback at enum-creation time is
 *       enough — no enum subclassing or Mixin scaffolding required.</li>
 *   <li>Color formulas are verbatim from the NeoForge / Fabric Twilight Forest port
 *       ({@code twilightforest.asm.GrassColorModifier*} classes in the upstream source),
 *       so visuals are 1:1 wherever the modifier actually runs (server worldgen, paired-client
 *       clients that have the same enum, or codex-twilight if the mod is later promoted
 *       to {@code environment="*"}).</li>
 *   <li>Vanilla clients can't run this code, so {@code GrassColorModifierCodecMixin} maps
 *       the TF enum values to vanilla equivalents on encode-to-network — Phase 1 visual
 *       parity is "vanilla-approximate", with full 1:1 deferred to a later phase.</li>
 * </ul>
 */
public class CodexGrassColorEarlyRiser implements Runnable {

    @Override
    public void run() {
        // Phase 1 deferred: real enum extension blocked because ColorModifier (the SAM inner
        // interface required for the (String, ColorModifier) constructor of GrassColorModifier
        // in 1.21.1) cannot be Class.forName'd during MM preApply — its enclosing class is
        // currently being transformed, which triggers recursive class-init.
        //
        // Phase 1 visual parity is currently provided by {@link
        // twilightforest.codec.TFGrassColorModifierCodec} which translates the five
        // {@code twilightforest:*} grass-color-modifier strings to their nearest vanilla
        // equivalents at codec decode time, so the server boots and vanilla clients render
        // a near-vanilla approximation. Phase 2 will recover full 1:1 via Block
        // static-colour ground patches + ItemDisplay ambience.
        //
        // Keep this entrypoint registered so Phase 2 can iterate on it once we wire a
        // bytecode-only ColorModifier reference path.
        return;
    }

    /**
     * Build a runtime instance of the package-private {@code ColorModifier} SAM interface
     * via JDK {@link Proxy}, so we don't need an accesswidener entry exposing it.
     */
    private static Object makeModifier(String colorModifierClassName, Modifier modifier) {
        try {
            Class<?> iface = Class.forName(colorModifierClassName, false, CodexGrassColorEarlyRiser.class.getClassLoader());
            return Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, (proxy, method, args) -> {
                if (args != null && args.length == 3
                        && args[0] instanceof Double dx
                        && args[1] instanceof Double dz
                        && args[2] instanceof Integer ic) {
                    return modifier.modify(dx, dz, ic);
                }
                if ("toString".equals(method.getName())) {
                    return "twilightforest$ColorModifierProxy";
                }
                if ("hashCode".equals(method.getName())) {
                    return System.identityHashCode(proxy);
                }
                if ("equals".equals(method.getName())) {
                    return proxy == args[0];
                }
                return null;
            });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to locate BiomeSpecialEffects$GrassColorModifier$ColorModifier at " + colorModifierClassName, e);
        }
    }

    private static String mapClass(String intermediaryName) {
        return FabricLoader.getInstance().getMappingResolver()
                .mapClassName("intermediary", "net.minecraft." + intermediaryName);
    }

    @FunctionalInterface
    private interface Modifier {
        int modify(double x, double z, int color);
    }
}
