package model.data_engine.index;

import model.data_engine.BPlusTree;

/**
 * Wrapper class that encapsulates a B+ Tree index with metadata and management functionality.
 * Provides a named index with type information, performance statistics, and operational
 * controls for efficient data retrieval in the banking database system.
 */
public interface Index<K extends Comparable<K>, V> {
  /**
   * Gets the name of this index.
   * @return the string name of this index.
   */
  String name();

  /**
   * Gets the underlying B+ Tree for this index.
   * @return the B+ Tree data structure.
   */
  BPlusTree<K, V> tree();

  /**
   * Determines if this is a primary index.
   * @return true if this is a primary index, false if secondary.
   */
  boolean isPrimary();

  /**
   * Inserts a key-value pair into this index.
   * @param key the key to insert.
   * @param value the value to insert.
   * @return true if insertion was successful.
   */
  boolean insert(K key, V value);

  /**
   * Searches for a value by key in this index.
   * @param key the key to search for.
   * @return the value if found, null otherwise.
   */
  V search(K key);

  /**
   * Removes a key-value pair from this index.
   * @param key the key to remove.
   * @return the removed value, or null if not found.
   */
  V remove(K key);

  /**
   * Gets the number of entries in this index.
   * @return the size of this index.
   */
  int size();
}
