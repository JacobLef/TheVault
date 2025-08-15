package model.bplustree;

import java.util.Iterator;
import java.util.Map;

/**
 * Provides functionality for efficient account lookups, range queries for balance reporting,
 * and ordered iteration through account records. All primary operations
 * maintain O(log n) time complexity for optimal performance.
 *
 * @param <K> The type of the Keys stored by this BPlusTree.
 * @param <V> The type of the Values stored by this BPlusTree.
 *
 * <p>
 *  The following are features that any BPlusTree must adhere to:
 *    - Keys are maintained in ascending order throughout the tree
 *    - All leaf nodes are linked properly and completely
 *    - All actual data/values are stored only in the leaf nodes, such that internal (non-leaf)
 *      nodes contain ONLY keys
 *    - Each node can contain N children and N-1 keys (degree of node), where N is determined by
 *      the implementor.
 *    - All leaf nodes must be on the same level
 *    - The root can have >= 2 children pointers and at least one key
 *    - All non-root nodes must be at least half-way filled before a new node can be made
 * </p>
 */
public interface BPlusTree<K extends Comparable<K>, V> {
  /**
   * Inserts the given key-value pair into this BPlusTree, maintaining sorted order.
   * @param key   the key to be inserted, whose type must be comparable.
   * @param value the value to be inserted.
   * @return true if a new key was added and false if an existing key was updated.
   */
  boolean insert(K key, V value);

  /**
   * Deletes the given key-value pair from this BPlusTree, maintaining sorted order and all other
   * BPlusTree properties.
   * @param key the key to be deleted.
   * @return the value that was deleted from this BPlusTree.
   * @throws IllegalArgumentException if the given key cannot be found in this BPlusTree.
   */
  V delete(K key) throws IllegalArgumentException;

  /**
   * Clears this BPlusTree of all entries.
   */
  void clear();

  /**
   * Fetches the value associated with the given Key in this BPlusTree.
   * @param key the key whose value is to be fetched.
   * @return the value associated with the given key, whose type is of type {@code V}.
   */
  V get(K key);

  /**
   * Determines the minimum key stored in this BPlusTree.
   * @return the minimum key value or {@code null} for an empty tree.
   */
  K getMinKey();

  /**
   * Determines the maximum key stored in this BPlusTree.
   * @return the maximum key value or {@code null} for an empty tree.
   */
  K getMaxKey();

  /**
   * Determines the height of this BPlusTree.
   * @return the integer value of the height of this BPlusTree.
   */
  int height();

  /**
   * Determines the number of (key, value) pairs in this BPlusTree.
   * @return the integer value of the number of pairs in this BPlusTree.
   */
  int size();

  /**
   * Fetches a list of all the values stored within this BPlusTree that are between the two
   * given key's, where the range is assumed to be inclusive.
   * @param startKey  the key to start at.
   * @param endKey    the key to end at.
   * @return a Map of all values associated with the keys in the range [startKey, endKey]. The
   *         value-list is in ascending order with relation to the keys.
   * @implNote if the {@code startKey} or {@code endKey} are not present in this BPlusTree, then
   * the start or end, respectively, will be the first or last key in this tree.
   * @throws IllegalArgumentException if the endKey > startKey.
   */
  Map<K, V> rangeQuery(K startKey, K endKey) throws IllegalArgumentException;

  /**
   * Creates an iterator of all the values stored within this BPlusTree.
   * @return an empty iterator if no values are present and an iterator containing all values
   *         otherwise, in ascending order.
   */
  Iterator<V> iterator();

  /**
   * Creates an iterator of all keys stored within this BPlusTree.
   * @return an empty iterator if no keys are present and an iterator containing all keys
   *         otherwise, in ascending order.
   */
  Iterator<K> keyIterator();

  /**
   * Creates an iterator of all (key, value) pairs in this BPlusTree.
   * @return an empty iterator if no (key, value) pairs are present and an iterator containing
   *         all entries otherwise, in ascending order of the keys.
   */
  Iterator<Map.Entry<K, V>> entryIterator();

  /**
   * Creates an iterator of all values stored in this BPlusTree within the range [startKey, endKey].
   * @param startKey  the key to start at (inclusive).
   * @param endKey    the key to end at (inclusive).
   * @return an empty iterator if no (key, value) pairs currently exist within the given range
   *         and a complete iterator otherwise.
   * @implNote if the {@code startKey} or {@code endKey} are not present in this BPlusTree, then
   *           the start or end, respectively, will be the first or last key in this tree.
   * @throws IllegalArgumentException if the endKey > startKey.
   */
  Iterator<V> rangeIterator(K startKey, K endKey) throws IllegalArgumentException;

  /**
   * Determines if this BPlusTree contains the following key.
   * @param key the key to be checked.
   * @return true if the key exists and false otherwise.
   */
  boolean contains(K key);
}