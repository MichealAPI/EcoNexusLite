package it.mikeslab.econexuslite.handler;

import it.mikeslab.commons.api.database.async.AsyncDatabase;
import it.mikeslab.econexuslite.pojo.BankAccount;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor

/**
 * This class is responsible for handling bank accounts
 * and simplifying CRUD operations on them.
 */
public class BankAccountHandler {

    private final AsyncDatabase<BankAccount> db;


    // create Account, consider possible hook into Vault
    public CompletableFuture<Boolean> addAccount(UUID targetUUID, double defaultBalance, boolean upsert) {

        BankAccount dummyAccount = new BankAccount(targetUUID);
        dummyAccount.setValue(BankAccount.Field.BALANCE, defaultBalance);

        // First check if account already exists
        return findOne(targetUUID).thenCompose(existingAccount -> {
            // If account exists and upsert is false, return false
            if (existingAccount != null && !upsert) {
                return CompletableFuture.completedFuture(false);
            }

            // Either account doesn't exist or we want to update it
            return db.upsert(dummyAccount).thenApply(result -> true);
        });
    }

    // delete account
    public CompletableFuture<Boolean> deleteAccount(UUID referenceUUID) {

        // Generates a dummy bank account reference with the given UUID
        BankAccount dummyAccount = new BankAccount(referenceUUID);

        // Search and deletes, if exists, the account in the database
        return db.delete(dummyAccount);
    }

    // add money, consider possible hook into Vault

    /**
     * Edits the balance of a bank account identified by the given reference UUID.
     *
     * @param referenceUUID the unique identifier of the bank account to be edited
     * @param amount the value to set or add to the balance
     * @param forceSet if true, the balance is directly set to the specified amount;
     *                 if false, the specified amount is added to the current balance
     * @return a CompletableFuture containing a boolean result: true if the operation was successful,
     *         or false if the bank account does not exist
     */
    public CompletableFuture<Boolean> edit(UUID referenceUUID, double amount, boolean forceSet) {

        CompletableFuture<BankAccount> searchCompletable = this.findOne(referenceUUID);

        // If found, edits the balance amount

        return searchCompletable.thenApply(
                (val) -> {
                    if (val == null) {
                        return false;
                    }

                    // edit balance
                    double targetBalance = forceSet ? amount : (Double) val.getValue(BankAccount.Field.BALANCE) + amount;

                    // todo consider hook into Vault here

                    val.setValue(BankAccount.Field.BALANCE, targetBalance);

                    // Update the account in the database

                    return db.upsert(val).thenApply(result -> true).join();
                }
        );

    }

    // todo cross reference?
    public CompletableFuture<BankAccount> findOne(UUID referenceUUID) {
        BankAccount bankAccount = new BankAccount(referenceUUID);

        // todo in future, it may be possible to reference in another way the Account to then able to user to have more than one only bank account
        return db.findOne(bankAccount);
    }

    // remove money, consider possible hook into Vault

}
