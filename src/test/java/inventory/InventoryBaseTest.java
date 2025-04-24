package inventory;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.cryptomorin.xseries.XMaterial;
import it.mikeslab.econexuslite.EcoNexusLite;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Base test class for inventory tests.
 * Sets up MockBukkit and provides common functionality for inventory tests.
 */
public abstract class InventoryBaseTest {

    protected static ServerMock server;
    protected static EcoNexusLite plugin;
    protected PlayerMock player;

    @BeforeAll
    static void setUpAll() {
        // Initialize MockBukkit
        server = MockBukkit.mock();
        plugin = MockBukkit.load(EcoNexusLite.class);

        // Temporary workaround for XSeries bug
        try (MockedStatic<Bukkit> mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            mockedBukkit.when(Bukkit::getServer).thenReturn(null);
            XMaterial.matchXMaterial("STONE");
        }
    }

    @BeforeEach
    void setUp() {
        // Create a new player for each test
        player = server.addPlayer();
    }

    @AfterAll
    static void tearDownAll() {
        // Clean up MockBukkit
        MockBukkit.unload();
    }
}
