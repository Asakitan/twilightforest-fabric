package net.neoforged.neoforge.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.ArrayList;
import java.util.List;

public class AddReloadListenerEvent {
    private final RegistryAccess registryAccess;
    private final List<PreparableReloadListener> listeners = new ArrayList<>();

    public AddReloadListenerEvent(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public void addListener(PreparableReloadListener listener) {
        this.listeners.add(listener);
    }

    public RegistryAccess getRegistryAccess() {
        return this.registryAccess;
    }

    public List<PreparableReloadListener> getListeners() {
        return List.copyOf(this.listeners);
    }
}
