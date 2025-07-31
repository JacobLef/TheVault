package model;

import model.types.AccountType;
import model.types.UserProperty;
import model.user.BankAccount;
import model.data_engine.DataEngine;
import model.user.Transaction;
import model.user.User;
import model.user.UserLog;

import java.util.List;
import java.util.Objects;

/**
 * Serves as the central place to store all the information regarding a certain Bank, which is
 * delimited by its unique name. A Bank offers all functionality that the Model should offer and
 * stores user information within User and associated BankAccount objects.
 *
 * <p>
 *  Bank objects themselves do not store all the information which they are told to store.
 *  Instead, they rely on the DataEngine for all Data related operations. Therefore, a Bank
 *  object does not control the speed of execution of data-related actions nor how the actual
 *  data is stored. It simply acts as an API through which outside users can gain access to a
 *  Bank-like structure.
 * <p>
 */
public class Bank implements Managed {
  private final DataEngine database;
  private final String bankName;

  /**
   * Constructs a new Bank with respect to the given parameters and generalized DataEngine.
   * Allows for any DataEngine to be used, so long as the public API is the same, allowing for
   * Runtime changes in what DataEngine and thus what backend is used.
   *
   * @param database The database for this Bank to communicate with.
   * @param bankName The name of this Bank.
   * @throws NullPointerException if either of the given parameters are {@code null}.
   */
  public Bank(DataEngine database, String bankName, int routingNumber) throws NullPointerException {
    Objects.requireNonNull(database);
    Objects.requireNonNull(bankName);

    this.database = database;
    this.bankName = bankName;
  }

  @Override
  public User createUser(String userName, String password, String email) {
    return null;
  }

  @Override
  public UserLog deleteUser(String userName, String password) {
    return null;
  }

  @Override
  public User updateUser(String userName, String password, UserProperty prop, String newValue) {
    return null;
  }

  @Override
  public BankAccount createAccount(
      String userName,
      String password,
      String accountName,
      AccountType type,
      double... initBalance
  ) throws IllegalArgumentException {
    return null;
  }

  @Override
  public BankAccount deleteAccount(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException {
    return null;
  }

  @Override
  public double withdraw(
      String userName,
      String password,
      String accountName,
      double amount
  ) throws IllegalArgumentException {
    return 0;
  }

  @Override
  public void deposit(
      String userName,
      String password,
      String accountName,
      double amount
  ) throws IllegalArgumentException {

  }

  @Override
  public Transaction transfer(
      String fromUserName,
      String toUserName,
      String fromPass,
      String toPass,
      String fromAccountName,
      String toAccountName,
      double amount
  ) throws IllegalArgumentException {
    return null;
  }

  @Override
  public double getBalance(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException {
    return 0;
  }

  @Override
  public List<BankAccount> getAccountsFor(String userName, String password) {
    return List.of();
  }

  @Override
  public BankAccount getAccountFor(
      String userName, String password, String accountName
  ) throws IllegalArgumentException {
    return null;
  }

  @Override
  public boolean accountExists(String userName, String password, String accountName) {
    return false;
  }

  @Override
  public boolean userExists(String userName, String password) {
    return false;
  }
}