import be.seeseemelk.mockbukkit.MockBukkit;
import com.cryptomorin.xseries.XMaterial;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.pojo.BankAccount;
import it.mikeslab.econexuslite.pojo.Banknote;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("EcoNexusLite Plugin Test Suite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EcoNexusLiteTest {

    private static EcoNexusLite plugin;

    @BeforeAll
    static void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(EcoNexusLite.class);

        // Temporary workaround for XSeries bug
        try (MockedStatic<Bukkit> mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            mockedBukkit.when(Bukkit::getServer).thenReturn(null);
            XMaterial.matchXMaterial("STONE");
        }

        Assertions.assertTrue(plugin.isDatabaseConnected().join(),
                "Database connection should be established at startup");
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unload();
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigTests {

        @Test
        @Order(1)
        @DisplayName("Should load main and language configuration files")
        void configurationShouldLoadCorrectly() {
            assertAll("Configuration loading",
                    () -> Assertions.assertNotNull(plugin.getConfig(), "Main configuration should not be null"),
                    () -> Assertions.assertNotNull(plugin.getLanguage().getConfiguration(), "Language configuration should not be null")
            );
        }
    }

    @Nested
    @DisplayName("Bank Account Tests")
    class BankAccountTests {

        @Test
        @Order(2)
        @DisplayName("Should perform CRUD operations on bank accounts")
        void bankAccountCrudOperationsShouldWork() {
            UUID testUUID = UUID.randomUUID();
            double initialBalance = 100.0;

            try {
                // create
                boolean accountCreated = plugin.getBankAccountHandler()
                        .addAccount(testUUID, initialBalance, true)
                        .join();

                Assertions.assertTrue(accountCreated, "Account should be created successfully");

                // retrieve
                BankAccount account = plugin.getBankAccountHandler()
                        .findOne(testUUID)
                        .join();

                assertAll("Account retrieval and validation",
                        () -> Assertions.assertNotNull(account, "Retrieved account should not be null"),
                        () -> Assertions.assertEquals(initialBalance, account.getValue(BankAccount.Field.BALANCE), "Account balance should match the initial value")
                );

                BankAccount sameAccount = plugin.getBankAccountHandler()
                        .findOne(testUUID)
                        .join();
                Assertions.assertNotNull(sameAccount, "Account should be retrievable using the same UUID");

                // edit
                double newBalance = 200.0;
                boolean balanceUpdated = plugin.getBankAccountHandler()
                        .edit(testUUID, newBalance, true)
                        .join();

                BankAccount updatedAccount = plugin.getBankAccountHandler()
                        .findOne(testUUID)
                        .join();

                assertAll("Account balance update",
                        () -> Assertions.assertTrue(balanceUpdated, "Account balance should be updated"),
                        () -> Assertions.assertNotNull(updatedAccount, "Updated account should not be null"),
                        () -> Assertions.assertEquals(newBalance, updatedAccount.getValue(BankAccount.Field.BALANCE), "Account balance should match the new value"));

                // delete
                boolean accountDeleted = plugin.getBankAccountHandler()
                        .deleteAccount(testUUID)
                        .join();

                Assertions.assertTrue(accountDeleted, "Account should be deleted successfully");

                BankAccount deletedAccount = plugin.getBankAccountHandler()
                        .findOne(testUUID)
                        .join();

                Assertions.assertNull(deletedAccount, "Account should no longer exist after deletion");

            } finally {
                plugin.getBankAccountHandler().deleteAccount(testUUID).join();
            }
        }

        @Test
        @Order(3)
        @DisplayName("Should not find non-existent account")
        void shouldNotFindNonExistentAccount() {
            UUID randomUUID = UUID.randomUUID();
            BankAccount account = plugin.getBankAccountHandler().findOne(randomUUID).join();
            Assertions.assertNull(account, "Non-existent account should return null");
        }
    }

    @Nested
    @DisplayName("Banknote Tests")
    class BanknoteTests {

        @Test
        @Order(4)
        @DisplayName("Should create and recognize banknotes correctly")
        void banknoteCreationAndRecognitionShouldWork() {
            double testValue = 50.0;
            ItemStack banknote = plugin.getBanknoteHandler().createBanknoteItem(testValue);

            assertAll("Banknote creation and recognition",
                    () -> Assertions.assertNotNull(banknote, "Banknote should be created successfully"),
                    () -> Assertions.assertEquals(Material.PAPER, banknote.getType(), "Banknote should be made of paper by default"),
                    () -> Assertions.assertTrue(plugin.getBanknoteHandler().isBanknote(banknote), "Item should be recognized as a banknote"),
                    () -> Assertions.assertEquals(testValue, plugin.getBanknoteHandler().getBanknoteValue(banknote), "Banknote value should match the test value")
            );

            ItemStack nonBanknote = new ItemStack(Material.STONE);
            Assertions.assertFalse(plugin.getBanknoteHandler().isBanknote(nonBanknote), "Non-banknote item should not be recognized as a banknote");
        }

        @Test
        @Order(5)
        @DisplayName("Should create banknote with correct value")
        void banknoteCreationShouldWork() {
            double testValue = 25.0;
            ItemStack banknote = plugin.getBanknoteHandler().createBanknoteItem(testValue);

            assertAll("Banknote creation",
                    () -> Assertions.assertNotNull(banknote, "Banknote should be created successfully"),
                    () -> Assertions.assertTrue(plugin.getBanknoteHandler().isBanknote(banknote), "Item should be recognized as a banknote"),
                    () -> Assertions.assertEquals(testValue, plugin.getBanknoteHandler().getBanknoteValue(banknote), "Banknote value should match the test value")
            );
        }

        @Test
        @Order(6)
        @DisplayName("Should create Banknote objects correctly")
        void banknoteObjectShouldBeCreatedCorrectly() {
            Banknote banknote = new Banknote(75.0);
            Banknote anotherBanknote = new Banknote(100.0);

            assertAll("Banknote object creation",
                    () -> Assertions.assertNotNull(banknote, "Banknote should be created successfully"),
                    () -> Assertions.assertNotNull(anotherBanknote, "Second banknote should be created successfully")
            );
        }

        @Test
        @Order(7)
        @DisplayName("Should return 0 for invalid banknote value")
        void shouldReturnZeroForInvalidBanknote() {
            ItemStack invalid = new ItemStack(Material.DIRT);
            double value = plugin.getBanknoteHandler().getBanknoteValue(invalid);
            Assertions.assertEquals(0.0, value, "Invalid banknote should return value 0");
        }

        @Test
        @Order(8)
        @DisplayName("Should load banknotes correctly from config")
        void banknoteLoaderShouldWorkCorrectly() {
            // Get the BanknoteLoader from the plugin
            it.mikeslab.econexuslite.bootstrap.BanknoteLoader loader = plugin.getBanknoteLoader();
            Assertions.assertNotNull(loader, "BanknoteLoader should not be null");

            // Load banknotes from config
            HashSet<Banknote> banknotes = loader.fromConfig(Banknote.class);
            Assertions.assertNotNull(banknotes, "Loaded banknotes should not be null");
            Assertions.assertFalse(banknotes.isEmpty(), "Loaded banknotes should not be empty");

            // Verify specific banknotes are loaded
            boolean foundTen = false;
            boolean foundHundred = false;

            for (Banknote banknote : banknotes) {

                double value = banknote.getValue(Banknote.Field.VALUE);

                if (value == 10.0) {
                    foundTen = true;
                } else if (value == 100.0) {
                    foundHundred = true;
                }
            }

            boolean finalFoundHundred = foundHundred;
            boolean finalFoundTen = foundTen;
            assertAll("Specific banknotes loading",
                    () -> Assertions.assertTrue(finalFoundTen, "Banknote with value 10.0 should be loaded"),
                    () -> Assertions.assertTrue(finalFoundHundred, "Banknote with value 100.0 should be loaded")
            );
        }
    }
}
