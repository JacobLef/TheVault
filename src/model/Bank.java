package model;

import model.types.AccountType;
import model.user.BankAccount;
import model.data_engine.DataEngine;

import java.util.List;
import java.util.Objects;

/**
 * Serves as the central place to store all the information regarding a certain Bank, which is
 * delimited by its unique name. A Bank offers all functionality that the Model should offer and
 * stores user information within BankAccount objects.
 *
 * <p>
 *  Bank objects themselves do not store all the information which they are told to store.
 *  Instead, they rely on the DataEngine for all Data related operations. Therefore, a Bank
 *  object does not control the speed of execution of data-related actions nor how the actual
 *  data is stored. It simply acts as an APi through which outside users can gain access to a
 *  Bank-like structure.
 * <p>
 */
public class Bank implements Model<BankAccount> {
  private final DataEngine database;
  private final String bankName;

  /**
   * Constructs a new Bank with respect to the given parameters and generalized DataEngine.
   * Allows for any DataEngine to be used, so long as the public API is the same, allowing for
   * Runtime changes in what DataEngine and thus what backend is used.
   * @param database  The database for this Bank to communicate with.
   * @param bankName  The name of this Bank.
   * @throws NullPointerException if either of the given parameters are {@code null}.
   */
  public Bank(DataEngine database, String bankName) throws NullPointerException {
    Objects.requireNonNull(database);
    Objects.requireNonNull(bankName);

    this.database = database;
    this.bankName = bankName;
  }

  @Override
  public BankAccount makeAccount(
      String accountName,
      AccountType type,
      double... initBalance
  ) throws IllegalArgumentException {
    return null;
  }

  @Override
  public double withdraw(String accountName, double amount) throws IllegalArgumentException {
    throw new IllegalArgumentException("Withdraw for Bank is not finished");
  }

  @Override
  public void deposit(String accountName, double amount) throws IllegalArgumentException {
    throw new IllegalArgumentException("Deposit for Bank is not finished");
  }

  @Override
  public void transfer(String from, String to, double amount) throws IllegalArgumentException {
    throw new IllegalArgumentException("Transfer for Bank is not finished");
  }

  @Override
  public double getBalance(String accountName) throws IllegalArgumentException {
    throw new IllegalArgumentException("getBalance for Bank is not finished");
  }

  @Override
  public List<BankAccount> getAccountsFor(String username) {
    return null;
  }
}
