package model;

import model.user.BankAccount;

import java.util.List;

/**
 * Serves as a read-only Model such that information can only be read from a ReadableModel
 * instance and no changes can be made to the underlying data.
 *
 * <p>
 * Offers functionality for presence checks regarding users and accounts, and fetching
 * functionality for the balance of accounts as well as of the account(s) themselves that is/are
 * associated with a given user.
 * </p>
 */
public interface ReadableModel {
  /**
   * Gets the amount of money currently stored within the account whose identification number
   * belongs the name provided.
   *
   * @param userName    The name of the user whose account is to be fetched.
   * @param password    The password of the user whose account is to be fetched.
   * @param accountName The name of the account which is to be retrieved.
   * @return The balance of the account under the given name as a long.
   * @throws IllegalArgumentException if the given accountName does not exist within this Model.
   */
  double getBalance(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException;

  /**
   * Gets all the BankAccounts associated with the given username, given in a list.
   * @param userName The name of the user whose accounts are to be fetched.
   * @param password The password of the user whose accounts are to be fetched.
   * @return a List, in any arbitrary order, of all accounts associated with the given name.
   * @throws IllegalArgumentException if the given username does not exist within this
   *         ReadableModel or if the given password is incorrect for the given user.
   */
  List<BankAccount> getAccountsFor(String userName, String password);

  /**
   * Fetches the information associated with the given accountName with respect to the given user.
   * @param userName    The user whose account information is to be fetched.
   * @param password    The password of the given user.
   * @param accountName The name of the account associated with the given user, which is to be
   *                    fetched.
   * @return The BankAccount information associated with the given user and their account.
   * @throws IllegalArgumentException if the given user or account do not exist in this
   *         ReadableModel or if the given password is incorrect.
   */
  BankAccount getAccountFor(
      String userName,
      String password,
      String accountName
  ) throws IllegalArgumentException;

  /**
   * Does the given account exist under the given user in this ReadableModel?
   * @param userName  The user to check under.
   * @param password  The password of the given user.
   * @param accountName The name of the account which is to be checked for its presence.
   * @return true -> does exist, false otherwise.
   */
  boolean accountExists(String userName, String password, String accountName);

  /**
   * Does the given user and password exist within this ReadableModel?
   * @param userName  The user to be checked.
   * @param password  The password of the user to be checked.
   * @return true if the user does exist and false otherwise.
   */
  boolean userExists(String userName, String password);
}
