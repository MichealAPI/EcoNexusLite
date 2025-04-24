package it.mikeslab.econexuslite.pojo;

import it.mikeslab.commons.api.database.util.SimpleMapConvertible;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a banknote in the economy system.
 * Banknotes can be withdrawn and deposited by players.
 */
public class Banknote extends SimpleMapConvertible<Double, Banknote> {

    /**
     * Creates a new banknote with the specified value
     * @param value The monetary value of the banknote
     */
    public Banknote(double value) {
        super(value, Field.class, Field.VALUE); // fixme: the super constructor sets the value before it is validated!
        if (value < 0) {
            throw new IllegalArgumentException("Banknote value cannot be negative");
        }
    }

    /**
     * Enum representing the fields of a banknote
     */
    @Getter
    @RequiredArgsConstructor
    public enum Field implements SimpleIdentifiers {
        VALUE("value"),
        MATERIAL("material"),
        DISPLAY_NAME("displayName"),
        LORE("lore"),
        GLOW("glow"),
        CUSTOM_MODEL_DATA("customModelData");

        private final String key;
    }
}
