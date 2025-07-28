package model;

/**
 * Serves as a read-only Model such that information can only be read from a ReadableModel
 * instance and no changes can be made to the underlying data.
 */
public interface ReadableModel {
  /**
   * Gets the amount of money currently stored within the account whose identification number
   * belongs the name provided.
   *
   * @param accountName The name of the account which is to be retrieved.
   * @return The balance of the account under the given name as a long.
   * @throws IllegalArgumentException if the given accountName does not exist within this Model.
   */
  long getBalance(String accountName) throws IllegalArgumentException;
}
