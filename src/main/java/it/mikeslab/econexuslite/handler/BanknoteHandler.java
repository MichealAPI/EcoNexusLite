package it.mikeslab.econexuslite.handler;

import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.commons.api.inventory.config.ConfigField;
import it.mikeslab.commons.api.various.item.ItemCreator;
import it.mikeslab.econexuslite.util.NBTUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the creation, recognition, and withdrawal of banknotes
 */
@RequiredArgsConstructor // No longer needs itemCreator injected
public class BanknoteHandler {

    private final JavaPlugin plugin;
    private final Configurable config;

    // Cache for specific banknote configurations
    private final Map<Double, ConfigurationSection> specificBanknoteConfigs = new HashMap<>();

    // --- Config Enums (keep as they are useful) ---
    @Getter
    @RequiredArgsConstructor
    public enum ConfigSection {
        BANKNOTES_SPECIFIC("banknotes.specific"),
        BANKNOTES_BASE("banknotes.base");

        private final String path;
    }

    // Map internal keys to the keys ItemCreator expects (ConfigField)
    // This provides a layer of abstraction if your internal config keys differ.
    private static final Map<String, String> internalToItemCreatorKeys = Map.of(
            "material", ConfigField.MATERIAL.getField(),
            "display-name", ConfigField.DISPLAYNAME.getField(),
            "lore", ConfigField.LORE.getField(),
            "glow", ConfigField.GLOWING.getField(), // Note the key difference: glow vs glowing
            "custom-model-data", ConfigField.CUSTOM_MODEL_DATA.getField()
            // todo Add "amount" if needed, though banknotes are usually single items
            // "amount", ConfigField.AMOUNT.getField()
    );


    /**
     * Initializes the BanknoteHandler
     */
    public void initialize() {
        loadSpecificBanknoteConfigs();
    }

    /**
     * Loads specific banknote configurations from the config file
     */
    private void loadSpecificBanknoteConfigs() {
        specificBanknoteConfigs.clear();
        ConfigurationSection banknoteSection = config.getConfiguration().getConfigurationSection(ConfigSection.BANKNOTES_SPECIFIC.getPath());
        if (banknoteSection == null) return;

        for (String key : banknoteSection.getKeys(false)) {
            try {
                double value = Double.parseDouble(key);
                specificBanknoteConfigs.put(value, banknoteSection.getConfigurationSection(key));
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid banknote value in config: " + key);
            }
        }
    }

    /**
     * Creates a banknote item with the specified value using ItemCreator.create(section, placeholders).
     *
     * @param value The monetary value of the banknote.
     * @return The created banknote item, or null if creation fails.
     */
    public ItemStack createBanknoteItem(double value) {
        ConfigurationSection baseConfig = config.getConfiguration().getConfigurationSection(ConfigSection.BANKNOTES_BASE.getPath());
        ConfigurationSection specificConfig = specificBanknoteConfigs.get(value);

        ConfigurationSection targetConfig = specificConfig != null ? specificConfig : baseConfig;

        if (targetConfig == null) {
            plugin.getLogger().warning("No configuration found for banknote value: " + value);
            return null;
        }

        Map<String, String> placeholders = Map.of("%value%", String.format("%.2f", value));

        ItemStack banknote = ItemCreator.create(targetConfig, placeholders);

        // Set the NBT tag to identify this as a banknote with the specified value
        if (banknote != null) {
            banknote = NBTUtil.setBanknoteValue(banknote, value);
        }

        return banknote;
    }

    // --- Helper methods to get merged configuration values ---
    // These are generic helpers to simplify getting values with specific overriding base.

    private <T> T getValue(ConfigurationSection specific, ConfigurationSection base, String internalKey, T defaultValue) {
        Object value = null;
        if (specific != null && specific.contains(internalKey)) {
            value = specific.get(internalKey);
        } else if (base != null && base.contains(internalKey)) {
            value = base.get(internalKey);
        }

        if (value != null && defaultValue.getClass().isAssignableFrom(value.getClass())) {
            // Special handling for empty lists if default is a non-empty list
            if (value instanceof List && ((List<?>) value).isEmpty() && defaultValue instanceof List && !((List<?>) defaultValue).isEmpty()) {
                return defaultValue;
            }
            //noinspection unchecked
            return (T) value;
        }
        // Bukkit's get() might return null, or the type might be wrong, fallback to default.
        return defaultValue;
    }


    /**
     * Checks if an item is a valid banknote.
     *
     * @param item The item to check.
     * @return true if the item is a valid banknote, false otherwise.
     */
    public boolean isBanknote(ItemStack item) {
        return item != null && NBTUtil.isBanknote(item);
    }

    /**
     * Gets the value of a banknote item from its NBT tag.
     *
     * @param item The banknote item.
     * @return The value of the banknote, or 0 if the item is not a valid banknote.
     */
    public double getBanknoteValue(ItemStack item) {
        return NBTUtil.getBanknoteValue(item); // NBTUtil should handle null checks or return 0
    }

    /**
     * Creates a banknote with the specified value and gives it to the player
     * without removing the actual amount from the player's balance.
     *
     * @param player The player to give the banknote to.
     * @param value  The value of the banknote. Must be positive.
     * @return true if the banknote was successfully given, false otherwise (e.g., invalid value, inventory full, creation failed).
     */
    public boolean withdrawBanknote(Player player, double value) {
        if (value <= 0) {
            plugin.getLogger().warning("Attempted to withdraw banknote with non-positive value: " + value);
            return false;
        }

        ItemStack banknote = createBanknoteItem(value);
        if (banknote == null) {
            // Error already logged in createBanknoteItem
            return false;
        }

        if (player.getInventory().firstEmpty() == -1) {
            // player.sendMessage("Your inventory is full!"); // Optional message
            return false;
        }

        player.getInventory().addItem(banknote);
        // player.sendMessage("You received a banknote worth $" + String.format("%.2f", value)); // Optional message
        return true;
    }

    /**
     * Reloads the banknote configurations.
     */
    public void reload() {
        loadSpecificBanknoteConfigs();
        plugin.getLogger().info("Banknote configurations reloaded.");
    }
}
