import be.seeseemelk.mockbukkit.MockBukkit;
import it.mikeslab.econexuslite.EcoNexusLite;
import org.junit.jupiter.api.*;

class EcoNexusLiteTest {

    private static EcoNexusLite plugin;

    private static final String[] dependencies = {
        "Vault"
    };

    @BeforeAll
    public static void setUp() {

        MockBukkit.mock();
        plugin = MockBukkit.load(EcoNexusLite.class);

        for (String dependency : dependencies) {
            //MockBukkit.createMockPlugin();
        }
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unload();
    }

    @Test
    @DisplayName("Verify that configs have been loaded correctly")
    void testConfigs() {
        Assertions.assertNotNull(plugin.getLanguage().getConfiguration());
        Assertions.assertNotNull(plugin.getLanguage().getConfiguration());
    }

    @Test
    @DisplayName("Verify that inventories have been loaded successfully")
    void testInventories() {
        // todo registrar must be coded
    }

    @Test
    @DisplayName("Verify that Vault Economy has been hooked successfully")
    void testVault() {
        Assertions.assertNotNull(EcoNexusLite.getEcon());
    }


}
