package it.mikeslab.econexuslite.pojo;

import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.commons.api.database.SerializableMapConvertible;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
public class ConfigStructure {

    private final String keyPlaceholder; // id key
    private HashMap<String, String> specifiers = new HashMap<>(); // key -> value

    @Setter @Getter
    private String subsection; // subsection key

    /**
     * Load the object from the configuration using a given structure
     * @param config The configuration to load from
     * @return The loaded object
     * @param <T> The type of the object to load
     */
    public <T extends SerializableMapConvertible<T>> T load(Configurable config, T obj, Optional<String> path) {

        if (config == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        String tempKey = keyPlaceholder; // id key

        YamlConfiguration yamlCfg = config.getConfiguration();
        String baseKey = path.orElse("");
        baseKey += subsection != null && !subsection.isEmpty() ? subsection + "." + tempKey : tempKey;

        for (String key : yamlCfg.getConfigurationSection(tempKey).getKeys(false)) {

            if (specifiers.containsValue(key)) {

                map.put(specifiers.get(key), yamlCfg.get(baseKey + "." + key));

            } else {

                map.put(key, yamlCfg.get(baseKey + "." + key));

            }

        }

        if (obj == null) {
            return null;
        }

        return obj.fromMap(map);
    }

    /**
     * Save the object to the configuration using a given structure
     * @param config The configuration to save to
     * @param obj The object to save
     * @return True if the save was successful, false otherwise
     * @param <T> The type of the object to save
     */
    public <T extends SerializableMapConvertible<T>> boolean save(Configurable config, T obj) {

        if (obj == null || config == null) {
            return false;
        }

        Map<String, Object> map = obj.toMap();
        String tempKey = keyPlaceholder;

        // Key that will be used as the head of the configuration section
        if (map.containsKey(keyPlaceholder)) {

            tempKey = (String) map.get(keyPlaceholder);
            map.remove(keyPlaceholder);

        }

        YamlConfiguration yamlCfg = config.getConfiguration();
        String baseKey = subsection != null && !subsection.isEmpty() ? subsection + "." + tempKey : tempKey;

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            if (specifiers.containsKey(entry.getKey())) {

                yamlCfg.set(baseKey + "." + specifiers.get(entry.getKey()), entry.getValue());

            } else {

                yamlCfg.set(baseKey + "." + entry.getKey(), entry.getValue());
            }

        }

        // Try to save the configuration
        config.save();
        return true;
    }




}
