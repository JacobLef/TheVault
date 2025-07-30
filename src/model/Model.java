package model;

import model.types.AccountType;
import model.user.BankAccount;

/**
 * Serves as the primary host of data for the banking application, acting as a singular bank.
 *
 * <p>
 * Additionally, a Model offers functionality for CRUD operations which can be acted upon on the
 * data within this Model or regards to the Data to which this Model otherwise has access to.
 * <p>
 */
public interface Model<T extends BankAccount> extends ReadableModel {
  /**
   * Creates a new account under this Model with the given accountName and the provided balance.
   * If no balance is provided, then the default starting balance is 0.
   * @param accountName The name of the new account which is to be created.
   * @param initBalance The starting balance of the new account (optional).
   * @param type        The type of Account which is to be added to this Bank.
   * @return The newly created BankAccount stored within this Model.
   * @throws IllegalArgumentException if the given accountName already exists.
   */
   BankAccount makeAccount(
       String accountName,
       AccountType type,
       double... initBalance
   ) throws IllegalArgumentException;

  /**
   * Withdraws an {@code amount} amount of money out of the given {@code accountName}, if that
   * amount of money can be withdrawn from the given account.
   * @param accountName The name of the account to withdraw from.
   * @param amount The amount of money to withdraw.
   * @return The amount of money withdrawn from the account.
   * @throws IllegalArgumentException if the given accountName does not exist or if there is not
   *         enough money to withdraw.
   */
  double withdraw(String accountName, double amount) throws IllegalArgumentException;

  /**
   * Deposits {@code amount} of money into the given {@code accountName} stored within this Model.
   * @param accountName The name of the account to deposit money into.
   * @param amount The amount of money which is to be deposited into the given account.
   * @throws IllegalArgumentException if the given account does not exist within this Model.
   */
  void deposit(String accountName, double amount) throws IllegalArgumentException;

  /**
   * Transfers {@code amount} of money from the given {@code from} account to the given {@code to}
   * account.
   * @param from    The account being withdrawn from.
   * @param to      The account being deposited into.
   * @param amount  The amount to transfer from one account to the other.
   * @throws IllegalArgumentException if either account does not currently exist within this Model
   *         or if the given {@code amount} cannot be feasibly withdrawn from the given {@code}
   *         from account.
   */
  void transfer(String from, String to, double amount) throws IllegalArgumentException;
}
