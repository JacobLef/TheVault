package model;

import model.types.AccountStatus;
import model.types.AccountType;
import model.types.TransactionType;
import model.types.UserProperty;
import model.user.BankAccount;
import model.data_engine.DataEngine;
import model.user.Transaction;
import model.user.User;
import model.user.UserLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
  private final String name;
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
    this.name = bankName;
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

    boolean duplicateUsername = engine.exists(
        this.name, this.routingNumber, "users", Map.of("username", userName)
    );
    boolean duplicateEmail = engine.exists(
        this.name, this.routingNumber, "users", Map.of("email", email)
    );
    if (duplicateEmail) {
      throw new IllegalArgumentException("Email is already in use: " + email);
    }
    if (duplicateUsername) {
      throw new IllegalArgumentException("Username is already in use: " + userName);
    }

    Map<String, Object> userRecord = Map.of(
        "username", userName,
        "password", password,
        "email", email
    );

    engine.insert(this.name, this.routingNumber, "users", userRecord);

    return new User(userName, password, email);
  }

  @Override
  public UserLog deleteUser(
      String userName,
      String password
  ) throws RuntimeException {
    this.checkCredentials(userName, password);

    Map<String, Object> userRecord = engine.selectOne(
        this.name,
        this.routingNumber,
        "users",
        Map.of("username", userName, "password", password)
    );

    List<Map<String, Object>> accounts = engine.select(
        this.name,
        this.routingNumber,
        "accounts",
        Map.of("ownerUsername", userName)
    );

    List<Map<String, Object>> transactionRecords = new ArrayList<>();
    for (Map<String, Object> record: accounts) {
      String accountName = (String) record.get("accountName");
      List<Map<String, Object>> fromTransactions = engine.select(
          this.name,
          this.routingNumber,
          "transactions",
          Map.of("fromUser", userName, "fromAccount", accountName)
      );

      List<Map<String, Object>> toTransactions = engine.select(
          this.name,
          this.routingNumber,
          "transactions",
          Map.of("toUser", userName, "toAccount", accountName)
      );

      transactionRecords.addAll(fromTransactions);
      transactionRecords.addAll(toTransactions);
    }

    this.deleteUserData(userName, password, transactionRecords, accounts);

    User deletedUser = new User(userName, password, (String) userRecord.get("email"));
    Map<String, BankAccount> deletedAccounts = accounts.stream()
        .collect(Collectors.toMap(
            record -> (String) record.get("accountName"),
            record -> new BankAccount(
                (String) record.get("ownerUsername"),
                (String) record.get("accountName"),
                (Double) record.get("balance"),
                (AccountType) record.get("accountType"),
                (AccountStatus) record.get("accountStatus"),
                (LocalDateTime) record.get("createdAt")
            )
        ));

    List<Transaction> deletedTransactions = transactionRecords.stream()
        .map(record -> new Transaction(
            (String) record.get("fromUser"),
            (String) record.get("toUser"),
            (String) record.get("fromAccount"),
            (String) record.get("toAccount"),
            (Double) record.get("amount"),
            (TransactionType) record.get("transactionType"),
            (LocalDateTime) record.get("createdAt")
        )).toList();

    return new UserLog(deletedUser, deletedAccounts, deletedTransactions);
  }

  @Override
  public User updateUser(
      String userName,
      String password,
      UserProperty prop,
      String newValue
  ) throws IllegalArgumentException {
    this.checkCredentials(userName, password);

    if (newValue == null) {
      throw new NullPointerException("New value cannot be null!");
    }

    if (prop == UserProperty.USERNAME) {
      if (engine.exists(name, routingNumber, "users", Map.of("username", newValue))) {
        throw new IllegalArgumentException("Username is already in use: " + newValue);
      }
    } else if (prop == UserProperty.EMAIL) {
      if (engine.exists(name, routingNumber, "users", Map.of("email", newValue))) {
        throw new IllegalArgumentException("Email is already in use: " + newValue);
      }
    }

    Map<String, Object> userCriteria = Map.of("username", userName, "password", password);
    Map<String, Object> updates = Map.of(prop.toString().toLowerCase(), newValue);

    engine.beginTransaction();
    try {
      engine.update(name, routingNumber, "users", userCriteria, updates);

      if (prop == UserProperty.USERNAME) {
        List<Map<String, Object>> userAccounts = engine.select(
            name, routingNumber, "accounts", Map.of("ownerUsername", userName)
        );

        for (Map<String, Object> record: userAccounts) {
          engine.update(
              name, routingNumber, "accounts",
              Map.of("ownerUsername", userName, "accountName", record.get("accountName")),
              Map.of("ownerUsername", newValue)
          );
        }

        engine.update(
            name, routingNumber, "transactions",
            Map.of("fromUser", userName),
            Map.of("fromUser", newValue)
        );

        engine.update(
            name, routingNumber, "transactions",
            Map.of("toUser", userName),
            Map.of("toUser", newValue)
        );
      }

      engine.commitTransaction();
    } catch (Exception e) {
      engine.rollbackTransaction();
      throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
    }

    String updatedUsername = prop == UserProperty.USERNAME ? newValue : userName;
    String updatedPassword = prop == UserProperty.PASSWORD ? newValue : password;

    Map<String, Object> updatedUserRecord = engine.selectOne(
        name, routingNumber, "users",
        Map.of("username", updatedUsername, "password", updatedPassword)
    );

    return new User(updatedUsername, updatedPassword, (String) updatedUserRecord.get("email"));
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
   * Is the given credentials a valid user within this Bank?
   * @param userName the username to be checked.
   * @param password the password to be checked.
   * @throws IllegalArgumentException if the given username does not exist in this Bank or if the
   *         given password does not match for the username.
   */
  private void checkCredentials(String userName, String password) {
    boolean isInvalid = !this.engine.exists(
        this.name,
        this.routingNumber,
        "users",
        Map.of("username", userName, "password", password)
    );
    if (isInvalid) {
      throw new IllegalArgumentException(
          "The given user does not exist within " + this.name + ": \n"
              + "Username: " + userName + "\n" + "Password: " + password
      );
    }
  }

  /**
   * Deletes all user data with respect to the given transaction record, such that it no longer
   * is present in this Bank and can not be fetched again.
   * @param username            the name of the user whose information is to be deleted.
   * @param password            the password of the user whose data is to be deleted.
   * @param transactionRecords  the transaction records from which the information of the
   *                            *from* user is to be deleted.
   * @param accountRecords      the account records which are to be deleted.
   * @implNote it is assumed that the given password is a match for the given username, and that
   *           the given username exists within the Database.
   * @throws RuntimeException if there is any error with deleting the user's data.
   */
  private void deleteUserData(
      String username,
      String password,
      List<Map<String, Object>> transactionRecords,
      List<Map<String, Object>> accountRecords
  ) throws RuntimeException {
    engine.beginTransaction();
    try {
      for (Map<String, Object> trans : transactionRecords) {
        engine.delete(
            this.name,
            this.routingNumber,
            "transactions",
            Map.of("transactionId", trans.get("transactionId"))
        );
      }

      for (Map<String, Object> record : accountRecords) {
        engine.delete(
            this.name,
            this.routingNumber,
            "accounts",
            Map.of("ownerUsername", username, "accountName", record.get("accountName"))
        );
      }

      engine.delete(
          this.name,
          this.routingNumber,
          "users",
          Map.of("username", username, "password", password)
      );

      engine.commitTransaction();
    } catch (Exception e) {
      engine.rollbackTransaction();
      throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
    }
  }
}