import be.seeseemelk.mockbukkit.MockBukkit;
import it.mikeslab.commons.api.logger.LogUtils;
import it.mikeslab.econexuslite.EcoNexusLite;
import org.junit.jupiter.api.*;

import java.util.UUID;

class EcoNexusLiteTest {

    private static EcoNexusLite plugin;

    @BeforeAll
    public static void setUp() {

        MockBukkit.mock();
        plugin = MockBukkit.load(EcoNexusLite.class);

        // Check database connection
        Assertions.assertTrue(plugin.isDatabaseConnected().join());
    }

    @AfterAll
    public static void tearDown() {

        MockBukkit.unload();
    }

    @Test
    @Order(0)
    @DisplayName("Verify that configs have been loaded correctly")
    void testConfigs() {
        Assertions.assertNotNull(plugin.getLanguage().getConfiguration());
        Assertions.assertNotNull(plugin.getLanguage().getConfiguration());
    }

    @Test
    @DisplayName("Testing BankAccount handler")
    void testBankAccount() {

        UUID dummyUUID = UUID.randomUUID();

        plugin.getBankAccountHandler()
                .addAccount(dummyUUID, 100.0, true)
                .join();

        plugin.getLogger().info("Dummy Bank account created");

        plugin.getBankAccountHandler()
                .findOne(dummyUUID)
                .thenAccept(account -> {
                    LogUtils.debug(
                            LogUtils.LogSource.DATABASE,
                            "Bank account found: " + account
                    );
                    Assertions.assertNotNull(account);
                    Assertions.assertEquals(100.0, account.getBalance());

                    plugin.getLogger().info("Dummy Bank account found and balance is correct");
                }).thenRun(() -> plugin.getBankAccountHandler().deleteAccount(dummyUUID))
                .join();

    }


}
