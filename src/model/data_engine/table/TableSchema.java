package model.data_engine.table;

import java.util.List;
import java.util.Map;

/**
 * Defines the schema of a database table, specifying structure, data types, and constraints of
 * its fields. It serves as a contract for components that interact with the table, enabling
 * consistent data access, validation, and schema evolution while abstracting away low-level
 * storage or SQL-like logic.
 */
public interface TableSchema {
  /**
   * Is the given record a valid record for this table?
   * @param record The record whose validity is to be checked.
   * @return {@code true} if it is a valid record and {@code false} otherwise.
   * @throws NullPointerException if the given record is {@code null}.
   */
  boolean isValidRecord(Map<String, Object> record) throws NullPointerException;

  /**
   * Fetches all the Columns stored within this TableSchema.
   *
   * @return an unmodifiable List of columns stored in this TableSchema.
   */
  List<ColumnDefinition> getColumns();

  /**
   * Fetches the column by the given name, if it exists in this TableSchema.
   * @param columnName The name of the column to be fetched.
   * @return The appropriate {@code ColumnDefinition}.
   * @throws IllegalArgumentException if the given column does not exist in this TableSchema.
   * @throws NullPointerException if the given {@code columnName} is {@code null}.
   */
  ColumnDefinition getColumn(
      String columnName
  ) throws IllegalArgumentException, NullPointerException;

  /**
   * Fetches the primary key column of this TableSchema. It is guaranteed that each TableSchema
   * has a primary key column, so this method can never fail, unless the Table is empty.
   * @return The {@code ColumnDefinition} that is the primary key column for this TableSchema.
   * @throws IllegalStateException if there currently is no primary key col set.
   */
  ColumnDefinition getPrimaryKeyColumn() throws IllegalStateException;

  /**
   * Fetches the name of the primary key column in this TableSchema.
   * @return the name, as an {@code String} if there is a primary key column set in this TableSchema
   *         and {@code null} otherwise.
   */
  String getPrimaryKeyColumnName();

  /**
   * Fetches the name of this TableSchema.
   * @return The {@code String} name of this TableSchema.
   */
  String getTableName();
}
