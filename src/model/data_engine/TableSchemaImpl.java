package model.data_engine;

import model.types.DataType;

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
 * Any TableSchemaImpl object is immutable and can only be read from.
 */
public class TableSchemaImpl implements TableSchema {
  public static class Builder {
    private String tableName;
    private List<ColumnDefinition> cols = new ArrayList<>();

    public Builder tableName(String tableName) {
      this.tableName = tableName;
      return this;
    }

    public Builder addColumn(
        String name,
        DataType type,
        boolean isNullable,
        boolean isUnique,
        boolean isPrimaryKey
    ) {
      this.cols.add(new ColumnDefinitionImpl(name, type, isNullable, isUnique, isPrimaryKey));
      return this;
    }

    public TableSchemaImpl build() {
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

    String[] primaryKeyCols = (String[]) this.cols.stream()
        .filter(ColumnDefinition::primaryKey)
        .map(ColumnDefinition::name)
        .toArray();
    if (primaryKeyCols.length == 0) {
      throw new IllegalArgumentException("No primary key columns found!");
    }
    if (primaryKeyCols.length > 1) {
      throw new IllegalArgumentException("Multiple primary key columns found!");
    }
    this.primaryKeyColumnName = primaryKeyCols[0];
  }

  @Override
  public boolean isValidRecord(Map<String, Object> record) {
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
  public ColumnDefinition getColumn(String columnName) {
    return columnMap.getOrDefault(columnName, null);
  }

  private boolean isCorrectType(Object value, DataType expected) {
    return switch (expected) {
      case INTEGER -> value instanceof Integer || value instanceof Long;
      case DOUBLE -> value instanceof Double || value instanceof Float;
      case STRING -> value instanceof String;
      case DATETIME -> value instanceof Date || value instanceof LocalDateTime;
      case BOOLEAN -> value instanceof Boolean;
    };
  }

}
