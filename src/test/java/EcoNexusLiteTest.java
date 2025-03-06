import be.seeseemelk.mockbukkit.MockBukkit;
import it.mikeslab.econexuslite.EcoNexusLite;
import org.junit.jupiter.api.*;

class EcoNexusLiteTest {

    private static EcoNexusLite plugin;

    @BeforeAll
    public static void setUp() {

        MockBukkit.mock();
        plugin = MockBukkit.load(EcoNexusLite.class);

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


}
