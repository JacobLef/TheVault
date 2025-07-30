package model.data_engine;

import java.util.Map;

/**
 * Defines the schema of a database table, specifying structure, data types, and constraints of
 * its fields. It serves as a contract for components that interact with the table, enabling
 * consistent data access, validation, and schema evolution while abstracting away low-level
 * storage or SQL-like logic.
 */
public interface TableSchema {
  boolean isValidRecord(Map<String, Object> record);
  ColumnDefinition getColumn(String columnName);
}
