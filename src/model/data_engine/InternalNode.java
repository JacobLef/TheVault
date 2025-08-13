package model.data_engine;

import java.util.Iterator;
import java.util.Map;

/**
 * An internal node in a B+ tree that contains only keys and child pointers for navigation.
 * Internal nodes guide searches toward the appropriate leaf nodes but store no actual values.
 * Each internal node maintains the B+ tree's balanced structure by directing traversal
 * operations to the correct subtrees.
 *
 * @param <K> The type of the Keys stored in this InternalNode (must be comparable).
 * @param <V> InternalNode's do not store values, so this is ignored for all specification purposes.
 */
public class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
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
