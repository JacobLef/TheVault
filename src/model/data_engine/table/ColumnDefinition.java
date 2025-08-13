package model.data_engine.table;

import model.types.DataType;

/**
 * Defines the properties of any arbitrary properties and the behaviors it must offer.
 */
public interface ColumnDefinition {
  /**
   * Gets the name of this ColumnDefinition.
   * @return The String name of this ColumnDefinition.
   */
  String name();

  /**
   * Gets the type of data which this ColumnDefinition stores.
   * @return The DataType representation of the data which this ColumnDefinition stores.
   */
  DataType type();

  /**
   * Is this ColumnDefinition nullable?
   * @return true -> is nullable and false otherwise.
   */
  boolean nullable();

  /**
   * Is this ColumnDefinition unique.
   * @return true -> is unique and false otherwise.
   */
  boolean unique();

  /**
   * Is this ColumnDefinition a primary key?
   * @return true -> it is a primary key-holder and false otherwise.
   */
  boolean primaryKey();
}
