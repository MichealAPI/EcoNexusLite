package it.mikeslab.econexuslite;

import it.mikeslab.commons.LabCommons;
import it.mikeslab.commons.api.config.Configurable;
import it.mikeslab.commons.api.database.Database;
import it.mikeslab.commons.api.database.async.AsyncDatabase;
import it.mikeslab.commons.api.database.async.AsyncDatabaseImpl;
import it.mikeslab.commons.api.database.config.ConfigDatabaseUtil;
import it.mikeslab.commons.api.inventory.config.GuiConfig;
import it.mikeslab.commons.api.inventory.event.GuiListener;
import it.mikeslab.commons.api.inventory.factory.GuiFactory;
import it.mikeslab.commons.api.inventory.util.action.ActionHandler;
import it.mikeslab.commons.api.various.message.MessageHelperImpl;
import it.mikeslab.econexuslite.config.ConfigKey;
import it.mikeslab.econexuslite.config.LanguageKey;
import it.mikeslab.econexuslite.handler.BankAccountHandler;
import it.mikeslab.econexuslite.helper.InventoryHelper;
import it.mikeslab.econexuslite.pojo.BankAccount;
import lombok.AccessLevel;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public final class EcoNexusLite extends JavaPlugin {

    @Getter
    private static Economy econ = null;

    private GuiFactory guiFactory;
    private GuiListener guiListener;

    private LabCommons labCommons;
    private ActionHandler actionHandler;
    private ConcurrentHashMap<String, GuiConfig> cachedConfigs;

    @Getter(AccessLevel.PRIVATE)
    private AsyncDatabase<BankAccount> bankAccountDatabase;

    private BankAccountHandler bankAccountHandler;

    private MessageHelperImpl messageHelper;

    private boolean placeholderAPIEnabled;

    private Configurable language, customConfig;

    private static EcoNexusLite instance;

    // MockBukkit constructor
    public EcoNexusLite(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File f1, File f2) {
        super(loader, descriptionFile, f1, f2);
    }

    @Override
    public void onEnable() {

        // Get the API plug-in instance
        this.labCommons = (LabCommons) this.getServer()
                .getPluginManager()
                .getPlugin("LabCommons");

        if (labCommons != null)
            this.messageHelper = new MessageHelperImpl();

        this.initConfig();

        this.checkDebugMode();

        this.initDatabase().thenAccept(
                success -> {
                    if (success) {
                        this.getLogger().info("Connected to the database");
                    } else {
                        fatalError("Failed to connect to the database. This plugin will be disabled.");
                    }
                }
        );

        this.bankAccountHandler = new BankAccountHandler(
                this.getBankAccountDatabase()
        );

        // Try to hook into Vault
        boolean res = this.setupEconomy();

        if (!res) {

            this.getLogger().info("Vault not found, using internal economy system");

        }
    }

    @Override
    public void onDisable() {

        // disconnect from the database
        this.bankAccountDatabase.disconnect().thenAccept(
                success -> {
                    if(success) {
                        this.getLogger().info("Disconnected from the database");
                    } else {
                        this.getLogger().warning("Failed to disconnect from the database");
                    }
                }
        );

    }


    private void initConfig() {

        File dataFolder = this.isTestEnvironment() ? null : this.getDataFolder();

        if (dataFolder == null) {
            // set to the test/resources folder

            dataFolder = new File("src%test%resources".replace("%", File.separator));

        }


        String customConfigFileName = "config.yml";
        String languageFileName = "language.yml";

        if (!this.isTestEnvironment()) {
            save(customConfigFileName);
            save(languageFileName);
        }

        this.customConfig = LabCommons.registerConfigurable(dataFolder, customConfigFileName, ConfigKey.class);
        this.language = LabCommons.registerConfigurable(dataFolder, languageFileName, LanguageKey.class);
    }

    private CompletableFuture<Boolean> initDatabase() {

        ConfigurationSection section = this.getCustomConfig()
                .getConfiguration()
                .getConfigurationSection("database");

        ConfigDatabaseUtil<BankAccount> configDatabaseUtil = new ConfigDatabaseUtil<>(
                section,
                this.getDataFolder()
        );

        Database<BankAccount> database = configDatabaseUtil.getDatabaseInstance();

        this.bankAccountDatabase = new AsyncDatabaseImpl<>(database);

        if (this.isTestEnvironment()) {
            return CompletableFuture.completedFuture(
                    this.bankAccountDatabase.connect(new BankAccount()).join()
            );
        }

        return this.bankAccountDatabase.connect(new BankAccount());
    }

    public void checkDebugMode() {

        setMongoLoggingToInfo(); // evaluates if the mongo logging should
        // be set to info according to the configuration

        if(this.getCustomConfig().getBoolean(ConfigKey.DEBUG_MODE)) {
            LabCommons.enableDebuggingMode();
        } else {
            LabCommons.disableDebuggingMode();
        }
    }

    private void initInventories() {

        InventoryHelper helper = new InventoryHelper();
        helper.initialize(this);

        this.guiFactory = helper.getGuiFactory();
        this.guiListener = helper.getGuiListener();
        this.actionHandler = helper.getActionHandler();
        this.cachedConfigs = helper.getCachedGuiConfig();

        // todo condition parser & gui config registrar implementation

    }

    /**
     * Reloads the configuration files and inventories
     */
    public void reload() {
        // Reload configuration files
        this.checkDebugMode();

        this.language = this.getLanguage().reload();
        this.customConfig = this.getCustomConfig().reload();

        // Reload inventories
        this.initInventories();
    }

    /*
     * Logs a fatal error and disables the plugin
     */
    private void fatalError(String error) {
        this.getLogger().log(Level.SEVERE, error);
        this.getServer().getPluginManager().disablePlugin(this);
    }

    private void initListeners() {

        this.getServer().getPluginManager().registerEvents(
                guiListener,
                this
        );

    }

    /**
     * Set the mongo logging level to >WARN if the configuration is set to false
     */
    private void setMongoLoggingToInfo() {
        if(!this.getCustomConfig().getBoolean(ConfigKey.MONGO_LOGGING)) {

            if (this.labCommons == null) return;

            LabCommons.disableMongoInfoLogging();
        }
    }

    private void save(String resource) {
        if(!new File(getDataFolder(), resource).exists()) {
            saveResource(resource, false);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void initPlaceholders() {
        // todo
    }

    private boolean isTestEnvironment() {
        return this.getServer().getName().equals("ServerMock");
    }

    public CompletableFuture<Boolean> isDatabaseConnected() {

        if (this.bankAccountDatabase == null) {
            return CompletableFuture.completedFuture(false);
        }

        return this.bankAccountDatabase.isConnected();
    }



}
