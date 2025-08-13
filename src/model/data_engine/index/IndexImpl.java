package model.data_engine.index;

import model.bplustree.BPlusTree;

/**
 * Implementation of Index interface that wraps a B+ Tree with metadata and management
 * functionality for database indexing operations.
 */
public record IndexImpl<K extends Comparable<K>, V>(
    String name,
    BPlusTree<K, V> tree,
    boolean isPrimary
) implements Index<K, V> {
  @Override
  public boolean insert(K key, V value) {
    return tree.insert(key, value);
  }

  @Override
  public V search(K key) {
    return tree.get(key);
  }

  @Override
  public V remove(K key) {
    try {
      return tree.delete(key);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public int size() {
    return tree.size();
  }
}
