package model;

import model.security.PasswordService;
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
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Serves as the central place to store all the information regarding a certain Bank, which is
 * delimited by its unique name. A Bank offers all functionality that the Model should offer and
 * stores user information within the provided DataEngine.
 *
 * <p>
 *  Bank objects themselves do not store all the information which they are told to store.
 *  Instead, they rely on the DataEngine for all Data related operations. Therefore, a Bank
 *  object does not control the speed of execution of data-related actions nor how the actual
 *  data is stored. It simply acts as an API through which outside users can gain access to a
 *  Bank-like structure.
 * <p>
 *
 * <p>
 *   A Bank object relies on the fixed DataEngine schema, such that the following relations must
 *   be guaranteed to be defined by the given DataEngine:
 *    - {@code users} (user authentication and profile data)
 *    - {@code accounts} (account information and balances)
 *    - {@code transactions} (financial transaction records)
 * </p>
 */
public class Bank implements Managed {
  private final DataEngine engine;
  private final PasswordService ps;
  private final String name;
  private final int routingNumber;

  /**
   * Constructs a new Bank with respect to the given parameters and generalized DataEngine.
   * Allows for any DataEngine to be used, so long as the public API is the same, allowing for
   * Runtime changes in what DataEngine and thus what backend is used.
   *
   * @param database The database for this Bank to communicate with.
   * @param ps       The password service for this Bank to encrypt and decrypt passwords with.
   * @param bankName The name of this Bank.
   * @param routingNumber The routing number associated with this Bank. This can be assumed to be
   *                     a delimiter between banks as it is a unique identifier.
   * @throws NullPointerException if either of the given parameters are {@code null}.
   */
  public Bank(
      DataEngine database,
      PasswordService ps,
      String bankName,
      int routingNumber
  ) throws NullPointerException {
    Objects.requireNonNull(database);
    Objects.requireNonNull(bankName);
    Objects.requireNonNull(ps);

    this.engine = database;
    this.ps = ps;
    this.name = bankName;
    this.routingNumber = routingNumber;
  }

  @Override
  public User createUser(
      String username,
      String plaintextPass,
      String email
  ) throws IllegalArgumentException, NullPointerException {
    if (username == null || plaintextPass == null || email == null) {
      throw new NullPointerException("Username, password, and email cannot be null!");
    }

    boolean duplicateUsername = engine.exists(
        this.name, this.routingNumber, "users", Map.of("username", username)
    );
    boolean duplicateEmail = engine.exists(
        this.name, this.routingNumber, "users", Map.of("email", email)
    );
    if (duplicateEmail) {
      throw new IllegalArgumentException("Email is already in use: " + email);
    }
    if (duplicateUsername) {
      throw new IllegalArgumentException("Username is already in use: " + username);
    }

    String hashedPassword = ps.encode(plaintextPass);

    Map<String, Object> userRecord = Map.of(
        "username", username,
        "password", hashedPassword,
        "email", email
    );

    engine.insert(this.name, this.routingNumber, "users", userRecord);

    return new User(username, hashedPassword, email);
  }

  @Override
  public UserLog deleteUser(
      String username,
      String plaintextPass
  ) throws RuntimeException {
    this.checkCredentials(username, plaintextPass);

    Map<String, Object> userRecord = engine.selectOne(
        this.name,
        this.routingNumber,
        "users",
        Map.of("username", username)
    );

    List<Map<String, Object>> accountRecords = engine.select(
        this.name,
        this.routingNumber,
        "accounts",
        Map.of("ownerUsername", username)
    );

    List<Map<String, Object>> transactionRecords = new ArrayList<>();
    for (Map<String, Object> account: accountRecords) {
      String accountName = (String) account.get("accountName");
      List<Map<String, Object>> fromTransactions = engine.select(
          this.name,
          this.routingNumber,
          "transactions",
          Map.of("fromUser", username, "fromAccount", accountName)
      );

      List<Map<String, Object>> toTransactions = engine.select(
          this.name,
          this.routingNumber,
          "transactions",
          Map.of("toUser", username, "toAccount", accountName)
      );

      transactionRecords.addAll(fromTransactions);
      transactionRecords.addAll(toTransactions);
    }

    this.deleteUserData(username, transactionRecords, accountRecords);

    User deletedUser = new User(
        username,
        (String) userRecord.get("password"),
        (String) userRecord.get("email")
    );
    Map<String, BankAccount> deletedAccounts = accountRecords.stream()
        .collect(Collectors.toMap(
            record -> (String) record.get("accountName"),
            record -> new BankAccount(
                (String) record.get("ownerUsername"),
                (String) record.get("accountName"),
                (Double) record.get("balance"),
                (AccountType) record.get("type"),
                (AccountStatus) record.get("status"),
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
      String username,
      String plaintextPass,
      UserProperty prop,
      String newValue
  ) throws IllegalArgumentException {
    this.checkCredentials(username, plaintextPass);

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

    Map<String, Object> userCriteria = Map.of("username", username);
    Map<String, Object> updates;

    if (prop == UserProperty.PASSWORD) {
      updates = Map.of("password", ps.encode(newValue));
    } else {
      updates = Map.of(prop.toString().toLowerCase(), newValue);
    }

    engine.beginTransaction();
    try {
      engine.update(name, routingNumber, "users", userCriteria, updates);

      if (prop == UserProperty.USERNAME) {
        List<Map<String, Object>> userAccounts = engine.select(
            name, routingNumber, "accounts", Map.of("ownerUsername", username)
        );

        for (Map<String, Object> record: userAccounts) {
          engine.update(
              name, routingNumber, "accounts",
              Map.of("ownerUsername", username, "accountName", record.get("accountName")),
              Map.of("ownerUsername", newValue)
          );
        }

        engine.update(
            name, routingNumber, "transactions",
            Map.of("fromUser", username),
            Map.of("fromUser", newValue)
        );

        engine.update(
            name, routingNumber, "transactions",
            Map.of("toUser", username),
            Map.of("toUser", newValue)
        );
      }

      engine.commitTransaction();
    } catch (Exception e) {
      engine.rollbackTransaction();
      throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
    }

    String updatedUsername = prop == UserProperty.USERNAME ? newValue : username;

    Map<String, Object> updatedUserRecord = engine.selectOne(
        name, routingNumber, "users",
        Map.of("username", updatedUsername)
    );

    return new User(
        updatedUsername,
        (String) updatedUserRecord.get("password"),
        (String) updatedUserRecord.get("email")
    );
  }

  @Override
  public BankAccount createAccount(
      String username,
      String plaintextPass,
      String accountName,
      AccountType type,
      double... initBalance
  ) throws IllegalArgumentException {
    if (type == null || accountName == null) {
      throw new NullPointerException("Account type and account name must be provided!");
    }
    this.checkCredentials(username, plaintextPass);

    if (accountExists(username, accountName)) {
      throw new IllegalArgumentException(
          "An account with the following properties already exists: \n"
              + "\tBank: " + this.name + "\n"
              + "\tUsername: " + username + "\n"
              + "\tAccount Name: " + accountName + "\n"
              + "\tAccount Type: " + type + "\n"
      );
    }

    double bal = initBalance.length == 0 ? 0 : initBalance[0];
    engine.insert(
        name, routingNumber, "accounts",
        Map.of(
            "ownerUsername", username,
            "accountName", accountName,
            "type", type.toString(),
            "balance", bal
        )
    );

    return new BankAccount(
        username, accountName, bal, type, AccountStatus.Free, LocalDateTime.now()
    );
  }

  @Override
  public BankAccount deleteAccount(
      String username,
      String plaintextPass,
      String accountName
  ) throws IllegalArgumentException {
    this.checkCredentials(username, plaintextPass);
    if (!accountExists(username, accountName)) {
      throw new IllegalArgumentException(
          "Within bank of name " + name + "The account by the name of " + accountName + " does not "
              + "exist under the user " + username + "."
      );
    }

    double currentBal = (double) this.engine.selectOne(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    ).get("balance");
    if (currentBal != 0) {
      throw new IllegalArgumentException("Cannot delete an account with a balance!");
    }

    engine.update(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName),
        Map.of("status", AccountStatus.Closed)
    );

    Map<String, Object> updatedAccountRecord = engine.selectOne(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    );

    return new BankAccount(
        (String) updatedAccountRecord.get("ownerUsername"),
        (String) updatedAccountRecord.get("accountName"),
        (Double) updatedAccountRecord.get("balance"),
        (AccountType) updatedAccountRecord.get("type"),
        (AccountStatus) updatedAccountRecord.get("status"),
        (LocalDateTime) updatedAccountRecord.get("createdAt")
    );
  }

  private void deleteUserData(
      String username,
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
          Map.of("username", username)
      );

      engine.commitTransaction();
    } catch (Exception e) {
      engine.rollbackTransaction();
      throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
    }
  }

  @Override
  public double withdraw(
      String username,
      String plaintextPass,
      String accountName,
      double amount
  ) throws IllegalArgumentException, IllegalStateException {
    this.withdrawBalance(username, plaintextPass, accountName, amount);

    Map<String, Object> depositRecord = this.createTransactionRecord(
        TransactionType.WITHDRAWAL, null, username, null, accountName, amount, "Withdraw"
    );
    this.engine.insert(name, routingNumber, "transactions", depositRecord);

    return amount;
  }

  private void withdrawBalance(
      String username,
      String plaintextPass,
      String accountName,
      double amount
  ) throws IllegalArgumentException, IllegalStateException {
    if (amount < 0) {
      throw new IllegalArgumentException("Cannot withdraw a balance less than zero!");
    }

    this.checkCredentials(username, plaintextPass);
    if (!accountExists(username, accountName)) {
      throw new IllegalArgumentException(
          "The account with the following properties does not exist: \n"
              + "\tBank: " + this.name + "\n"
              + "\tUsername: " + username + "\n"
              + "\tAccount Name: " + accountName + "\n"
      );
    }

    Map<String, Object> accountRecord = engine.selectOne(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    );
    AccountStatus status = (AccountStatus) accountRecord.get("status");

    if (status == AccountStatus.Frozen) {
      throw new IllegalStateException("Cannot withdraw from a frozen account!");
    }
    if (status == AccountStatus.Closed) {
      throw new IllegalStateException("Cannot withdraw from a closed account!");
    }

    double originalBal = (double) accountRecord.get("balance");
    if (amount > originalBal) {
      throw new IllegalArgumentException(
          "Insufficient funds in account with following properties: "
              + "\n\tBank: " + this.name + "\n"
              + "\tUsername: " + username + "\n"
              + "\tAccount Name: " + accountName + "\n"
              + "\tBalance: " + originalBal + "\n"
              + "\tTried to withdraw: " + amount
      );
    }

    this.engine.update(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName),
        Map.of("balance", originalBal - amount)
    );
  }

  @Override
  public void deposit(
      String username,
      String plaintextPass,
      String accountName,
      double amount
  ) throws IllegalArgumentException {
    this.depositBalance(username, plaintextPass, accountName, amount);

    Map<String, Object> depositRecord = this.createTransactionRecord(
        TransactionType.DEPOSIT, null, username, null, accountName, amount, "Deposit"
    );
    this.engine.insert(name, routingNumber, "transactions", depositRecord);
  }

  private void depositBalance(
      String username,
      String plaintextPass,
      String accountName,
      double amount
  ) {
    if (amount < 0) {
      throw new IllegalArgumentException("Cannot deposit a balance less than zero!");
    }

    this.checkCredentials(username, plaintextPass);
    if (!accountExists(username, accountName)) {
      throw new IllegalArgumentException(
          "The account with the following properties does not exist: \n"
              + "\tBank: " + this.name + "\n"
              + "\tUsername: " + username + "\n"
              + "\tAccount Name: " + accountName + "\n"
              + "\tBalance: " + amount + "\n"
              + "\tTried to deposit: " + amount
      );
    }

    Map<String, Object> accountRecord = engine.selectOne(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    );
    AccountStatus status = (AccountStatus) accountRecord.get("status");
    if (status == AccountStatus.Closed) {
      throw new IllegalStateException("Cannot deposit from a closed account!");
    }
    if (status == AccountStatus.Frozen) {
      throw new IllegalStateException("Cannot deposit from a frozen account!");
    }

    double originalBal = (double) accountRecord.get("balance");
    engine.update(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName),
        Map.of("balance", originalBal + amount)
    );
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
    engine.beginTransaction();
    TransactionType type = fromUserName.equals(toUserName) ?
        TransactionType.INTERNALTRANSFER : TransactionType.EXTERNALTRANSFER;
    try {
      this.withdrawBalance(fromUserName, fromPass, fromAccountName, amount);
      this.depositBalance(toUserName, toPass, toAccountName, amount);
      Map<String, Object> transferRecord = this.createTransactionRecord(
          type, fromUserName, toUserName, fromAccountName, toAccountName, amount, "Transfer"
      );
      this.engine.insert(name, routingNumber, "transactions", transferRecord);
      engine.commitTransaction();
    } catch (Exception e) {
      engine.rollbackTransaction();
      throw new IllegalArgumentException("Transfer Failed: " + e.getMessage(), e);
    }
    return new Transaction(fromUserName, toUserName, fromAccountName, toAccountName, amount, type);
  }

  private Map<String, Object> createTransactionRecord(
      TransactionType type,
      String fromUser,
      String toUser,
      String fromAccount,
      String toAccount,
      double amount,
      String description
  ) {
    Map<String, Object> record = new HashMap<>();
    record.put("transactionType", type.toString());
    record.put("fromUser", fromUser);
    record.put("toUser", toUser);
    record.put("fromAccount", fromAccount);
    record.put("toAccount", toAccount);
    record.put("amount", amount);
    record.put("bankName", this.name);
    record.put("routingNumber", this.routingNumber);
    record.put("description", description);
    record.put("createdAt", LocalDateTime.now());
    record.put("status", "COMPLETED");
    return record;
  }

  @Override
  public double getBalance(
      String username,
      String plaintextPass,
      String accountName
  ) throws IllegalArgumentException {
    this.checkCredentials(username, plaintextPass);
    if (!accountExists(username, accountName)) {
      throw new IllegalArgumentException(
          "The account with the following properties does not exist: \n"
              + "Bank: " + this.name + "\n"
              + "\tUsername: " + username + "\n"
              + "\tAccount Name: " + accountName + "\n"
      );
    }

    return (double) this.engine.selectOne(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    ).get("balance");
  }

  @Override
  public User getUser(String username, String plaintextPass) {
    this.checkCredentials(username, plaintextPass);
    Map<String, Object> userRecord = this.engine.selectOne(
        name, routingNumber, "users",
        Map.of("username", username)
    );
    return new User(
        username,
        (String) userRecord.get("password"),
        (String) userRecord.get("email"),
        (LocalDateTime) userRecord.get("createdAt")
    );
  }

  @Override
  public List<BankAccount> getAccountsFor(
      String username,
      String plaintextPass
  ) throws IllegalArgumentException {
    this.checkCredentials(username, plaintextPass);
    List<Map<String, Object>> accountRecords = this.engine.select(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username)
    );
    return accountRecords.stream()
        .map(record -> new BankAccount(
            (String) record.get("ownerUsername"),
            (String) record.get("accountName"),
            (double) record.get("balance"),
            (AccountType) record.get("type"),
            (AccountStatus) record.get("status"),
            (LocalDateTime) record.get("createdAt")
        )).toList();
  }

  @Override
  public BankAccount getAccountFor(
      String username, String plaintextPass, String accountName
  ) throws IllegalArgumentException {
    this.checkCredentials(username, plaintextPass);
    if (!accountExists(username, accountName)) {
      throw new IllegalArgumentException(
          "The account with the following properties does not exist: \n"
              + "Bank: " + this.name + "\n"
              + "\tUsername: " + username + "\n"
              + "\tAccount Name: " + accountName + "\n"
      );
    }

    Map<String, Object> accountRecord = this.engine.selectOne(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    );
    if (accountRecord == null) {
      return null;
    }
    return new BankAccount(
        (String) accountRecord.get("ownerUsername"),
        (String) accountRecord.get("accountName"),
        (double) accountRecord.get("balance"),
        (AccountType) accountRecord.get("type"),
        (AccountStatus) accountRecord.get("status"),
        (LocalDateTime) accountRecord.get("createdAt")
    );
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean accountExists(String username, String accountName) {
    return engine.exists(
        name, routingNumber, "accounts",
        Map.of("ownerUsername", username, "accountName", accountName)
    );
  }

  @Override
  public boolean userExists(String username, String plaintextPass) {
    Map<String, Object> userRecord = this.engine.selectOne(
        name, routingNumber, "users",
        Map.of("username", username)
    );

    if (userRecord == null) {
      return false;
    }

    String hashedPass = (String) userRecord.get("password");
    return this.ps.verify(plaintextPass, hashedPass);
  }

  @Override
  public Managed getActiveBank() {
    // this method will never throw an exception for Banks.
    return this;
  }

  private void checkCredentials(String username, String plaintextPass) {
    if (!this.userExists(username, plaintextPass)) {
      throw new IllegalArgumentException(
          "Invalid credentials for user: " + username
      );
    }
  }
}