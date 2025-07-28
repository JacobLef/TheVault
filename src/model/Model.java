package model;

/**
 * Serves as the primary host of data for the banking application, such that all the information
 * any individual user enters about themselves is stored within a Model.
 *
 * <p>
 * Additionally, a Model offers functionality for CRUD operations which can be acted upon on the
 * data stored in regards the user to which this Model belongs.
 * <p>
 */
public interface Model extends ReadableModel {
  /**
   * Creates a new account under this Model with the given accountName and the provided balance.
   * If no balance is provided, then the default starting balance is 0.
   * @param accountName The name of the new account which is to be created.
   * @param initBalance The starting balance of the new account (optional).
   * @throws IllegalArgumentException if the given accountName already exists.
   */
  void makeAccount(String accountName, long... initBalance) throws IllegalArgumentException;

  /**
   * Withdraws an {@code amount} amount of money out of the given {@code accountName}, if that
   * amount of money can be withdrawn from the given account.
   * @param accountName The name of the account to withdraw from.
   * @param amount The amount of money to withdraw.
   * @return The amount of money withdrawn from the account.
   * @throws IllegalArgumentException if the given accountName does not exist or if there is not
   *         enough money to withdraw.
   */
  long withdrawFrom(String accountName, long amount) throws IllegalArgumentException;

  /**
   * Deposits {@code amount} of money into the given {@code accountName} stored within this Model.
   * @param accountName The name of the account to deposit money into.
   * @param amount The amount of money which is to be deposited into the given account.
   * @throws IllegalArgumentException if the given account does not exist within this Model.
   */
  void depositTo(String accountName, long amount) throws IllegalArgumentException;
}
