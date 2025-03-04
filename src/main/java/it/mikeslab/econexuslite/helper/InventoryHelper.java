package it.mikeslab.econexuslite.helper;

import it.mikeslab.commons.api.inventory.CustomInventory;
import it.mikeslab.commons.api.inventory.config.ConditionParser;
import it.mikeslab.commons.api.inventory.config.GuiConfig;
import it.mikeslab.commons.api.inventory.event.GuiListener;
import it.mikeslab.commons.api.inventory.factory.GuiFactory;
import it.mikeslab.commons.api.inventory.factory.GuiFactoryImpl;
import it.mikeslab.commons.api.inventory.util.action.ActionHandlerImpl;
import it.mikeslab.commons.api.inventory.util.action.ActionRegistrar;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.inventory.action.ActionRegistrarImpl;
import it.mikeslab.econexuslite.inventory.config.GuiConfigRegistrar;
import it.mikeslab.econexuslite.inventory.config.condition.ConditionParserImpl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public class InventoryHelper {

    // todo populate
    private static final HashSet<String> INVENTORY_IDENTIFIERS = new HashSet<>();
    private ActionHandlerImpl actionHandler;
    private ConditionParser conditionParser;
    private ConcurrentHashMap<String, GuiConfig> cachedGuiConfig;
    private GuiFactory guiFactory;
    private GuiListener guiListener;
    private GuiConfigRegistrar guiConfigRegistrar;

    @Getter(AccessLevel.PRIVATE)
    private EcoNexusLite instance;

    public void initialize(EcoNexusLite instance) {

        this.instance = instance;
        this.initInventories();

    }

    private void initInventories() {

        this.cachedGuiConfig = new ConcurrentHashMap<>();

        this.initActions();

        if(guiFactory == null) {
            this.guiFactory = new GuiFactoryImpl(instance);
        }

        if(this.guiListener == null)
            this.guiListener = new GuiListener(guiFactory, instance);

        // todo from config
        this.guiConfigRegistrar = new GuiConfigRegistrar(
                instance,
                Section.GUIS.getFieldName()
        );

        this.guiConfigRegistrar.register();

        INVENTORY_IDENTIFIERS.addAll(
                guiConfigRegistrar.getInventoryKeys()
        );

        this.guiFactory.setActionHandler(actionHandler);
        this.guiFactory.setConditionParser(conditionParser);
        this.guiFactory.setInventoryMap(this.getGuiConfigRegistrar().getPlayerInventories());

    }


    private void initActions() {

        // todo still must be implemented according to the current project structure
        ActionRegistrar actionRegistrar = new ActionRegistrarImpl(instance);

        this.actionHandler = new ActionHandlerImpl(
                actionRegistrar.loadActions()
        );

        // todo still must be implemented according to the current project structure
        this.conditionParser = new ConditionParserImpl();

    }


    public void openInventory(Player player, String targetInventoryIdentifier) {

        UUID playerUUID = player.getUniqueId();

        // First time, it will be loading so may require further waiting

        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {

            // todo populate
            if(!INVENTORY_IDENTIFIERS.contains(targetInventoryIdentifier)) {
                return;
            }

            ConcurrentMap<String, CustomInventory> cachedInventoriesByUUID = this.guiConfigRegistrar
                    .getCachedInventoriesByUUID(playerUUID);

            Inventory inventory = cachedInventoriesByUUID
                    .get(targetInventoryIdentifier)
                    .getInventory();

            if (inventory == null) {
                instance.getLogger().warning("Could not find inventory for " + targetInventoryIdentifier);
                return;
            }

            instance.getServer()
                    .getScheduler()
                    .runTaskLater(instance, () -> player.openInventory(inventory), 1L);

        });
    }


    @Getter
    @RequiredArgsConstructor
    private enum Section {

        GUIS("guis");

        private final String fieldName;

    }

}
