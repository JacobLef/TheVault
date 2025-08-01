package model;

import model.data_engine.DataEngine;

/**
 * Serves as a Manager of Models through which all Model functionality is offered, yet only acts
 * on the currently active Model within this Manager.
 *
 * <p>
 *   A Manager offers additional functionality, different from that of a Model, where the current
 *   active Model can be set and the Manager's properties itself can be updated. All other
 *   functionality is the exact same and thus appears as such, so long as a Managed Model is
 *   currently active.
 * </p>
 */
public interface Manager extends Model {
  /**
   * Creates a new Bank with respect to the given parameters.
   * @param bankName        The name of the Bank which is to be created.
   * @param routingNumber   The routing number to be associated with the newly created bank.
   * @return The newly created Bank object.
   * @implNote If no bankName is given (it's an empty string or null), then the bank is to be
   *           given the name of 'Unnamed' as Bank names cannot be null.
   */
  Bank createBank(String bankName, int routingNumber);

  /**
   * Fetches the Bank stored within this Manager that has the respective properties.
   * @param bankName        The name of the Bank to be fetched.
   * @param routingNumber   The routing number associated with the Bank which is to be fetched.
   * @return The Bank associated with the two given properties.
   * @implNote The combination of the two given properties is guaranteed to be a unique identifier.
   * @throws IllegalArgumentException if no Bank can be found given the properties.
   */
  Bank getBank(String bankName, int routingNumber) throws IllegalArgumentException;

  /**
   * Removes the Bank delimited by the given properties from this Manager.
   * @param bankName        The name of the bank to be removed.
   * @param routingNumber   The routing number associated with the target bank.
   * @return The Bank which was removed.
   * @throws IllegalArgumentException if no Bank can be found given the properties.
   */
  Bank removeBank(String bankName, int routingNumber) throws IllegalArgumentException;

  /**
   * Sets the bank delimited by the given properties as the currently active Bank within this
   * Manager, such that all operations performed that are specific to Banks are to be performed
   * on the currently active Bank.
   * @param bankName        The name of the bank to be set to active.
   * @param routingNumber   The routing number of the bank which is to be set to active.
   * @throws IllegalArgumentException if no Bank can be found given the properties.
   */
  void setActiveBank(String bankName, int routingNumber) throws IllegalArgumentException;

  /**
   * Returns the currently active Bank, if there is one.
   * @return The currently active Bank if there is one and {@code null} otherwise.
   */
  Bank getActiveBank();
}
