package inventory;

import com.google.common.collect.Multimap;
import it.mikeslab.commons.api.inventory.pojo.action.GuiAction;
import it.mikeslab.econexuslite.inventory.action.ActionRegistrarImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ActionRegistrarImpl class.
 */
@DisplayName("Action Registrar Tests")
@ExtendWith(MockitoExtension.class)
class ActionRegistrarTest extends InventoryBaseTest {

    private ActionRegistrarImpl actionRegistrar;

    @BeforeEach
    void setUpActionRegistrar() {
        // Create a new action registrar for each test
        actionRegistrar = new ActionRegistrarImpl(plugin);
    }

    @Test
    @DisplayName("Should load actions")
    void shouldLoadActions() {
        // Load the actions
        Multimap<String, GuiAction> actions = actionRegistrar.loadActions();

        // Verify that actions are loaded
        assertNotNull(actions, "Actions should not be null");
        assertFalse(actions.isEmpty(), "Actions should not be empty");

        // Verify that specific actions are loaded
        assertTrue(actions.containsKey("message"), "Actions should contain 'message' action");
        assertTrue(actions.containsKey("player"), "Actions should contain 'player' action");
        assertTrue(actions.containsKey("console"), "Actions should contain 'console' action");
        assertTrue(actions.containsKey("title"), "Actions should contain 'title' action");
        assertTrue(actions.containsKey("sound"), "Actions should contain 'sound' action");
        assertTrue(actions.containsKey("close"), "Actions should contain 'close' action");
        assertTrue(actions.containsKey("potion"), "Actions should contain 'potion' action");
    }

    @Test
    @DisplayName("Should have non-empty action collections")
    void shouldHaveNonEmptyActionCollections() {
        // Load the actions
        Multimap<String, GuiAction> actions = actionRegistrar.loadActions();

        // Verify that each action collection is not empty
        for (String key : actions.keySet()) {
            Collection<GuiAction> actionCollection = actions.get(key);
            assertFalse(actionCollection.isEmpty(), "Action collection for '" + key + "' should not be empty");
        }
    }

    @Test
    @DisplayName("Should have correct number of actions")
    void shouldHaveCorrectNumberOfActions() {
        // Load the actions
        Multimap<String, GuiAction> actions = actionRegistrar.loadActions();

        // Verify that there are 7 action types (as seen in the ActionRegistrarImpl class)
        assertEquals(7, actions.keySet().size(), "There should be 7 action types");

        // Verify that each action type has exactly one action
        for (String key : actions.keySet()) {
            assertEquals(1, actions.get(key).size(), "Action type '" + key + "' should have exactly one action");
        }
    }
}
