// InventoryTest.java
package inventory;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import it.mikeslab.commons.api.inventory.CustomInventory;
import it.mikeslab.commons.api.inventory.pojo.GuiContext;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.helper.InventoryHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.Test;
import inventory.FakeGuiConfigRegistrar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InventoryTest {

    private ServerMock server;
    private InventoryHelper helper;
    private FakeGuiConfigRegistrar fakeRegistrar;
    private static final String TEST_INVENTORY_KEY = "testInventory";

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        EcoNexusLite plugin = MockBukkit.load(EcoNexusLite.class);

        helper = new InventoryHelper();
        helper.initialize(plugin);

        // Create the fake registrar
        Set<String> keys = new HashSet<>();
        keys.add(TEST_INVENTORY_KEY);
        fakeRegistrar = new FakeGuiConfigRegistrar(plugin, "section", keys);

        // Insert a custom inventory
        CustomInventory customInventory = new CustomInventory() {
            private final Inventory inv = Bukkit.createInventory(null, 9, "Dummy Inventory");

            @Override
            public GuiContext getGuiContext() {
                return null;
            }

            @Override
            public void setGuiContext(GuiContext guiContext) {

            }

            @Override
            public Inventory getInventory() { return inv; }
        };
        UUID playerId = UUID.randomUUID();
        fakeRegistrar.putCachedInventory(playerId, TEST_INVENTORY_KEY, customInventory);

        // Inject the fake registrar into the InventoryHelper
        Field field = InventoryHelper.class.getDeclaredField("guiConfigRegistrar");
        field.setAccessible(true);
        field.set(helper, fakeRegistrar);

        // Add the test key to the static set as well
        Field idsField = InventoryHelper.class.getDeclaredField("INVENTORY_IDENTIFIERS");
        idsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> inventoryIdentifiers = (Set<String>) idsField.get(null);
        inventoryIdentifiers.add(TEST_INVENTORY_KEY);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unload();
    }

    @Test
    public void testOpenInventory() {
        Player player = server.addPlayer();
        // InventoryHelper uses player UUID internally, so ensure it matches our stored custom inventory
        fakeRegistrar.putCachedInventory(player.getUniqueId(), TEST_INVENTORY_KEY,
                fakeRegistrar.getCachedInventoriesByUUID(null).get(TEST_INVENTORY_KEY));

        helper.openInventory(player, TEST_INVENTORY_KEY);

        assertNotNull(player.getOpenInventory());
        assertEquals("Dummy Inventory", player.getOpenInventory().getTopInventory().getName());
    }
}