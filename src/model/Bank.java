package model;

import model.types.AccountType;
import model.types.UserProperty;
import model.user.BankAccount;
import model.data_engine.DataEngine;
import model.user.Transaction;
import model.user.User;
import model.user.UserLog;

import java.util.List;
import java.util.Map;
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
  private final DataEngine engine;
  private final String bankName;
  private final int routingNumber;

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

    this.engine = database;
    this.bankName = bankName;
    this.routingNumber = routingNumber;
  }

  @Override
  public User createUser(
      String userName,
      String password,
      String email
  ) throws IllegalArgumentException, NullPointerException {
    if (userName == null || password == null || email == null) {
      throw new NullPointerException("Username, password, and email cannot be null!");
    }

    checkUsernameAndEmail(userName, email);

    Map<String, Object> userRecord = Map.of(
        "username", userName,
        "password", password,
        "email", email
    );

    engine.insert("users", userRecord);

    return new User(userName, password, email);
  }

  @Override
  public UserLog deleteUser(String userName, String password) throws IllegalArgumentException {
    return null;
  }

  @Override
  public User updateUser(
      String userName,
      String password,
      UserProperty prop,
      String newValue
  ) throws IllegalArgumentException {
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
  public User getUser(String userName, String password) {
    return null;
  }

  @Override
  public List<BankAccount> getAccountsFor(
      String userName,
      String password
  ) throws IllegalArgumentException {
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

  /**
   * Are the given username and email already assigned to an account within this Bank?
   * @param userName    the name of the user to be checked.
   * @param email       the email to be checked.
   * @throws IllegalArgumentException if either the given email or username already exist within
   *         this Bank, associated with a current user.
   */
  private void checkUsernameAndEmail(
      String userName,
      String email
  ) throws IllegalArgumentException {
    boolean duplicateUsername = engine.exists("user", Map.of("username", userName));
    boolean duplicateEmail = engine.exists("user", Map.of("email", email));
    if (duplicateUsername) {
      throw new IllegalArgumentException("Username already exists under a user!" + userName);
    }
    if (duplicateEmail) {
      throw new IllegalArgumentException("Email already exists under a user!" + email);
    }
  }
}