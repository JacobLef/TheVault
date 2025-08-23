package model.commandresult;

/**
 * Represents the result of executing a command, such that either a property was extracted from
 * the data returned by the execution of the command or no property/data was extracted. In the
 * event that no property/data was extracted, the methods offered by this interface should never
 * throw an error to stop execution but, rather, return {@code null}.
 *
 * <p>
 *   This container follows the Type-Safe Heterogeneous Container pattern to provide a unified
 *   command result interface that can safely store and retrieve different data types using class
 *   tokens as type witnesses.
 * </p>
 */
public interface CmdResult {
  /**
   * Retrieves the stored property with compile-time safety.
   * @param type the Class token representing the expected type.
   * @return the stored property cast to the specified type or {@code null} if no property is
   *         stored within this CmdResult.
   * @param <T> the type the caller wants to get back.
   * @throws ClassCastException if the stored property cannot be cast to the expected type.
   */
  <T> T getProperty(Class<T> type) throws ClassCastException;

  /**
   * Does this CmdResult have anything to return?
   * @return true -> it does and false otherwise.
   */
  boolean hasProperty();
}
