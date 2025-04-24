package it.mikeslab.econexuslite.bootstrap;

import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.econexuslite.pojo.Banknote;
import it.mikeslab.econexuslite.pojo.ConfigStructure;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;

/**
 * Loader for Banknote objects from configuration.
 * Uses the section key as the banknote value (ID) and parses the rest of the data.
 */
public class BanknoteLoader extends Loader<Banknote> {

    public BanknoteLoader(Configurable config, ConfigStructure structure) {
        super(config, structure);
    }

    /**
     * Load banknotes from the configuration.
     * Uses the section key as the banknote value (ID) and parses the rest of the data.
     * 
     * @param clazz The class of the banknote to load
     * @return A set of banknotes loaded from the configuration
     */
    @Override
    public HashSet<Banknote> fromConfig(Class<Banknote> clazz) {
        // Load from the "specific" subsection of the configured section
        return fromConfigSection(clazz, "specific");
    }

    /**
     * Process a configuration section to extract banknote objects.
     * Overrides the default implementation to handle banknote-specific logic.
     * 
     * @param clazz The class of the banknote to load
     * @param section The configuration section to process
     * @return A set of banknotes extracted from the section
     */
    @Override
    protected HashSet<Banknote> processConfigSection(Class<Banknote> clazz, ConfigurationSection section) {
        // Use a custom processor function to create banknotes from section keys
        return processConfigSection(clazz, section, key -> {
            try {
                // Use the section key as the banknote value (ID)
                double value = Double.parseDouble(key);
                return new Banknote(value);
            } catch (NumberFormatException e) {
                // Skip invalid keys
                return null;
            }
        });
    }
}
