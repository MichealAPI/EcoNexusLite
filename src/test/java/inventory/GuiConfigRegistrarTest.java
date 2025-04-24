package inventory;

import it.mikeslab.commons.api.inventory.helper.InventoryMap;
import it.mikeslab.econexuslite.inventory.config.GuiConfigRegistrar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GuiConfigRegistrar class.
 */
@DisplayName("GUI Config Registrar Tests")
class GuiConfigRegistrarTest extends InventoryBaseTest {

    private GuiConfigRegistrar registrar;

    @BeforeEach
    void setUpRegistrar() {
        // Create a new registrar for each test
        registrar = new GuiConfigRegistrar(plugin, "guis");
    }

    @Test
    @DisplayName("Should create GUI config registrar")
    void shouldCreateGuiConfigRegistrar() {
        // Verify that the registrar is created
        assertNotNull(registrar, "GUI config registrar should not be null");
    }

    @Test
    @DisplayName("Should have player inventories")
    void shouldHavePlayerInventories() {
        // Verify that the registrar has player inventories
        InventoryMap playerInventories = registrar.getPlayerInventories();
        assertNotNull(playerInventories, "Player inventories should not be null");
    }
}
