package model.data_engine.table;

import model.types.DataType;

import java.util.Objects;

/**
 * Specific implementation of a ColumnDefinition such that it simply acts a C-like structure, with
 * a set of getters and no other behavior or properties.
 */
public record ColumnDefinitionImpl(
    String name,
    DataType type,
    boolean nullable,
    boolean unique,
    boolean primaryKey
) implements ColumnDefinition {
  public ColumnDefinitionImpl {
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    name = name.trim();
  }
}
