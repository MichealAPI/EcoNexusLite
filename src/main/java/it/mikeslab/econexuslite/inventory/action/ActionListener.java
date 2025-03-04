package it.mikeslab.econexuslite.inventory.action;

import it.mikeslab.commons.api.inventory.CustomInventory;
import it.mikeslab.commons.api.inventory.pojo.action.GuiAction;
import it.mikeslab.econexuslite.EcoNexusLite;
import it.mikeslab.econexuslite.pojo.Condition;

import java.util.function.Supplier;

public interface ActionListener extends CustomInventory {

    /**
     * Injects an action into the action handler
     * @param prefix the prefix of the action
     * @param action the action to inject
     */
    default void injectAction(EcoNexusLite instance, String prefix, GuiAction action) {

        instance.getActionHandler().injectAction(
                this.getId(),
                prefix,
                action
        );

    }

    /**
     * Injects an action into the action handler with a condition
     * @param prefix the prefix of the action
     * @param action the action to inject
     * @param condition the condition to check before executing the action
     */
    default void injectAction(EcoNexusLite instance, String prefix, GuiAction action, Supplier<Condition> condition) {

        GuiAction mergedAction = new GuiAction((event, args) -> {

            if(!condition.get().isValid()) {
                condition.get().getErrorMessage().ifPresent(s -> event.getWhoClicked().sendMessage(s));
                return;
            }

            action.getAction().accept(event, args);


        });

        this.injectAction(instance, prefix, mergedAction);

    }

}
