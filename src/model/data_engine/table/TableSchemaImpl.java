package model.data_engine.table;

import model.types.AccountStatus;
import model.types.AccountType;
import model.types.DataType;
import model.types.TransactionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Specific implementation of a TableSchema such that it abstracts away all low-level
 * functionality of querying a database, and simply offers validity and getter operations on
 * the properties of a specific table.
 *
 * @implNote Any TableSchemaImpl object is immutable and can only be read from.
 */
public class TableSchemaImpl implements TableSchema {
  public static class Builder {
    private String tableName;
    private final List<ColumnDefinition> cols = new ArrayList<>();

    public Builder tableName(String tableName) {
      this.tableName = tableName;
      return this;
    }

    /**
     * Adds a column to this Builder whose properties reflect those given.
     * @param name The name of the column.
     * @param type The type of the variable stored within the column.
     * @param isNullable  Is the added column nullable?
     * @param isUnique Does the added column have the unique property?
     * @param isPrimaryKey Is the added column a primary key?
     * @return this Builder.
     * @throws IllegalArgumentException if the given name is already present in the columns listed
     *         within this Builder.
     */
    public Builder addColumn(
        String name,
        DataType type,
        boolean isNullable,
        boolean isUnique,
        boolean isPrimaryKey
    ) throws IllegalArgumentException {

      boolean duplicateName = this.cols.stream()
          .anyMatch(col -> col.name().equals(name));
      if (duplicateName) {
        throw new IllegalArgumentException("Duplicate column name: " + name);
      }

      this.cols.add(new ColumnDefinitionImpl(name, type, isNullable, isUnique, isPrimaryKey));
      return this;
    }

    /**
     * Constructs a new TableSchemaImpl object whose fields are copies of the fields of this
     * Builder.
     * @return the respective TableSchemaImpl object.
     * @throws NullPointerException if the {@code tableName} of this Builder is {@code null}.
     */
    public TableSchemaImpl build() throws NullPointerException {
      if (this.tableName == null) {
        throw new NullPointerException("Cannot have a null table name");
      }
      return new TableSchemaImpl(tableName, cols);
    }
  }

  private final String tableName;
  private final List<ColumnDefinition> cols;
  private final Map<String, ColumnDefinition> columnMap;
  private final String primaryKeyColumnName;

  private TableSchemaImpl(
      String name,
      List<ColumnDefinition> cols
  ) throws IllegalArgumentException {
    this.tableName = name;
    this.cols = List.copyOf(cols);
    this.columnMap = cols.stream()
        .collect(
            Collectors.toMap(ColumnDefinition::name, Function.identity())
        );

    String[] primaryKeyCols = this.cols
        .stream()
        .filter(ColumnDefinition::primaryKey)
        .map(ColumnDefinition::name)
        .toArray(String[]::new);
    if (primaryKeyCols.length == 0) {
      throw new IllegalArgumentException("No primary key columns found!");
    }
    if (primaryKeyCols.length > 1) {
      throw new IllegalArgumentException("Multiple primary key columns found!");
    }
    this.primaryKeyColumnName = primaryKeyCols[0];
  }

  @Override
  public boolean isValidRecord(Map<String, Object> record) throws NullPointerException {
    if (record == null) {
      throw new NullPointerException("Cannot have a null record!");
    }

    for (ColumnDefinition col : cols) {
      Object value = record.get(col.name());

      if (!col.nullable() && value == null) {
        return false;
      }

      if (value != null && !isCorrectType(value, col.type())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public List<ColumnDefinition> getColumns() {
    return cols;
  }

  @Override
  public ColumnDefinition getColumn(
      String columnName
  ) throws IllegalArgumentException, NullPointerException {
    if (columnName == null) {
      throw new NullPointerException("Cannot have a null column name!");
    }
    ColumnDefinition col = columnMap.getOrDefault(columnName, null);
    if (col == null) {
      throw new IllegalArgumentException("Column '" + columnName + "' not found!");
    }
    return col;
  }

  @Override
  public ColumnDefinition getPrimaryKeyColumn() throws IllegalStateException {
    if (primaryKeyColumnName == null || primaryKeyColumnName.isEmpty()) {
      throw new IllegalStateException("Primary key column name is empty!");
    }
    return columnMap.get(primaryKeyColumnName);
  }

  @Override
  public String getPrimaryKeyColumnName() {
    return this.primaryKeyColumnName;
  }

  @Override
  public String getTableName() {
    return this.tableName;
  }

  /**
   * Is the given value the correct data type?
   * @param value       The value whose type is to be compared to the expected.
   * @param expected    The expected type of the provided value.
   * @return true if they are a match and false otherwise.
   */
  private boolean isCorrectType(Object value, DataType expected) {
    return switch (expected) {
      case INTEGER -> value instanceof Integer || value instanceof Long;
      case DOUBLE -> value instanceof Double || value instanceof Float;
      case STRING -> value instanceof String;
      case DATETIME -> value instanceof Date || value instanceof LocalDateTime;
      case BOOLEAN -> value instanceof Boolean;
      case ACCOUNT_TYPE -> value instanceof AccountType;
      case ACCOUNT_STATUS -> value instanceof AccountStatus;
      case TRANSACTION_TYPE -> value instanceof TransactionType;
    };
  }

}
