package it.mikeslab.econexuslite.inventory.config;

import it.mikeslab.commons.api.inventory.CustomInventory;
import it.mikeslab.commons.api.inventory.InventoryType;
import it.mikeslab.commons.api.inventory.helper.InventoryMap;
import it.mikeslab.commons.api.inventory.pojo.GuiContext;
import it.mikeslab.commons.api.logger.LogUtils;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.inventory.DefaultMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class GuiConfigRegistrar {

    private final EcoNexusLite instance;
    private final ConfigurationSection section;

    @Getter
    private String fallbackGuiIdentifier;

    @Getter
    private final InventoryMap playerInventories;

    private final Map<String, GuiRegistrarCache> cache = new HashMap<>();

    @Getter
    private final Map<String, String> mandatoryInventories = new HashMap<>(); // keyId in a pair with displayName
    // which could be defaulted to keyId in case of null
    @Getter
    private int min, max, baseValue;

    private Map<String, CustomInventory> guis;

    // In future this may be needed for EcoNexusPremium
    //@Getter
    //private final InputMenuLoader inputMenuLoader;

    private String inventoryKeyId; // Internal use, rapid switching between keys


    public GuiConfigRegistrar(EcoNexusLite instance, String section) {
        this.instance = instance;
        this.section = instance.getCustomConfig()
                .getConfiguration()
                .getConfigurationSection(section);

        this.playerInventories = new InventoryMap();

        //this.inputMenuLoader = new InputMenuLoader(instance);
    }

    public ConcurrentMap<String, CustomInventory> getCachedInventoriesByUUID(UUID uuid) {

        loadInventoriesFor(uuid, true);

        return playerInventories.getCachedInventories(uuid);

    }

    private void loadInventoriesFor(UUID uuid, boolean checkNullity) {

        if(checkNullity && playerInventories.containsKey(uuid)) {
            return;
        }

        // Possibly heavy operation
        playerInventories.putAll(uuid, this.getGuis());

    }

    private Map<String, CustomInventory> getGuis() {

        if(guis != null && !guis.isEmpty()) {
            return guis;
        }

        this.guis = new HashMap<>();

        for(Map.Entry<String, GuiRegistrarCache> entry : cache.entrySet()) {

            String keyId = entry.getKey();
            this.inventoryKeyId = keyId;

            GuiRegistrarCache cache = entry.getValue();

            guis.put(keyId, createInventory(cache));

        }

        return guis;
    }


    public void unregister() {
        this.guis.clear();
        this.playerInventories.clear();
    }

    /**
     * Register the configuration for each custom inventory
     */
    public void register() {

        this.guis = new HashMap<>();
        this.cache.clear();

        for(String key : section.getKeys(false)) {

            this.inventoryKeyId = key;

            ConfigurationSection configSection = section.getConfigurationSection(key);
            if (configSection == null || !validateConfigSection(configSection)) {
                continue;
            }

            String typeAsString = configSection.getString(ConfigField.TYPE.getField());
            InventoryType type = InventoryType.fromString(typeAsString);
            String displayName = configSection.getString(ConfigField.DISPLAY_NAME.getField(), key); // display name defaults to key
            String path = configSection.getString(ConfigField.PATH.getField());

            if (type == InventoryType.MAIN) {
                this.fallbackGuiIdentifier = key;
            }

            this.cache.put(key, new GuiRegistrarCache(type, path, configSection));

        }

        if(fallbackGuiIdentifier == null) {
            LogUtils.log(
                    Level.SEVERE,
                    LogUtils.LogSource.CONFIG,
                    "No main menu found in the configuration"
            );
            Bukkit.getPluginManager().disablePlugin(instance);
        }
    }

    /**
     * Validate the configuration section
     * @param configSection The configuration section
     * @return Whether the configuration section is valid
     */
    private boolean validateConfigSection(ConfigurationSection configSection) {
        String typeAsString = configSection.getString(ConfigField.TYPE.getField());
        InventoryType type = InventoryType.fromString(typeAsString);
        String path = configSection.getString(ConfigField.PATH.getField());

        if(typeAsString == null) {
            logMissingRequiredField();
            return false;
        }

        if(type == null) {
            logMissingRequiredField();
            return false;
        }

        if(path == null) {
            logMissingRequiredField();
            return false;
        }

        return true;
    }

    private void logMissingRequiredField() {
        LogUtils.log(
                Level.WARNING,
                LogUtils.LogSource.CONFIG,
                "Inventory '" + inventoryKeyId + "' is missing a required field (Required fields:"
                        + ConfigField.TYPE.getField() + ", " + ConfigField.PATH.getField() + ")"
        );
    }


    private CustomInventory createInventory(GuiRegistrarCache cache) {

        // Replace the path with the correct separator
        String path = cache.getPath().replace("\\", File.separator) // todo should I remove this?
                .replace("/", File.separator);

        // Get the file
        File configFile = new File(instance.getDataFolder(), path); // todo add default sub-path /inventories

        // Check if the file exists
        if(!configFile.exists()) {
            LogUtils
                    .log(
                            Level.WARNING,
                            LogUtils.LogSource.CONFIG,
                            "File at '" + configFile.getAbsolutePath() + "' does not exist"
                    );
            return null;
        }

        Path fullPath = configFile.toPath();
        Path rootPath = instance.getDataFolder().toPath();

        // Get the relative path
        Path relativePath = rootPath.relativize(fullPath);

        // Create the gui config

        GuiContext context = GuiContext.builder()
                .fieldIdentifier(inventoryKeyId)
                .relativePath(relativePath)
                .closeOnFail(false) // todo configurable
                .inventoryType(cache.getInventoryType())
                .guiFactory(instance.getGuiFactory())
                .build();



        switch (cache.getInventoryType()) {

            case MAIN: return new DefaultMenu(instance, context);

            default: return null;

            // todo other cases

        }



    }

    public Set<String> getInventoryKeys() {
        return cache.entrySet().stream()
                .filter(entry -> entry.getValue().getInventoryType() != InventoryType.MAIN)
                .map(Map.Entry::getKey)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    @Getter
    @RequiredArgsConstructor
    public enum ConfigField {

        TYPE("type"),
        PATH("path"),
        DISPLAY_NAME("displayName");

        private final String field;

    }


}