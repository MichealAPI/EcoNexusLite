package it.mikeslab.econexuslite.inventory.config;

import it.mikeslab.commons.api.inventory.InventoryType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

@Getter
@RequiredArgsConstructor
public class GuiRegistrarCache {

    private final InventoryType inventoryType;

    private final String path; // Inventory's display name, defaults to its keyId

    private final ConfigurationSection section;

}