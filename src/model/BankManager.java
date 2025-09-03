package model;

import model.data_engine.DataBase;
import model.data_engine.DataEngine;
import model.security.PasswordService;
import model.security.PasswordServiceImpl;
import model.types.AccountType;
import model.types.UserProperty;
import model.user.BankAccount;
import model.user.Transaction;
import model.user.User;
import model.user.UserLog;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

/**
 * Represents the ability to manage any arbitrary number of Banks, so long as they are delimited
 * by unique names and routing numbers. Any functionality offered by a Bank can be replicated via
 * an instance of a BankManager, so long as the same Bank is used for all transactions.
 */
public class BankManager implements Manager {
  /**
   * Allows for customization of a Bank Manager, such that if no fields are given for the
   * currently active bank, data engine, or password service, they will be set to {@code null},
   * {@code DataBase}, and {@code PasswordServiceImpl}.
   */
  public static class Builder {
    private Managed active;
    private DataEngine de;
    private PasswordService ps;

    /**
     * Constructs a new Builder where ALL fields are set to {@code null}.
     */
    public Builder() {
      this.active = null;
      this.de = null;
      this.ps = null;
    }

    public Builder withBank(Managed m) {
      this.active = m;
      return this;
    }

    public Builder withDataEngine(DataEngine de) {
      this.de = de;
      return this;
    }

    public Builder withPasswordService(PasswordService ps) {
      this.ps = ps;
      return this;
    }

    /**
     * Constructs a new BankManager object whose fields are references to the fields of this
     * Builder. If the data engine or password service are still null when this is invoked, then
     * they will be et to their respective defaults: {@code DataBase} and {@code
     * PasswordServiceImpl}.
     * @return the newly created BankManager object.
     */
    public BankManager build() {
      if (this.de == null) {
        this.de = DataBase.getInstance();
      }

      if (this.ps == null) {
        this.ps = new PasswordServiceImpl();
      }

      return new BankManager(this);
    }
  }

  private Managed active;
  private final Set<String> bankNames;
  private final Set<Integer> routingNumbers;
  private final Map<Integer, Managed> managed;
  private final DataEngine de;
  private final PasswordService ps;

  /**
   * Constructs a new BankManager whose fields reflect those provided by the given builder.
   * @param b the builder whose fields are to be copied to this BankManager.
   */
  private BankManager(Builder b) {
    this.active = b.active;
    this.bankNames = new HashSet<>();
    this.routingNumbers = new HashSet<>();
    this.managed = new HashMap<>();
    this.de = b.de;
    this.ps = b.ps;
  }

  @Override
  public Managed createBank(String bankName, int routingNumber) {

    if (routingNumbers.contains(routingNumber)) {
      throw new IllegalArgumentException("Duplicate routing number: " + routingNumber);
    }

    if (bankNames.contains(bankName)) {
      throw new IllegalArgumentException("Duplicate bank name: " + bankName);
    }

    Managed res = new Bank(this.de, this.ps, bankName, routingNumber);
    this.bankNames.add(bankName);
    this.routingNumbers.add(routingNumber);
    this.managed.put(routingNumber, res);
    return res;
  }

  @Override
  public Managed getBank(int routingNumber) throws IllegalArgumentException {
    validateRoutingNumber(routingNumber);

    return this.managed.get(routingNumber);
  }

  @Override
  public Managed removeBank(int routingNumber) throws IllegalArgumentException {
    validateRoutingNumber(routingNumber);

    Managed res = this.managed.remove(routingNumber);
    if (res == this.active) {
      this.active = null;
    }
    this.routingNumbers.remove(routingNumber);
    this.bankNames.remove(res.getName());
    return res;
  }

  @Override
  public void setActiveBank(int routingNumber) throws IllegalArgumentException {
    this.validateRoutingNumber(routingNumber);

    this.active = this.managed.get(routingNumber);
  }

  /**
   * Validates the given routing number, checking if it is contained within the list of routing
   * numbers stored within this BankManager.
   * @param routingNumber the routing number whose presence is to be validated/checked.
   */
  private void validateRoutingNumber(int routingNumber) {
    if (!routingNumbers.contains(routingNumber)) {
      throw new IllegalArgumentException("Invalid routing number: " + routingNumber);
    }
  }

  @Override
  public Managed getActiveBank() throws UnsupportedOperationException {
    if (this.active == null) {
      throw new UnsupportedOperationException("No active bank found");
    }
    return this.active;
  }

  @Override
  public User createUser(
      String userName,
      String password,
      String email
  ) throws IllegalArgumentException, NullPointerException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.createUser(userName, password, email);
  }

  @Override
  public UserLog deleteUser(String userName, String password) throws RuntimeException {
    hasActiveBank();
    return this.active.deleteUser(userName, password);
  }

  @Override
  public User updateUser(
      String userName,
      String password,
      UserProperty prop,
      String newValue
  ) throws UnsupportedOperationException, IllegalArgumentException {
    hasActiveBank();
    return this.active.updateUser(userName, password, prop, newValue);
  }

  @Override
  public BankAccount createAccount(
      String userName,
      String password,
      String accountName,
      AccountType type,
      double... initBalance
  ) throws IllegalArgumentException, NullPointerException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.createAccount(userName, password, accountName, type, initBalance);
  }

  @Override
  public BankAccount deleteAccount(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.deleteAccount(userName, password, accountName);
  }

  @Override
  public double withdraw(
      String userName,
      String password,
      String accountName,
      double amount
  ) throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.withdraw(userName, password, accountName, amount);
  }

  @Override
  public void deposit(
      String userName,
      String password,
      String accountName,
      double amount
  ) throws IllegalArgumentException, UnsupportedOperationException {
    hasActiveBank();
    this.active.deposit(userName, password, accountName, amount);
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
  ) throws IllegalArgumentException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.transfer(
        fromUserName,
        toUserName,
        fromPass,
        toPass,
        fromAccountName,
        toAccountName,
        amount
    );
  }

  @Override
  public double getBalance(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.getBalance(userName, password, accountName);
  }

  @Override
  public User getUser(String userName, String password) throws UnsupportedOperationException {
    hasActiveBank();
    return this.active.getUser(userName, password);
  }

  @Override
  public List<BankAccount> getAccountsFor(
      String userName,
      String password
  ) throws UnsupportedOperationException {
    hasActiveBank();
    return this.active.getAccountsFor(userName, password);
  }

  @Override
  public BankAccount getAccountFor(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException, UnsupportedOperationException {
    hasActiveBank();
    return this.active.getAccountFor(userName, password, accountName);
  }

  @Override
  public String getName() {
    hasActiveBank();
    return this.active.getName();
  }

  @Override
  public boolean accountExists(
      String userName,
      String accountName
  ) throws UnsupportedOperationException{
    hasActiveBank();
    return this.active.accountExists(userName, accountName);
  }

  @Override
  public boolean userExists(String userName, String password) throws UnsupportedOperationException {
    hasActiveBank();
    return this.active.userExists(userName, password);
  }

  /**
   * Validates that there is a currently active Managed model to dispatch lower-level
   * functionality to (is it {@code null} ?)
   * @throws UnsupportedOperationException if there is no currently active Managed model.
   */
  private void hasActiveBank() throws UnsupportedOperationException {
    if (this.active == null) {
      throw new UnsupportedOperationException("No active bank found to perform operations on.");
    }
  }
}
