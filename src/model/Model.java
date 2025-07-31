package model;

import model.types.AccountType;
import model.types.UserProperty;
import model.user.BankAccount;
import model.user.Transaction;
import model.user.User;
import model.user.UserLog;

/**
 * Serves as the base API for what a Banking application can offer, with the assumption that a
 * Model represents a singular Bank/Banking entity.
 * <p>
 *   A Model offers CRUD operations for a user and the accounts they create, such that they can
 *   be properly created, updated, fetched, or removed.
 * </p>
 */
public interface Model {
  /**
   * Creates a new User for this Model, such that this User can open, close, and update bank
   * accounts of their choosing and creation.
   * @param userName  The name to be put on the account for the User in this Model.
   * @param password  The password to be put on the account for the User of this Model, which
   *                  must be entered before any transaction.
   * @param email     The email associated with this User's account.
   * @return The respective User object that is created as a result of this command.
   */
  User createUser(String userName, String password, String email);

  /**
   * Deletes the given user from this Model such that all the accounts associated with the
   * given User are closed.
   * @param userName  The name of the user whose accounts are to be closed.
   * @param password  The password of the user whose accounts are to be closed.
   * @return A log of all the information associated with the account which was deleted.
   */
  UserLog deleteUser(String userName, String password);

  /**
   * Updates the given property for the provided User within this Model.
   * @param userName  The name of the User who is to be updated within this Model.
   * @param password  The password associated with the given user.
   * @param prop      The property to be updated.
   * @param newValue  The value to now be associated with the given property.
   * @return the new User object associated created after the changes were made.
   */
  User updateUser(String userName, String password, UserProperty prop, String newValue);

  /**
   * Creates a new account under this Model with the given accountName and the provided balance.
   * If no balance is provided, then the default starting balance is 0.
   * @param userName    The name of the User's under whom anb account is to be opened.
   * @param password    The password of the account associated with the given {@code userName}.
   * @param accountName The name of the new account which is to be created.
   * @param initBalance The starting balance of the new account (optional).
   * @param type        The type of Account which is to be added to this Bank.
   * @return The newly created BankAccount stored within this Model.
   * @throws IllegalArgumentException if the given accountName already exists, if the given
   *         userName does not exist in this Model, or if the given password is incorrect for the
   *         given {@code userName}.
   */
  BankAccount createAccount(
      String userName,
      String password,
      String accountName,
      AccountType type,
      double... initBalance
  ) throws IllegalArgumentException;

  /**
   * Deletes the given account associated with the given user.
   * @param userName    The user whose account is to be deleted.
   * @param password    The password of the user whose account is to be deleted.
   * @param accountName The name of the account which is to be deleted.
   * @return The BankAccount which is deleted.
   * @throws IllegalArgumentException if the given user or account do not exist in this Model or
   *         if the given password does not match for the given user.
   */
  BankAccount deleteAccount(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException;

  /**
   * Withdraws an {@code amount} amount of money out of the given {@code accountName}, if that
   * amount of money can be withdrawn from the given account.
   * @param userName    The name of the User who is to have their account withdrawn from.
   * @param password    The password of the account associated with the given userName.
   * @param accountName The name of the account to withdraw from.
   * @param amount The amount of money to withdraw.
   * @return The amount of money withdrawn from the account.
   * @throws IllegalArgumentException if the given accountName does not exist, if there is not
   *         enough money to withdraw, if the given userName does not exist, or if the given
   *         password is not the correct password for the given userName.
   */
  double withdraw(
      String userName,
      String password,
      String accountName,
      double amount
  ) throws IllegalArgumentException;

  /**
   * Deposits {@code amount} of money into the given {@code accountName} stored within this Model.
   * @param userName    The name of the User who is to have money deposited into the given
   *                    accountName.
   * @param password    The password of the given User.
   * @param accountName The name of the account to deposit money into.
   * @param amount The amount of money which is to be deposited into the given account.
   * @throws IllegalArgumentException if the given account does not exist within this Model.
   */
  void deposit(
      String userName,
      String password,
      String accountName,
      double amount
  ) throws IllegalArgumentException;

  /**
   * Transfers {@code amount} of money from the given {@code from} account to the given {@code to}
   * account.
   * @param fromUserName       The user whose account is being withdrawn from.
   * @param toUserName         The user whose account is being deposited to.
   * @param fromPass           The password of the {@code fromUserName}.
   * @param toPass             The password of the {@code toUserName}.
   * @param fromAccountName    The account being withdrawn from.
   * @param toAccountName      The account being deposited into.
   * @param amount             The amount to transfer from one account to the other.
   * @throws IllegalArgumentException if either of the given User does not exist within this
   *         Model, if either of the given passwords are incorrect, if either of the given
   *         accounts do not exist within this Model, or if the given amount cannot be withdrawn
   *         from the user whose account is being withdrawn from.
   */
  Transaction transfer(
      String fromUserName,
      String toUserName,
      String fromPass,
      String toPass,
      String fromAccountName,
      String toAccountName,
      double amount
  ) throws IllegalArgumentException;
}
