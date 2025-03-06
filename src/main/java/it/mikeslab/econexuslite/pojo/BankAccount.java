package it.mikeslab.econexuslite.pojo;

import it.mikeslab.commons.api.database.SerializableMapConvertible;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BankAccount implements SerializableMapConvertible<BankAccount> { // todo check comparison with Identity

    private UUID ownerUUID;

    @Setter
    private double balance;


    public BankAccount(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    @Override
    public BankAccount fromMap(Map<String, Object> map) {
        return null;
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of();
    }

    @Override
    public String getUniqueIdentifierName() {
        return "ownerUUID";
    }

    @Override
    public Object getUniqueIdentifierValue() {
        return this.ownerUUID;
    }

    @Override
    public Set<String> identifiers() {
        return Set.of();
    }
}
//@AllArgsConstructor
