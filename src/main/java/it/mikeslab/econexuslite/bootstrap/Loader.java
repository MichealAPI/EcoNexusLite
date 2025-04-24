package it.mikeslab.econexuslite.bootstrap;

import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.commons.api.database.SerializableMapConvertible;
import it.mikeslab.econexuslite.pojo.ConfigStructure;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public abstract class Loader<T extends SerializableMapConvertible<T>> {

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private final Configurable config;
    private final ConfigStructure structure;

    @SuppressWarnings("unchecked")
    protected T createInstance(Class<T> clazz) {
        try {
            Constructor<?> ctor = CONSTRUCTOR_CACHE.computeIfAbsent(
                    clazz,
                    c -> {
                        try {
                            Constructor<?> constructor = c.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            return constructor;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            return (T) ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance", e);
        }
    }

    /**
     * Load a set of objects from the configuration
     * given a specific config structure
     * @param clazz the class of the object to load
     * @return a set of objects
     */
    public HashSet<T> fromConfig(Class<T> clazz) {

        if (config == null) {
            return null;
        }

        String configSubPath = structure.getSubsection();
        ConfigurationSection section = configSubPath == null ?
                                        this.config.getConfiguration() :
                                        this.config
                                                .getConfiguration()
                                                .getConfigurationSection(configSubPath);

        if (section == null) {
            return null;
        }

        HashSet<T> set = new HashSet<>();

        T obj = createInstance(clazz);

        for (String key : section.getKeys(false)) {
            T data = structure.load(config, obj, Optional.of(key));
            if (data != null) {
                set.add(data);
            }
        }

        return set;
    }

    /**
     * Saves a single object to the configuration
     * @param obj the object to save
     * @return whether the save was successful
     */
    public boolean saveToConfig(T obj) {

        if (obj == null) {
            return false;
        }

        if (config == null) {
            return false;
        }

        return structure.save(config, obj);

    }

}
