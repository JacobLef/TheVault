package model.data_engine;

import model.Bank;

import java.util.List;
import java.util.Map;

/**
 * Provides data persistence and retrieval operations for banking domain objects within the
 * Banking application. All banking operations (user management, account operations, and
 * transactions) are performed through this interface.
 *
 * <p>
 *   The DataEngine maintains a fixed relational schema optimized for banking operations, with
 *   predefined tables for users, accounts, and transactions. All implementations must provide
 *   ACID transaction support to ensure Banking data integrity.
 * </p>
 *
 * <p>
 *   This interface abstracts the underlying storage mechanism, allowing different
 *   implementations (in-memory, B+Tree based, and so forth) while maintaining consistent banking
 *   operations.
 * </p>
 */
public interface DataEngine {
  /**
   * Inserts a new record into the specified table.
   *
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.
   * @param tableName the name of the table to insert into.
   * @param record    the key-value pairs representing the record to insert.
   * @throws IllegalArgumentException if tableName is invalid, record violates table constraints,
   *         or if the given bankName and routingNumber do not match.
   * @throws NullPointerException if tableName or record is {@code null}
   */
  void insert(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> record
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Retrieves all records from the specified table that match the given criteria.
   *
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.
   * @param tableName the name of the table to query.
   * @param criteria  the key-value pairs that records must match, or empty map for all records.
   * @return list of matching records as key-value pairs, empty list if no matches found
   * @throws IllegalArgumentException if tableName is invalid, or if the given bankName and
   *         routingNumber do not match.
   * @throws NullPointerException if tableName or criteria is {@code null}
   */
  List<Map<String, Object>> select(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Retrieves the first record from the specified table that matches the given criteria.
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.
   * @param tableName the name of the table to query.
   * @param criteria  the key-value pairs that the record must match.
   * @return the first matching record as key-value pairs, or {@code null} if no match found
   * @throws IllegalArgumentException if tableName is invalid or if the given bank and routingNumber
   *         do not match.
   * @throws NullPointerException if tableName or criteria is {@code null}
   */
  Map<String, Object> selectOne(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Updates all records in the specified table that match the given criteria.
   *
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.
   * @param tableName the name of the table to update.
   * @param criteria  the key-value pairs that records must match to be updated.
   * @param newValues the key-value pairs representing the new field values
   * @throws IllegalArgumentException if tableName is invalid, newValues violate table
   *         constraints, or if the given bank and routing number do not match.
   * @throws NullPointerException if tableName, criteria, or newValues is {@code null}
   */
  void update(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria,
      Map<String, Object> newValues
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Deletes all records from the specified table that match the given criteria.
   *
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.
   * @param tableName  the name of the table to delete from.
   * @param criteria   the key-value pairs that records must match to be deleted.
   * @throws IllegalArgumentException if tableName is invalid, or if the given bank and routing
   *         number do not match.
   * @throws NullPointerException if tableName or criteria is {@code null}
   */
  void delete(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Retrieves all records from the specified table.
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.
   * @param tableName the name of the table to query.
   * @return list of all records in the table as key-value pairs, empty list if table is empty
   * @throws IllegalArgumentException if tableName is invalid, or if the given bank name and
   *         routing number do not match.
   * @throws NullPointerException if tableName is {@code null}
   */
  List<Map<String, Object>> selectAll(
      String bankName,
      int routingNumber,
      String tableName
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Checks whether any records exist in the specified table that match the given criteria.
   * @param bankName  the name of the bank executing this operation.
   * @param routingNumber the routing number associated with the given bank.*
   * @param tableName the name of the table to check.
   * @param criteria  the key-value pairs that records must match.
   * @return {@code true} if at least one matching record exists, {@code false} otherwise
   * @throws IllegalArgumentException if tableName is invalid, or if the given bank name and
   *         routing number do not match.
   * @throws NullPointerException if tableName or criteria is {@code null}
   */
  boolean exists(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Begins a new transaction for subsequent database operations.
   * All operations after this call will be part of the transaction until commit or rollback.
   *
   * @throws IllegalStateException if a transaction is already active
   */
  void beginTransaction() throws IllegalStateException;

  /**
   * Commits the current transaction, making all changes permanent.
   *
   * @throws IllegalStateException if no transaction is currently active
   */
  void commitTransaction() throws IllegalStateException;

  /**
   * Rolls back the current transaction, undoing all changes since the transaction began.
   *
   * @throws IllegalStateException if no transaction is currently active
   */
  void rollbackTransaction() throws IllegalStateException;

  /**
   * Registers a bank with this data engine to enable data operations.
   * @param bank the bank to register
   * @param routingNumber the unique routing number for this bank
   * @throws IllegalArgumentException if the bank name or routing number is already registered
   */
  void registerBank(Bank bank, int routingNumber) throws IllegalArgumentException;
}

