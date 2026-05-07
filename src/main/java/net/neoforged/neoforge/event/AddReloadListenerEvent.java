package net.neoforged.neoforge.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * NeoForge stub. Currently unused — the only caller in TF source
 * (CodecResourceReloadListener) is excluded from compilation in build.gradle,
 * so {@link #addListener(PreparableReloadListener)} being a no-op is safe.
 *
 * If those excluded files are re-enabled later, replace the empty body with
 * a Fabric ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(...)
 * call so the listener actually fires on /reload.
 */
public class AddReloadListenerEvent {
    private final RegistryAccess registryAccess;

    public AddReloadListenerEvent(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public void addListener(PreparableReloadListener listener) {
        // No-op: see class javadoc.
    }

    public RegistryAccess getRegistryAccess() {
        return this.registryAccess;
    }
}
