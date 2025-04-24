package it.mikeslab.econexuslite.pojo;

import it.mikeslab.commons.api.database.util.SimpleMapConvertible;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BankAccount extends SimpleMapConvertible<UUID, BankAccount> {

    public BankAccount(@Nullable UUID ownerUUID) {
        super(ownerUUID, Field.class, Field.OWNER_UUID);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Field implements SimpleIdentifiers {

        BALANCE("balance"),
        OWNER_UUID("ownerUUID"),;

        private final String key;

    }
}
