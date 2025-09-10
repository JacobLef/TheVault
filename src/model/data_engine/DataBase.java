package model.data_engine;

import model.Bank;
import model.bplustree.BPlusTree;
import model.bplustree.BPlusTreeImpl;
import model.data_engine.index.Index;
import model.data_engine.index.IndexImpl;
import model.data_engine.table.TableSchema;
import model.data_engine.table.TableSchemaImpl;
import model.types.AccountStatus;
import model.types.DataType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Singleton database implementation for the banking application that manages
 * multiple banks and their associated data tables. Provides CRUD operations,
 * transaction management, and data persistence using custom B+ tree indexing
 * for efficient account and transaction storage.
 *
 * <p>
 * This class extends DataEngine to add bank registration capabilities while maintaining
 * full compatibility with the core data engine interface.
 * </p>
 */
public class DataBase implements DataEngine {
  private static DataBase instance;
  private final Map<String, TableSchema> tables;
  private final BPlusTree<Integer, Map<String, Object>> userTable;
  private final BPlusTree<Integer, Map<String, Object>> accountTable;
  private final BPlusTree<Integer, Map<String, Object>> transactionTable;
  private final Map<String, Integer> userNameToId;
  private final Map<String, Integer> accountKeyToId;
  private final Map<String, Bank> banks;
  private final Map<Integer, Bank> bankRoutingMap;
  private boolean inTransaction;
  private final List<DatabaseOperation> transactionLog;
  private final AtomicInteger nextUserId = new AtomicInteger(1);
  private final AtomicInteger nextAccountId = new AtomicInteger(1);
  private final AtomicInteger nextTransactionId = new AtomicInteger(1);
  private final Index<String, List<Integer>> usernameIndex;
  private final Index<String, List<Integer>> emailIndex;
  private final Index<String, List<Integer>> ownerUsernameIndex;

  /**
   * Represents a database operation that can be rolled back during transaction management.
   */
  private abstract static class DatabaseOperation {
    abstract void rollback();
  }

  /**
   * Operation for inserting a record that can be rolled back by deleting the inserted record.
   */
  private class InsertOperation extends DatabaseOperation {
    private final String tableName;
    private final Integer recordId;
    private final Map<String, Object> record;

    InsertOperation(String tableName, Integer recordId, Map<String, Object> record) {
      this.tableName = tableName;
      this.recordId = recordId;
      this.record = new HashMap<>(record);
    }

    @Override
    void rollback() {
      deleteRecordFromTable(tableName, recordId, record);
    }
  }

  /**
   * Operation for updating a record that can be rolled back by restoring the original values.
   */
  private class UpdateOperation extends DatabaseOperation {
    private final String tableName;
    private final Integer recordId;
    private final Map<String, Object> oldRecord;
    private final Map<String, Object> newRecord;

    UpdateOperation(
        String tableName,
        Integer recordId,
        Map<String, Object> oldRecord,
        Map<String, Object> newRecord
    ) {
      this.tableName = tableName;
      this.recordId = recordId;
      this.oldRecord = new HashMap<>(oldRecord);
      this.newRecord = new HashMap<>(newRecord);
    }

    @Override
    void rollback() {
      switch (tableName) {
        case "users" -> {
          userTable.insert(recordId, oldRecord);
          updateUserIndexes(newRecord, oldRecord);
        }
        case "accounts" -> {
          accountTable.insert(recordId, oldRecord);
          updateAccountIndexes(newRecord, oldRecord);
        }
        case "transactions" -> transactionTable.insert(recordId, oldRecord);
      }
    }
  }

  /**
   * Operation for deleting a record that can be rolled back by reinserting the deleted record.
   */
  private class DeleteOperation extends DatabaseOperation {
    private final String tableName;
    private final Integer recordId;
    private final Map<String, Object> deletedRecord;

    DeleteOperation(String tableName, Integer recordId, Map<String, Object> deletedRecord) {
      this.tableName = tableName;
      this.recordId = recordId;
      this.deletedRecord = new HashMap<>(deletedRecord);
    }

    @Override
    void rollback() {
      switch (tableName) {
        case "users" -> {
          userTable.insert(recordId, deletedRecord);
          String username = (String) deletedRecord.get("username");
          userNameToId.put(username, recordId);
          updateUsernameIndex(username, recordId);
          updateEmailIndex((String) deletedRecord.get("email"), recordId);
        }
        case "accounts" -> {
          accountTable.insert(recordId, deletedRecord);
          String ownerUsername = (String) deletedRecord.get("ownerUsername");
          String accountName = (String) deletedRecord.get("accountName");
          String accountKey = ownerUsername + ":" + accountName;
          accountKeyToId.put(accountKey, recordId);
          updateOwnerUsernameIndex(ownerUsername, recordId);
        }
        case "transactions" -> transactionTable.insert(recordId, deletedRecord);
      }
    }
  }

  /**
   * Creates a new DataBase such that this DataBase contains the following tables and indexes:
   * - users table with primary key on user_id and secondary indexes on username/email.
   * - accounts table with primary key on account_id and secondary indexes on owner relations.
   * - transactions table with primary key on transaction_id and indexes for querying patterns.
   * All tables are initialized to be empty.
   */
  private DataBase() {
    this.userTable = new BPlusTreeImpl<>();
    this.accountTable = new BPlusTreeImpl<>();
    this.transactionTable = new BPlusTreeImpl<>();

    this.userNameToId = new HashMap<>();
    this.accountKeyToId = new HashMap<>();

    this.banks = new HashMap<>();
    this.bankRoutingMap = new HashMap<>();

    this.inTransaction = false;
    this.transactionLog = new ArrayList<>();

    this.tables = new HashMap<>();
    this.initTables();

    this.usernameIndex = new IndexImpl<>(
        "username_index",
        new BPlusTreeImpl<String, List<Integer>>(),
        false
    );
    this.emailIndex = new IndexImpl<>(
        "email_index",
        new BPlusTreeImpl<String, List<Integer>>(),
        false
    );
    this.ownerUsernameIndex = new IndexImpl<>(
        "owner_username_index",
        new BPlusTreeImpl<String, List<Integer>>(),
        false
    );
  }

  /**
   * Initializes all the tables used in this DataBase (user, accounts, transactions). No
   * information is put into these tables by default.
   */
  private void initTables() {
    this.tables.put("users", new TableSchemaImpl.Builder()
        .tableName("users")
        .addColumn(
            "user_id",
            DataType.INTEGER,
            false,
            true,
            true
        )
        .addColumn(
            "username",
            DataType.STRING,
            false,
            true,
            false
        )
        .addColumn(
            "password",
            DataType.STRING,
            false,
            false,
            false
        )
        .addColumn(
            "email",
            DataType.STRING,
            false,
            true,
            false
        )
        .addColumn(
            "createdAt",
            DataType.DATETIME,
            false,
            false,
            false
        )
        .build()
    );
    this.tables.put("accounts", new TableSchemaImpl.Builder()
        .tableName("accounts")
        .addColumn("account_id", DataType.INTEGER, false, true, true)
        .addColumn("ownerUsername", DataType.STRING, false, false, false)
        .addColumn("accountName", DataType.STRING, false, false, false)
        .addColumn("balance", DataType.DOUBLE, false, false, false)
        .addColumn("type", DataType.ACCOUNT_TYPE, false, false, false)
        .addColumn("status", DataType.ACCOUNT_STATUS, false, false, false)
        .addColumn("createdAt", DataType.DATETIME, false, false, false)
        .build()
    );
    this.tables.put("transactions", new TableSchemaImpl.Builder()
        .tableName("transactions")
        .addColumn("transaction_id", DataType.INTEGER, false, true, true)
        .addColumn("fromUser", DataType.STRING, true, false, false)
        .addColumn("toUser", DataType.STRING, true, false, false)
        .addColumn("fromAccount", DataType.STRING, true, false, false)
        .addColumn("toAccount", DataType.STRING, true, false, false)
        .addColumn("amount", DataType.DOUBLE, false, false, false)
        .addColumn("transactionType", DataType.TRANSACTION_TYPE, false, false, false)
        .addColumn("bankName", DataType.STRING, false, false, false)
        .addColumn("routingNumber", DataType.INTEGER, false, false, false)
        .addColumn("description", DataType.STRING, true, false, false)
        .addColumn("status", DataType.STRING, false, false, false)
        .addColumn("createdAt", DataType.DATETIME, false, false, false)
        .build()
    );
  }

  /**
   * Gets the one instance of this DataBase such that only one object can ever exist (singleton).
   * @return the one DataBase instance stored within this DataBase.
   */
  public static DataBase getInstance() {
    if (instance == null) {
      instance = new DataBase();
    }
    return instance;
  }

  @Override
  public void registerBank(Bank bank, int routingNumber) {
    String bankName = bank.getName();

    if (banks.containsKey(bankName)) {
      throw new IllegalArgumentException("Bank name already registered: " + bankName);
    }

    if (bankRoutingMap.containsKey(routingNumber)) {
      throw new IllegalArgumentException("Routing number already registered: " + routingNumber);
    }

    banks.put(bankName, bank);
    bankRoutingMap.put(routingNumber, bank);
  }

  @Override
  public void insert(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> record
  ) throws IllegalArgumentException, NullPointerException {
    validateInsertParameters(bankName, tableName, record);
    validateBankRegistration(bankName, routingNumber);

    TableSchema schema = tables.get(tableName);
    if (schema == null) {
      throw new IllegalArgumentException("Table does not exist: " + tableName);
    }

    Map<String, Object> recordWithDefaults = new HashMap<>(record);
    addDefaultValues(recordWithDefaults, schema);
    checkRecordValidity(schema, recordWithDefaults);

    switch (tableName) {
      case "users" -> insertUser(recordWithDefaults);
      case "accounts" -> insertAccount(recordWithDefaults);
      case "transactions" -> insertTransaction(recordWithDefaults);
      default -> throw new IllegalArgumentException("Unknown table: " + tableName);
    }
  }

  /**
   * Validates that a bank is properly registered with matching name and routing number.
   * Ensures data integrity by confirming the bank is authorized for database operations.
   *
   * @param bankName the name of the bank to validate
   * @param routingNumber the routing number of the bank to validate
   * @throws IllegalArgumentException if bank name or routing number is not registered,
   *                                  or if they don't match the same bank
   */
  private void validateBankRegistration(String bankName, int routingNumber) {
    Bank bankByName = banks.get(bankName);
    Bank bankByRouting = bankRoutingMap.get(routingNumber);

    if (bankByName == null) {
      throw new IllegalArgumentException("Bank not registered: " + bankName);
    }

    if (bankByRouting == null) {
      throw new IllegalArgumentException("Routing number not registered: " + routingNumber);
    }

    if (bankByName != bankByRouting) {
      throw new IllegalArgumentException(
          "Bank name and routing number do not match the same registered bank: "
              + bankName + " / " + routingNumber
      );
    }
  }

  /**
   * Validates the parameters for insert operation to ensure they meet basic requirements.
   * Checks for null values and empty strings which would indicate invalid operation parameters.
   *
   * @param bankName the name of the bank requesting the operation
   * @param tableName the target table for the insert operation
   * @param record the data record to be inserted
   * @throws NullPointerException if any parameter is null
   * @throws IllegalArgumentException if bank name, table name are empty, or record is empty
   */
  private void validateInsertParameters(
      String bankName,
      String tableName,
      Map<String, Object> record
  ) throws NullPointerException, IllegalArgumentException{
    if (bankName == null || tableName == null || record == null) {
      throw new NullPointerException("Bank name, table name, and record cannot be null");
    }
    if (bankName.trim().isEmpty() || tableName.trim().isEmpty()) {
      throw new IllegalArgumentException("Bank name and table name cannot be empty");
    }
    if (record.isEmpty()) {
      throw new IllegalArgumentException("Record cannot be empty");
    }
  }

  /**
   * Adds default values to a record for fields not provided by the caller.
   * Automatically generates primary keys, timestamps, and default status values
   * to ensure database integrity and consistency across all records.
   *
   * @param record the record to enhance with default values
   * @param schema the table schema defining the structure and constraints
   */
  private void addDefaultValues(Map<String, Object> record, TableSchema schema) {
    if ("users".equals(schema.getTableName()) && !record.containsKey("user_id")) {
      record.put("user_id", nextUserId.getAndIncrement());
    }
    if ("accounts".equals(schema.getTableName()) && !record.containsKey("account_id")) {
      record.put("account_id", nextAccountId.getAndIncrement());
    }
    if ("transactions".equals(schema.getTableName()) && !record.containsKey("transaction_id")) {
      record.put("transaction_id", nextTransactionId.getAndIncrement());
    }
    if (!record.containsKey("createdAt")) {
      record.put("createdAt", LocalDateTime.now());
    }
    if ("accounts".equals(schema.getTableName()) && !record.containsKey("status")) {
      record.put("status", AccountStatus.Free);
    }
  }

  /**
   * Inserts a user record into the users table and maintains associated indexes.
   * Validates uniqueness constraints for username and email before insertion.
   * Updates secondary indexes to enable efficient user lookups by username and email.
   *
   * @param record the complete user record containing all required fields
   * @throws IllegalArgumentException if username or email already exists in the system
   */
  private void insertUser(Map<String, Object> record) {
    Integer userId = (Integer) record.get("user_id");
    String username = (String) record.get("username");
    String email = (String) record.get("email");

    if (userNameToId.containsKey(username)) {
      throw new IllegalArgumentException("Username already exists: " + username);
    }
    if (emailExists(email)) {
      throw new IllegalArgumentException("Email already exists: " + email);
    }

    userTable.insert(userId, record);
    userNameToId.put(username, userId);
    updateUsernameIndex(username, userId);
    updateEmailIndex(email, userId);

    if (inTransaction) {
      transactionLog.add(new InsertOperation("users", userId, record));
    }
  }

  /**
   * Inserts an account record into the accounts table and maintains ownership indexes.
   * Validates account uniqueness by checking the combination of owner username and account name.
   * Updates secondary indexes to support efficient account lookups by owner relationships.
   *
   * @param record the complete account record containing all required banking information
   * @throws IllegalArgumentException if an account already exists for the given username and
   *         account name combination
   */
  private void insertAccount(Map<String, Object> record) {
    Integer accountId = (Integer) record.get("account_id");
    String ownerUsername = (String) record.get("ownerUsername");
    String accountName = (String) record.get("accountName");

    String accountKey = ownerUsername + ":" + accountName;
    if (accountKeyToId.containsKey(accountKey)) {
      throw new IllegalArgumentException(
          "Account already exists for user: " + ownerUsername + ", account: " + accountName
      );
    }

    accountTable.insert(accountId, record);
    accountKeyToId.put(accountKey, accountId);
    updateOwnerUsernameIndex(ownerUsername, accountId);

    if (inTransaction) {
      transactionLog.add(new InsertOperation("accounts", accountId, record));
    }
  }

  /**
   * Inserts a transaction record into the transactions table for financial audit trails.
   * Stores complete transaction details including parties, amounts, and metadata
   * required for banking compliance and transaction history tracking.
   *
   * @param record the complete transaction record containing all financial transaction data
   */
  private void insertTransaction(Map<String, Object> record) {
    Integer transactionId = (Integer) record.get("transaction_id");
    transactionTable.insert(transactionId, record);

    if (inTransaction) {
      transactionLog.add(new InsertOperation("transactions", transactionId, record));
    }
  }

  @Override
  public List<Map<String, Object>> select(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    validateSelectParameters(bankName, tableName, criteria);
    validateBankRegistration(bankName, routingNumber);

    return switch (tableName) {
      case "users" -> selectUsers(criteria);
      case "accounts" -> selectAccounts(criteria);
      case "transactions" -> selectTransactions(criteria);
      default -> throw new IllegalArgumentException("Unknown table: " + tableName);
    };
  }

  /**
   * Validates parameters for select operations to ensure they meet basic requirements.
   * Performs null checks on all required parameters to prevent runtime failures
   * during query execution and data retrieval operations.
   *
   * @param bankName the name of the bank requesting the operation
   * @param tableName the target table for the select operation
   * @param criteria the search criteria to filter records
   * @throws NullPointerException if any parameter is null
   */
  private void validateSelectParameters(
      String bankName,
      String tableName,
      Map<String, Object> criteria
  ) throws NullPointerException {
    if (bankName == null || tableName == null || criteria == null) {
      throw new NullPointerException("Bank name, table name, and criteria cannot be null");
    }
  }

  /**
   * Selects users based on search criteria with optimized index usage.
   * Uses the username index for direct lookups when username criteria is provided,
   * otherwise performs full table scan with criteria filtering for maximum flexibility.
   *
   * @param criteria the search criteria to match against user records
   * @return list of user records matching the specified criteria, empty list if no matches found
   */
  private List<Map<String, Object>> selectUsers(Map<String, Object> criteria) {
    if (criteria.isEmpty()) {
      return getAllUsers();
    }

    if (criteria.containsKey("username")) {
      String username = (String) criteria.get("username");
      return getRecordById(username, userNameToId, userTable);
    }

    return getAllUsers().stream()
        .filter(user -> matchesCriteria(user, criteria))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a single record by looking up its ID in the specified key-to-ID mapping
   * and then fetching the complete record from the corresponding B+ tree table.
   *
   * @param key the key to look up in the mapping
   * @param keyToIdMap the mapping from keys to record IDs
   * @param table the B+ tree table containing the actual records
   * @return list containing the found record, or empty list if not found
   */
  private List<Map<String, Object>> getRecordById(
      String key,
      Map<String, Integer> keyToIdMap,
      BPlusTree<Integer, Map<String, Object>> table
  ) {
    Integer id = keyToIdMap.get(key);
    if (id != null) {
      Map<String, Object> record = table.get(id);
      return record != null ? List.of(record) : List.of();
    }
    return List.of();
  }

  /**
   * Retrieves all users from the users table by iterating through the B+ tree structure.
   * Provides complete table scan functionality for operations requiring access to all user records
   * such as administrative reporting or bulk operations.
   *
   * @return list containing all user records currently stored in the users table
   */
  private List<Map<String, Object>> getAllUsers() {
    List<Map<String, Object>> results = new ArrayList<>();
    userTable.iterator().forEachRemaining(results::add);
    return results;
  }

  /**
   * Selects accounts based on search criteria with optimized lookup strategies.
   * Uses composite key lookups for owner-account name combinations, owner username filtering
   * for user-specific queries, or full table scan for complex criteria matching.
   *
   * @param criteria the search criteria to match against account records
   * @return list of account records matching the specified criteria, empty list if no matches found
   */
  private List<Map<String, Object>> selectAccounts(Map<String, Object> criteria) {
    if (criteria.isEmpty()) {
      return getAllAccounts();
    }

    if (criteria.containsKey("ownerUsername") && criteria.containsKey("accountName")) {
      String ownerUsername = (String) criteria.get("ownerUsername");
      String accountName = (String) criteria.get("accountName");
      String accountKey = ownerUsername + ":" + accountName;
      return getRecordById(accountKey, accountKeyToId, accountTable);
    }

    if (criteria.containsKey("ownerUsername")) {
      String ownerUsername = (String) criteria.get("ownerUsername");
      return getAllAccounts().stream()
          .filter(account -> ownerUsername.equals(account.get("ownerUsername")))
          .collect(Collectors.toList());
    }

    return getAllAccounts().stream()
        .filter(account -> matchesCriteria(account, criteria))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all accounts from the accounts table by iterating through the B+ tree structure.
   * Provides complete table scan functionality for banking operations requiring access to all account records
   * such as balance reporting, account auditing, or administrative oversight.
   *
   * @return list containing all account records currently stored in the accounts table
   */
  private List<Map<String, Object>> getAllAccounts() {
    List<Map<String, Object>> results = new ArrayList<>();
    accountTable.iterator().forEachRemaining(results::add);
    return results;
  }

  /**
   * Selects transactions based on search criteria with full table scan capability.
   * Provides flexible transaction history retrieval supporting complex filtering criteria
   * required for financial auditing, reporting, and compliance operations.
   *
   * @param criteria the search criteria to match against transaction records
   * @return list of transaction records matching the specified criteria, empty list if no
   *         matches found
   */
  private List<Map<String, Object>> selectTransactions(Map<String, Object> criteria) {
    if (criteria.isEmpty()) {
      return getAllTransactions();
    }

    return getAllTransactions().stream()
        .filter(transaction -> matchesCriteria(transaction, criteria))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all transactions from the transactions table by iterating through the B+ tree
   * structure. Provides complete table scan functionality for financial operations requiring
   * access to all transaction records such as audit trails, compliance reporting, or
   * comprehensive financial analysis.
   *
   * @return list containing all transaction records currently stored in the transactions table
   */
  private List<Map<String, Object>> getAllTransactions() {
    List<Map<String, Object>> results = new ArrayList<>();
    transactionTable.iterator().forEachRemaining(results::add);
    return results;
  }

  @Override
  public Map<String, Object> selectOne(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    validateBankRegistration(bankName, routingNumber);
    List<Map<String, Object>> results = select(bankName, routingNumber, tableName, criteria);
    return results.isEmpty() ? null : results.get(0);
  }

  @Override
  public void update(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria,
      Map<String, Object> newValues
  ) throws IllegalArgumentException, NullPointerException {
    validateUpdateParameters(bankName, tableName, criteria, newValues);
    validateBankRegistration(bankName, routingNumber);

    List<Map<String, Object>> recordsToUpdate = select(
        bankName,
        routingNumber,
        tableName,
        criteria
    );

    for (Map<String, Object> record : recordsToUpdate) {
      updateRecord(tableName, record, newValues);
    }
  }

  /**
   * Validates parameters for update operations to ensure they meet basic requirements.
   * Performs null checks on all required parameters to prevent runtime failures during
   * record modification operations and maintain database integrity.
   *
   * @param bankName the name of the bank requesting the operation
   * @param tableName the target table for the update operation
   * @param criteria the search criteria to identify records for updating
   * @param newValues the new field values to apply to matching records
   * @throws NullPointerException if any parameter is null
   */
  private void validateUpdateParameters(
      String bankName,
      String tableName,
      Map<String, Object> criteria,
      Map<String, Object> newValues
  ) throws NullPointerException {
    if (bankName == null || tableName == null || criteria == null || newValues == null) {
      throw new NullPointerException("Parameters cannot be null");
    }
  }

  /**
   * Updates a single record with new values and maintains all relevant indexes.
   * Merges new values with existing record data and updates the appropriate B+ tree storage.
   * Handles index maintenance for username and ownership relationships when modified.
   *
   * @param tableName the name of the table containing the record to update
   * @param record the existing record to be modified
   * @param newValues the new field values to apply to the record
   */
  private void updateRecord(
      String tableName,
      Map<String, Object> record,
      Map<String, Object> newValues
  ) {
    Integer id = (Integer) record.get(getTablePrimaryKey(tableName));

    // Check for primary key modification (not allowed)
    String primaryKeyCol = getTablePrimaryKey(tableName);
    if (newValues.containsKey(primaryKeyCol)) {
      throw new IllegalArgumentException("Cannot modify primary key field: " + primaryKeyCol);
    }

    Map<String, Object> updatedRecord = new HashMap<>(record);
    updatedRecord.putAll(newValues);

    if (inTransaction) {
      transactionLog.add(new UpdateOperation(tableName, id, record, updatedRecord));
    }

    switch (tableName) {
      case "users" -> {
        if (newValues.containsKey("username")) {
          String newUsername = (String) newValues.get("username");
          String oldUsername = (String) record.get("username");
          if (!newUsername.equals(oldUsername) && userNameToId.containsKey(newUsername)) {
            throw new IllegalArgumentException("Username already exists: " + newUsername);
          }
        }
        if (newValues.containsKey("email")) {
          String newEmail = (String) newValues.get("email");
          String oldEmail = (String) record.get("email");
          if (!newEmail.equals(oldEmail) && emailExists(newEmail)) {
            throw new IllegalArgumentException("Email already exists: " + newEmail);
          }
        }

        userTable.insert(id, updatedRecord);
        updateUserIndexes(record, updatedRecord);
      }
      case "accounts" -> {
        if (newValues.containsKey("accountName") || newValues.containsKey("ownerUsername")) {
          String newOwnerUsername = (String) updatedRecord.get("ownerUsername");
          String newAccountName = (String) updatedRecord.get("accountName");
          String newAccountKey = newOwnerUsername + ":" + newAccountName;

          String oldOwnerUsername = (String) record.get("ownerUsername");
          String oldAccountName = (String) record.get("accountName");
          String oldAccountKey = oldOwnerUsername + ":" + oldAccountName;

          if (!newAccountKey.equals(oldAccountKey) && accountKeyToId.containsKey(newAccountKey)) {
            throw new IllegalArgumentException("Account already exists: " + newAccountKey);
          }
        }

        accountTable.insert(id, updatedRecord);
        updateAccountIndexes(record, updatedRecord);
      }
      case "transactions" -> transactionTable.insert(id, updatedRecord);
    }
  }

  /**
   * Determines the primary key column name for the specified table.
   * Maps table names to their respective primary key identifiers to support
   * generic record operations across different table structures.
   *
   * @param tableName the name of the table to get the primary key for
   * @return the primary key column name for the specified table
   * @throws IllegalArgumentException if the table name is not recognized
   */
  private String getTablePrimaryKey(String tableName) {
    return switch (tableName) {
      case "users" -> "user_id";
      case "accounts" -> "account_id";
      case "transactions" -> "transaction_id";
      default -> throw new IllegalArgumentException("Unknown table: " + tableName);
    };
  }

  @Override
  public void delete(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    if (bankName == null || tableName == null || criteria == null) {
      throw new NullPointerException("Parameters cannot be null");
    }

    validateBankRegistration(bankName, routingNumber);

    List<Map<String, Object>> recordsToDelete = select(bankName, routingNumber, tableName, criteria);

    for (Map<String, Object> record : recordsToDelete) {
      deleteRecord(tableName, record);
    }
  }

  /**
   * Deletes a single record from the specified table and maintains all relevant indexes.
   * Removes the record from the primary B+ tree storage and cleans up all secondary indexes
   * to maintain database consistency and prevent orphaned index entries.
   *
   * @param tableName the name of the table containing the record to delete
   * @param record the record to be removed from the database
   */
  private void deleteRecord(String tableName, Map<String, Object> record) {
    Integer id = (Integer) record.get(getTablePrimaryKey(tableName));

    if (inTransaction) {
      transactionLog.add(new DeleteOperation(tableName, id, record));
    }

    deleteRecordFromTable(tableName, id, record);
  }

  /**
   * Executes the physical deletion of a record from the specified table and all associated indexes.
   * Removes the record from the primary B+ tree and cleans up all secondary index mappings.
   * This method contains the actual deletion logic used by both normal operations and rollback
   * operations.
   *
   * @param tableName the name of the table to delete from
   * @param recordId the primary key ID of the record to delete
   * @param record the complete record data for index cleanup
   */
  private void deleteRecordFromTable(
      String tableName,
      Integer recordId,
      Map<String, Object> record
  ) {
    switch (tableName) {
      case "users" -> {
        userTable.delete(recordId);
        String username = (String) record.get("username");
        userNameToId.remove(username);
        removeFromUsernameIndex(username, recordId);
        removeFromEmailIndex((String) record.get("email"), recordId);
      }
      case "accounts" -> {
        accountTable.delete(recordId);
        String ownerUsername = (String) record.get("ownerUsername");
        String accountName = (String) record.get("accountName");
        String accountKey = ownerUsername + ":" + accountName;
        accountKeyToId.remove(accountKey);
        removeFromOwnerUsernameIndex(ownerUsername, recordId);
      }
      case "transactions" -> transactionTable.delete(recordId);
    }
  }

  @Override
  public List<Map<String, Object>> selectAll(
      String bankName,
      int routingNumber,
      String tableName
  ) throws IllegalArgumentException, NullPointerException {
    validateBankRegistration(bankName, routingNumber);
    return select(bankName, routingNumber, tableName, Map.of());
  }

  @Override
  public boolean exists(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    validateBankRegistration(bankName, routingNumber);
    return !select(bankName, routingNumber, tableName, criteria).isEmpty();
  }

  @Override
  public void beginTransaction() throws IllegalStateException {
    if (inTransaction) {
      throw new IllegalStateException("Transaction already active");
    }
    inTransaction = true;
    transactionLog.clear();
  }

  @Override
  public void commitTransaction() throws IllegalStateException {
    if (!inTransaction) {
      throw new IllegalStateException("No active transaction");
    }
    inTransaction = false;
    transactionLog.clear();
  }

  @Override
  public void rollbackTransaction() throws IllegalStateException {
    if (!inTransaction) {
      throw new IllegalStateException("No active transaction");
    }

    for (int i = transactionLog.size() - 1; i >= 0; i--) {
      transactionLog.get(i).rollback();
    }

    inTransaction = false;
    transactionLog.clear();
  }

  /**
   * Checks if the given record is a valid record with respect to the provided table.
   * @param schema the table schema to check validity through.
   * @param record the record whose validity is to be checked.
   */
  private void checkRecordValidity(TableSchema schema, Map<String, Object> record) {
    if (!schema.isValidRecord(record)) {
      throw new IllegalArgumentException(
          "Invalid record for table " + schema.getTableName() + ": " + record
      );
    }
  }

  /**
   * Evaluates whether a record matches the specified search criteria.
   * Performs field-by-field comparison between record values and criteria requirements,
   * ensuring exact matches for all specified criteria fields.
   *
   * @param record the database record to evaluate against the criteria
   * @param criteria the search criteria containing field-value pairs that must match
   * @return true if the record matches all specified criteria, false otherwise
   */
  private boolean matchesCriteria(Map<String, Object> record, Map<String, Object> criteria) {
    for (Map.Entry<String, Object> criterion : criteria.entrySet()) {
      Object recordValue = record.get(criterion.getKey());
      Object criterionValue = criterion.getValue();

      if (recordValue == null || !recordValue.equals(criterionValue)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines if an email address already exists in the user database.
   * Performs a scan through all user records to check for email uniqueness,
   * supporting email constraint validation during user registration operations.
   *
   * @param email the email address to check for existing usage
   * @return true if the email is already registered to a user, false otherwise
   */
  private boolean emailExists(String email) {
    return getAllUsers().stream()
        .anyMatch(user -> email.equals(user.get("email")));
  }

  /**
   * Updates the username index when a new user is added to the system.
   * Maintains secondary index mapping usernames to user IDs for efficient user lookups.
   * Creates new index entries or updates existing ones to support O(log n) username searches.
   *
   * @param username the username to add to the index
   * @param userId the unique user ID associated with the username
   */
  private void updateUsernameIndex(String username, Integer userId) {
    List<Integer> userIds = usernameIndex.search(username);
    if (userIds == null) {
      userIds = new ArrayList<>();
    }
    userIds.add(userId);
    usernameIndex.insert(username, userIds);
  }

  /**
   * Updates the email index when a new user is added to the system.
   * Maintains secondary index mapping email addresses to user IDs for efficient user lookups.
   * Creates new index entries or updates existing ones to support email-based authentication.
   *
   * @param email the email address to add to the index
   * @param userId the unique user ID associated with the email address
   */
  private void updateEmailIndex(String email, Integer userId) {
    List<Integer> userIds = emailIndex.search(email);
    if (userIds == null) {
      userIds = new ArrayList<>();
    }
    userIds.add(userId);
    emailIndex.insert(email, userIds);
  }

  /**
   * Updates the owner username index when a new account is added to the system.
   * Maintains secondary index mapping owner usernames to account IDs for efficient account lookups.
   * Supports banking operations requiring retrieval of all accounts owned by a specific user.
   *
   * @param ownerUsername the username of the account owner to add to the index
   * @param accountId the unique account ID associated with the owner
   */
  private void updateOwnerUsernameIndex(String ownerUsername, Integer accountId) {
    List<Integer> accountIds = ownerUsernameIndex.search(ownerUsername);
    if (accountIds == null) {
      accountIds = new ArrayList<>();
    }
    accountIds.add(accountId);
    ownerUsernameIndex.insert(ownerUsername, accountIds);
  }

  /**
   * Updates user indexes when a user record is modified to maintain consistency.
   * Handles username changes by updating the username index and internal username-to-ID mapping.
   * Ensures all secondary indexes remain synchronized with the primary data storage.
   *
   * @param oldRecord the user record before modification containing original values
   * @param newRecord the user record after modification containing updated values
   */
  private void updateUserIndexes(Map<String, Object> oldRecord, Map<String, Object> newRecord) {
    String oldUsername = (String) oldRecord.get("username");
    String newUsername = (String) newRecord.get("username");
    Integer userId = (Integer) oldRecord.get("user_id");

    if (!oldUsername.equals(newUsername)) {
      removeFromUsernameIndex(oldUsername, userId);
      updateUsernameIndex(newUsername, userId);
      userNameToId.remove(oldUsername);
      userNameToId.put(newUsername, userId);
    }
  }

  /**
   * Updates account indexes when an account record is modified to maintain consistency.
   * Handles owner username changes by updating the owner username index and internal account key mapping.
   * Rebuilds composite keys when ownership information changes to preserve lookup efficiency.
   *
   * @param oldRecord the account record before modification containing original values
   * @param newRecord the account record after modification containing updated values
   */
  private void updateAccountIndexes(Map<String, Object> oldRecord, Map<String, Object> newRecord) {
    String oldOwnerUsername = (String) oldRecord.get("ownerUsername");
    String newOwnerUsername = (String) newRecord.get("ownerUsername");
    Integer accountId = (Integer) oldRecord.get("account_id");

    if (!oldOwnerUsername.equals(newOwnerUsername)) {
      removeFromOwnerUsernameIndex(oldOwnerUsername, accountId);
      updateOwnerUsernameIndex(newOwnerUsername, accountId);

      String oldAccountName = (String) oldRecord.get("accountName");
      String oldAccountKey = oldOwnerUsername + ":" + oldAccountName;
      String newAccountKey = newOwnerUsername + ":" + oldAccountName;
      accountKeyToId.remove(oldAccountKey);
      accountKeyToId.put(newAccountKey, accountId);
    }
  }

  /**
   * Removes a user ID from the username index during user deletion or username changes.
   * Maintains index consistency by removing obsolete entries and cleaning up empty index lists.
   * Completely removes index entries when no more users are associated with a username.
   *
   * @param username the username to remove from the index
   * @param userId the user ID to remove from the username's associated list
   */
  private void removeFromUsernameIndex(String username, Integer userId) {
    List<Integer> userIds = usernameIndex.search(username);
    if (userIds != null) {
      userIds.remove(userId);
      if (userIds.isEmpty()) {
        usernameIndex.remove(username);
      } else {
        usernameIndex.insert(username, userIds);
      }
    }
  }

  /**
   * Removes a user ID from the email index during user deletion or email changes.
   * Maintains index consistency by removing obsolete entries and cleaning up empty index lists.
   * Completely removes index entries when no more users are associated with an email address.
   *
   * @param email the email address to remove from the index
   * @param userId the user ID to remove from the email's associated list
   */
  private void removeFromEmailIndex(String email, Integer userId) {
    List<Integer> userIds = emailIndex.search(email);
    if (userIds != null) {
      userIds.remove(userId);
      if (userIds.isEmpty()) {
        emailIndex.remove(email);
      } else {
        emailIndex.insert(email, userIds);
      }
    }
  }

  /**
   * Removes an account ID from the owner username index during account deletion or ownership
   * changes. Maintains index consistency by removing obsolete entries and cleaning up empty
   * index lists. Completely removes index entries when no more accounts are associated with an
   * owner username.
   *
   * @param ownerUsername the owner username to remove from the index
   * @param accountId the account ID to remove from the owner's associated list
   */
  private void removeFromOwnerUsernameIndex(String ownerUsername, Integer accountId) {
    List<Integer> accountIds = ownerUsernameIndex.search(ownerUsername);
    if (accountIds != null) {
      accountIds.remove(accountId);
      if (accountIds.isEmpty()) {
        ownerUsernameIndex.remove(ownerUsername);
      } else {
        ownerUsernameIndex.insert(ownerUsername, accountIds);
      }
    }
  }
}