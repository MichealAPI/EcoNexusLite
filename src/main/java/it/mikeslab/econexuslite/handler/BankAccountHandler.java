package it.mikeslab.econexuslite.handler;

import it.mikeslab.commons.api.database.async.AsyncDatabase;
import it.mikeslab.econexuslite.pojo.BankAccount;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
/**
 * This class is responsible for handling bank accounts
 * and simplifying CRUD operations on them.
 */
public class BankAccountHandler {

    private final AsyncDatabase<BankAccount> bankAccountDatabase;


    // create Account, consider possible hook into Vault



    // delete account

    // add money, consider possible hook into Vault

    // remove money, consider possible hook into Vault

}
