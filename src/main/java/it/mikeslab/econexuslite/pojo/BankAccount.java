package it.mikeslab.econexuslite.pojo;

import it.mikeslab.commons.api.database.SerializableMapConvertible;
import it.mikeslab.commons.api.logger.LogUtils;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BankAccount implements SerializableMapConvertible<BankAccount> { // todo check comparison with Identity

    private Map<String, Object> values;
    private UUID ownerUUID;

    public BankAccount(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.values = null;
    }

    @Override
    public BankAccount fromMap(Map<String, Object> map) {

        BankAccount bankAccount = new BankAccount();

        if (map != null) {

            bankAccount.setValues(new HashMap<>(map));

            LogUtils.debug(
                    LogUtils.LogSource.DATABASE,
                    "Retrieved values from Database: " + bankAccount.getValues()
            );

            bankAccount.getValues().remove(this.getUniqueIdentifierName());

        }

        return bankAccount;
    }

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();

        map.put(
                this.getUniqueIdentifierName(),
                this.getUniqueIdentifierValue()
        );

        if (values != null && !values.isEmpty()) {
            LogUtils.debug(
                    LogUtils.LogSource.DATABASE,
                    "Populating a map based on a BankAccount POJO instance which contains: " + values.toString()
            );
            map.putAll(values);
        }

        return map;
    }

    @Override
    public String getUniqueIdentifierName() {
        return "ownerUUID";
    }

    @Override
    public Object getUniqueIdentifierValue() {
        return this.ownerUUID.toString();
    }

    public void setBalance(double balance) {
        this.addValue("balance", balance);
    }

    public double getBalance() {
        return getValue("balance"); // todo extract key
    }

    private <T> T getValue(String key) {
        return (T) this.values.get(key);
    }

    private void addValue(String key, Object value) {

        if (this.values == null) {
            this.values = new HashMap<>();
        }

        this.values.put(key, value);
    }

    @Override
    public Set<String> identifiers() {
        return Set.of("balance");
    }
}
//@AllArgsConstructor
