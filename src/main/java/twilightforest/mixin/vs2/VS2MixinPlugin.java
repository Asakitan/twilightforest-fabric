package twilightforest.mixin.vs2;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin for {@code codex_twilight_vs2.mixins.json}.
 *
 * <p>Probes whether ValkyrienSkies's {@code BoxesBlockShapeImpl$BuilderImpl}
 * class is on the classloader before allowing any VS2-targeting mixin to apply.
 * If VS2 is absent (e.g. dev environment without the VS jar) the plugin returns
 * {@code false} from {@link #shouldApplyMixin}, so SpongePowered Mixin silently
 * skips the application instead of raising
 * {@code MixinTargetNotFoundException} during mod load.
 */
public final class VS2MixinPlugin implements IMixinConfigPlugin {

    private static final String VS2_PROBE_CLASS =
        "org.valkyrienskies.core.impl.api_impl.physics.blockstates.BoxesBlockShapeImpl$BuilderImpl";

    private boolean vs2Present;

    @Override
    public void onLoad(String mixinPackage) {
        boolean present;
        try {
            // ClassLoader-only probe — no static init.
            Class.forName(VS2_PROBE_CLASS, false, VS2MixinPlugin.class.getClassLoader());
            present = true;
        } catch (ClassNotFoundException | LinkageError e) {
            present = false;
        }
        this.vs2Present = present;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return this.vs2Present;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // No-op.
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No-op.
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No-op.
    }
}
