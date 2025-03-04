package it.mikeslab.econexuslite.inventory.config.condition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Operand {

    private final String value;

    public double asDouble() {
        return Double.parseDouble(value);
    }

}
