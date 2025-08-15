package model.data_engine;

import jdk.dynalink.Operation;
import model.Bank;
import model.bplustree.BPlusTree;
import model.bplustree.LeafNodeImpl;
import model.data_engine.index.Index;
import model.data_engine.index.IndexImpl;
import model.data_engine.table.TableSchema;
import model.data_engine.table.TableSchemaImpl;
import model.types.DataType;
import model.user.BankAccount;
import model.user.Transaction;
import model.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton database implementation for the banking application that manages
 * multiple banks and their associated data tables. Provides CRUD operations,
 * transaction management, and data persistence using custom B+ tree indexing
 * for efficient account and transaction storage.
 */
public class DataBase implements DataEngine {
  private static DataBase instance;
  private Map<String, TableSchema> tables; // tableName -> TableSchema;
  private BPlusTree<Integer, User> userTable;
  private BPlusTree<Integer, BankAccount> accountTable;
  private BPlusTree<Integer, Transaction> transactionTable;
  private Map<String, Integer> userNameToId;
  private Map<String, Integer> accountNameToId;
  private Map<String, Bank> banks;  // bankName -> Bank
  private Map<Integer, Bank> bankRoutingMap;  // routingNumber -> Bank
  private boolean inTransaction;
  private List<Operation> transactionLog;
  private AtomicInteger nextUserId = new AtomicInteger(1);
  private AtomicInteger nextAccountId = new AtomicInteger(1);
  private AtomicInteger nextTransactionId = new AtomicInteger(1);
  private Index<Integer, List<Integer>> userAccountIndex;
  private Index<Double, List<Integer>> balanceIndex;

  /**
   * Creates a new DataBase such that this DataBase contains the following tables and indexes:
   * - users table with primary key on user_id and secondary indexes on username/email.
   * - accounts table with primary key on account_id and secondary indexes on owner relations.
   * - transactions table with primary key on transaction_id and indexes for querying patterns.
   * All tables are initialized to be empty.
   */
  private DataBase() {
    this.userTable = new LeafNodeImpl<>();
    this.accountTable = new LeafNodeImpl<>();
    this.transactionTable = new LeafNodeImpl<>();

    this.userNameToId = new HashMap<>();
    this.accountNameToId = new HashMap<>();

    this.banks = new HashMap<>();
    this.bankRoutingMap = new HashMap<>();

    this.inTransaction = false;
    this.transactionLog = new ArrayList<>();

    this.initTables();

    this.userAccountIndex = new IndexImpl<>(
        "user_to_accounts",
        new LeafNodeImpl<Integer, List<Integer>>(),
        false
    );
    this.balanceIndex = new IndexImpl<>(
        "balance_index",
        new LeafNodeImpl<Double, List<Integer>>(),
        false
    );
  }

  /**
   * Initializes all the tables used in this DataBase (user, accounts, transactions). No
   * information is put into these tables by default.
   */
  private void initTables() {
    this.tables = new HashMap<>();
    this.tables.put("users", new TableSchemaImpl.Builder()
        .tableName("users")
        .addColumn("user_id", DataType.INTEGER, false, true, true)
        .addColumn("username", DataType.STRING, false, true, false)
        .addColumn("password", DataType.STRING, false, false, false)
        .addColumn("email", DataType.STRING, false, true, false)
        .addColumn("createdAt", DataType.DATETIME, false, false, false)
        .build()
    );
    this.tables.put("accounts", new TableSchemaImpl.Builder()
        .tableName("accounts")
        .addColumn("account_id", DataType.INTEGER, false, true, true)
        .addColumn("ownerUsername", DataType.STRING, false, false, false)
        .addColumn("accountName", DataType.STRING, false, false, false)
        .addColumn("balance", DataType.DOUBLE, false, false, false)
        .addColumn("type", DataType.STRING, false, false, false)
        .addColumn("status", DataType.STRING, false, false, false)
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
        .addColumn("transactionType", DataType.STRING, false, false, false)
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
  public void insert(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> record
  ) throws IllegalArgumentException, NullPointerException {
    if (bankName == null || tableName == null || record == null) {
      throw new NullPointerException(
          "When trying to insert, the bank name, table name, or record is null and cannot be!"
      );
    }
    if (bankName.isEmpty() || tableName.isEmpty()) {
      throw new IllegalArgumentException(
          "When trying to insert, bank name or table name are empty and cannot be!"
      );
    }
    if (record.isEmpty()) {
      throw new IllegalArgumentException(
          "Invalid record provided when trying to insert: " + record
      );
    }
    if (this.tables.containsKey(bankName)) {
      throw new IllegalArgumentException(
          "Bank already exists under name: " + bankName
      );
    }
    if (this.bankRoutingMap.containsKey(routingNumber)) {
      throw new IllegalArgumentException(
          "Bank already exists under routing number: " + routingNumber
      );
    }
  }

  @Override
  public List<Map<String, Object>> select(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    return List.of();
  }

  @Override
  public Map<String, Object> selectOne(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    return Map.of();
  }

  @Override
  public void update(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria,
      Map<String, Object> newValues
  ) throws IllegalArgumentException, NullPointerException {

  }

  @Override
  public void delete(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {

  }

  @Override
  public List<Map<String, Object>> selectAll(
      String bankName,
      int routingNumber,
      String tableName
  ) throws IllegalArgumentException, NullPointerException {
    return List.of();
  }

  @Override
  public boolean exists(
      String bankName,
      int routingNumber,
      String tableName,
      Map<String, Object> criteria
  ) throws IllegalArgumentException, NullPointerException {
    return false;
  }

  @Override
  public void beginTransaction() throws IllegalStateException {

  }

  @Override
  public void commitTransaction() throws IllegalStateException {

  }

  @Override
  public void rollbackTransaction() throws IllegalStateException {

  }
}