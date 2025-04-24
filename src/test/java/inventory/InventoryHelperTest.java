package inventory;

import it.mikeslab.econexuslite.helper.InventoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the InventoryHelper class.
 */
@DisplayName("Inventory Helper Tests")
class InventoryHelperTest extends InventoryBaseTest {

    private InventoryHelper inventoryHelper;

    @BeforeEach
    void setUpInventoryHelper() {
        // Initialize the inventory helper
        inventoryHelper = new InventoryHelper();
    }

    @Test
    @DisplayName("Should create inventory helper")
    void shouldCreateInventoryHelper() {
        // Verify that the inventory helper is created
        assertNotNull(inventoryHelper, "Inventory helper should not be null");
    }

    @Test
    @DisplayName("Should handle null inventory identifiers")
    void shouldHandleNullInventoryIdentifiers() {
        // Try to open an inventory with an invalid identifier
        // This should not throw an exception even though the helper is not fully initialized
        assertDoesNotThrow(() -> inventoryHelper.openInventory(player, "invalid_inventory_id"),
                "Opening an invalid inventory should not throw an exception");
    }
}
