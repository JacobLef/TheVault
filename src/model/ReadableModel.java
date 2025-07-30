package model;

import model.user.BankAccount;

import java.util.List;

/**
 * Serves as a read-only Model such that information can only be read from a ReadableModel
 * instance and no changes can be made to the underlying data.
 */
public interface ReadableModel {
  /**
   * Gets the amount of money currently stored within the account whose identification number
   * belongs the name provided.
   *
   * @param accountName The name of the account which is to be retrieved.
   * @return The balance of the account under the given name as a long.
   * @throws IllegalArgumentException if the given accountName does not exist within this Model.
   */
  double getBalance(String accountName) throws IllegalArgumentException;

  /**
   * Gets all of the BankAccounts associated with the given name, given in a list.
   * @param name The name on the accounts to retrieve.
   * @return a List, in any arbitrary order, of all accounts associated with the given name.
   */
  List<BankAccount> getAccountsFor(String name);
}
