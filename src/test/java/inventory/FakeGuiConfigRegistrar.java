package inventory;

import it.mikeslab.commons.api.inventory.CustomInventory;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.inventory.config.GuiConfigRegistrar;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FakeGuiConfigRegistrar extends GuiConfigRegistrar {

    private final ConcurrentMap<UUID, ConcurrentMap<String, CustomInventory>> fakeCache = new ConcurrentHashMap<>();
    private final Set<String> fakeKeys;

    public FakeGuiConfigRegistrar(EcoNexusLite plugin, String section, Set<String> fakeKeys) {
        super(plugin, section);
        this.fakeKeys = fakeKeys;
    }

    @Override
    public Set<String> getInventoryKeys() {
        return Collections.unmodifiableSet(fakeKeys);
    }

    @Override
    public ConcurrentMap<String, CustomInventory> getCachedInventoriesByUUID(UUID uuid) {
        return fakeCache.getOrDefault(uuid, new ConcurrentHashMap<>());
    }

    public void putCachedInventory(UUID uuid, String key, CustomInventory inv) {
        fakeCache.putIfAbsent(uuid, new ConcurrentHashMap<>());
        fakeCache.get(uuid).put(key, inv);
    }
}