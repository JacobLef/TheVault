package model.data_engine;

import model.types.DataType;

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
) implements ColumnDefinition { }