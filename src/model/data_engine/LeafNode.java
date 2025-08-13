package model.data_engine;

import java.util.Iterator;
import java.util.Map;

/**
 * A leaf node in a B+ tree that stores key-value pairs and provides the actual data storage.
 * Leaf nodes are linked together to enable efficient sequential access and range queries.
 * This is the only type of node that contains actual values in the B+ tree structure.
 *
 * @param <K> The type of the Keys stored in this LeafNode (must be comparable).
 * @param <V> The type of the Values stored in this LeafNode.
 */
public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
  @Override
  public boolean insert(K key, V value) {
    return false;
  }

  @Override
  public V delete(K key) throws IllegalArgumentException {
    return null;
  }

  @Override
  public void clear() {

  }

  @Override
  public V get(K key) {
    return null;
  }

  @Override
  public K getMinKey() {
    return null;
  }

  @Override
  public K getMaxKey() {
    return null;
  }

  @Override
  public int height() {
    return 0;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Map<K, V> rangeQuery(K startKey, K endKey) throws IllegalArgumentException {
    return Map.of();
  }

  @Override
  public Iterator<V> iterator() {
    return null;
  }

  @Override
  public Iterator<K> keyIterator() {
    return null;
  }

  @Override
  public Iterator<Map.Entry<K, V>> entryIterator() {
    return null;
  }

  @Override
  public Iterator<V> rangeIterator(K startKey, K endKey) throws IllegalArgumentException {
    return null;
  }

  @Override
  public boolean contains(K key) {
    return false;
  }
}
