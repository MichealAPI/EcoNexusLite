package it.mikeslab.econexuslite.bootstrap;

import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.commons.api.database.SerializableMapConvertible;
import it.mikeslab.econexuslite.pojo.ConfigStructure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Abstract base class for loading objects from configuration.
 * Provides common functionality for loading and saving objects.
 * 
 * @param <T> The type of object to load/save
 */
@RequiredArgsConstructor
public abstract class Loader<T extends SerializableMapConvertible<T>> {

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    @Getter
    protected final Configurable config;
    @Getter
    protected final ConfigStructure structure;

    /**
     * Creates a new instance of the specified class using reflection.
     * Uses a cache to improve performance for repeated instantiations.
     *
     * @param clazz The class to instantiate
     * @return A new instance of the specified class
     * @throws RuntimeException if instantiation fails
     */
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
     * Load a set of objects from the configuration given a specific config structure.
     * This method uses the structure's subsection to determine which part of the config to load from.
     *
     * @param clazz The class of the object to load
     * @return A set of objects, or null if loading fails
     */
    public HashSet<T> fromConfig(Class<T> clazz) {
        return fromConfigSection(clazz, null);
    }

    /**
     * Load a set of objects from a specific subsection of the configuration.
     * This method allows loading from a nested section within the structure's subsection.
     *
     * @param clazz The class of the object to load
     * @param nestedSection The name of the nested section to load from, or null to load from the structure's subsection
     * @return A set of objects, or null if loading fails
     */
    protected HashSet<T> fromConfigSection(Class<T> clazz, String nestedSection) {
        if (config == null) {
            return null;
        }

        String configSubPath = structure.getSubsection();
        if (configSubPath == null || configSubPath.isEmpty()) {
            return null;
        }

        ConfigurationSection section = config.getConfiguration().getConfigurationSection(configSubPath);
        if (section == null) {
            return null;
        }

        // If a nested section is specified, get that section
        if (nestedSection != null && !nestedSection.isEmpty()) {
            section = section.getConfigurationSection(nestedSection);
            if (section == null) {
                return null;
            }
        }

        return processConfigSection(clazz, section);
    }

    /**
     * Process a configuration section to extract objects.
     * This method can be overridden by subclasses to provide custom processing logic.
     *
     * @param clazz The class of the object to load
     * @param section The configuration section to process
     * @return A set of objects extracted from the section
     */
    protected HashSet<T> processConfigSection(Class<T> clazz, ConfigurationSection section) {
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
     * Process a configuration section using a custom processor function.
     * This method allows for specialized processing of configuration sections.
     *
     * @param clazz The class of the object to load
     * @param section The configuration section to process
     * @param processor A function that processes each key in the section and returns an object
     * @return A set of objects processed from the section
     */
    protected HashSet<T> processConfigSection(Class<T> clazz, ConfigurationSection section, 
                                             Function<String, T> processor) {
        if (section == null) {
            return new HashSet<>();
        }

        HashSet<T> set = new HashSet<>();

        for (String key : section.getKeys(false)) {
            T data = processor.apply(key);
            if (data != null) {
                set.add(data);
            }
        }

        return set;
    }

    /**
     * Saves a single object to the configuration.
     * Uses the structure to determine how to save the object.
     *
     * @param obj The object to save
     * @return true if the save was successful, false otherwise
     */
    public boolean saveToConfig(T obj) {
        if (obj == null || config == null) {
            return false;
        }

        return structure.save(config, obj);
    }

}
