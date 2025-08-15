package model.bplustree;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A leaf node in a B+ tree that stores key-value pairs and provides the actual data storage.
 * Leaf nodes are linked together to enable efficient sequential access and range queries.
 * This is the only type of node that contains actual values in the B+ tree structure.
 *
 * @param <K> The type of the Keys stored in this LeafNode (must be comparable).
 * @param <V> The type of the Values stored in this LeafNode.
 */
public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
  private final V[] values;
  private LeafNode<K, V> next;
  private LeafNode<K, V> prev;

  /**
   * Creates a new empty LeafNode with {@code null} neighbors.
   */
  @SuppressWarnings("unchecked")
  public LeafNode() {
    super();
    this.values = (V[]) new Object[MAX_KEYS];
    this.next = null;
    this.prev = null;
  }

  /**
   * Creates a new LeafNode with the specified next node but an {@code null} previous.
   * @param next the next leaf node after this LeafNode in the linked list.
   */
  @SuppressWarnings("unchecked")
  public LeafNode(LeafNode<K, V> next) {
    super();
    this.values = (V[]) new Object[MAX_KEYS];
    this.next = next;
    this.prev = null;
    if (next != null) {
      next.setPrev(this);
    }
  }

  boolean isLeaf() { return true; }

  boolean isFull() {
    return this.keyCount >= MAX_KEYS;
  }

  boolean isUnderflow() {
    return this.keyCount < MIN_KEYS;
  }

  Node<K, V> split() {
    LeafNode<K, V> newLeaf = new LeafNode<K, V>();
    int splitIndex = keyCount / 2;
    int moveCount = keyCount - splitIndex;

    for (int i = 0; i < moveCount; i++) {
      newLeaf.keys[i] = this.keys[splitIndex + i];
      newLeaf.values[i] = this.values[splitIndex + i];
      this.keys[splitIndex + i] = null;
      this.values[splitIndex + i] = null;
    }

    newLeaf.keyCount = moveCount;
    this.keyCount = splitIndex;

    newLeaf.next = this.next;
    newLeaf.prev = this;

    if (this.next != null) {
      this.next.prev = newLeaf;
    }

    this.next = newLeaf;

    newLeaf.parent = this.parent;

    return newLeaf;
  }

  boolean canMergeWith(Node<K, V> other) {
    if (other == null || !other.isLeaf()) {
      return false;
    }

    LeafNode<K, V> otherLeaf = (LeafNode<K, V>) other;
    return (this.keyCount + otherLeaf.keyCount) <= MAX_KEYS;
  }

  void mergeWith(Node<K, V> other) {
    if (!canMergeWith(other)) {
      throw new IllegalArgumentException("Cannot merge: combined size exceeds maximum");
    }

    LeafNode<K, V> otherLeaf = (LeafNode<K, V>) other;

    for (int i = 0; i < otherLeaf.keyCount; i++) {
      this.keys[this.keyCount + i] = otherLeaf.keys[i];
      this.values[this.keyCount + i] = otherLeaf.values[i];
    }
    this.keyCount += otherLeaf.keyCount;

    this.next = otherLeaf.next;
    if (otherLeaf.next != null) {
      otherLeaf.next.prev = this;
    }

    otherLeaf.clear();
  }

  @Override
  public boolean insert(K key, V value) {
    boolean keyExists = this.contains(key);
    if (keyExists) {
      for (int i = 0; i < keyCount; i++) {
        if (keys[i].equals(key)) {
          values[i] = value;
          break;
        }
      }
    } else {
      this.insertUnique(key, value);
    }
    return !keyExists;
  }

  /**
   * Inserts the given key into this LeafNode, with the assumption the given key does not appear
   * in this LeafNode.
   * @param key    the key to be inserted.
   * @param value  the value to be inserted.
   */
  private void insertUnique(K key, V value) {
    if (this.isFull()) {
      throw new IllegalStateException("Cannot insert into full leaf node - split required!");
    }

    int insertIndex = getInsertionIndex(key);

    for (int i = keyCount; i > insertIndex; i--) {
      keys[i] = keys[i - 1];
      values[i] = values[i - 1];
    }

    keys[insertIndex] = key;
    values[insertIndex] = value;
    keyCount++;
  }

  /**
   * Finds the index within this LeafNode in which the given value should be inserted, such that
   * sorted order is maintained (least to greatest).
   * @param key the key whose value is to be inserted into this LeafNode.
   * @return the appropriate zeroed index where the given value should be placed.
   */
  private int getInsertionIndex(K key) {
    int targetIndex = 0;
    for (int i = 0; i < this.keyCount; i++) {
      if (key.compareTo(this.keys[i]) > 0) {
        targetIndex = i + 1;
      } else {
        break;
      }
    }
    return targetIndex;
  }

  @Override
  public V delete(K key) throws IllegalArgumentException {
    int index = -1;
    for (int i = 0; i < this.keyCount; i++) {
      if (keys[i].equals(key)) {
        index = i;
        break;
      }
    }

    if (index == -1) {
      throw new IllegalArgumentException("Key not found: " + key);
    }

    V rv = this.values[index];
    for (int i = index; i < keyCount - 1; i++) {
      this.values[i] = this.values[i + 1];
      this.keys[i] = this.keys[i + 1];
    }

    keys[keyCount - 1] = null;
    values[keyCount - 1] = null;
    keyCount--;

    return rv;
  }

  @Override
  public void clear() {
    for (int i = 0; i < keyCount; i++) {
      values[i] = null;
      keys[i] = null;
    }
    keyCount = 0;
    this.next = null;
    this.prev = null;
    this.parent = null;
  }

  @Override
  public V get(K key) {
    for (int i = 0; i < keyCount; i++) {
      if (keys[i].equals(key)) {
        return values[i];
      }
    }

    return null;
  }

  @Override
  public K getMinKey() {
    if (this.keyCount == 0) {
      return null;
    }
    return keys[0];
  }

  @Override
  public K getMaxKey() {
    if (this.keyCount == 0) {
      return null;
    }
    return keys[keyCount - 1];
  }

  @Override
  public int height() {
    return 1;
  }

  @Override
  public int size() {
    return keyCount;
  }

  @Override
  public Map<K, V> rangeQuery(K startKey, K endKey) throws IllegalArgumentException {
    if (startKey.compareTo(endKey) > 0) {
      throw new IllegalArgumentException("Start key must be <= end key");
    }

    Map<K, V> result = new LinkedHashMap<>();
    LeafNode<K, V> current = this;

    while (current.prev != null && current.getMinKey() != null
        && startKey.compareTo(current.getMinKey()) < 0) {
      current = current.prev;
    }

    while (current != null) {
      for (int i = 0; i < current.keyCount; i++) {
        K currentKey = current.keys[i];
        if (currentKey.compareTo(startKey) >= 0 && currentKey.compareTo(endKey) <= 0) {
          result.put(currentKey, current.values[i]);
        } else if (currentKey.compareTo(endKey) > 0) {
          return result;
        }
      }
      current = current.next;
    }

    return result;
  }

  @Override
  public Iterator<V> iterator() {
    return new LeafValueIterator();
  }

  @Override
  public Iterator<K> keyIterator() {
    return new LeafKeyIterator();
  }

  @Override
  public Iterator<Map.Entry<K, V>> entryIterator() {
    return new LeafEntryIterator();
  }

  @Override
  public Iterator<V> rangeIterator(K startKey, K endKey) throws IllegalArgumentException {
    if (startKey.compareTo(endKey) > 0) {
      throw new IllegalArgumentException("Start key must be <= end key");
    }

    return new LeafRangeIterator(startKey, endKey);
  }

  @Override
  public boolean contains(K key) {
    return this.get(key) != null;
  }

  /**
   * Provides sequential access to all values stored across the entire B+ Tree in key-sorted order.
   * Starts from the leftmost leaf node and traverses the linked list of leaves, yielding only the
   * values without their corresponding keys.
   */
  private class LeafValueIterator implements Iterator<V> {
    private LeafNode<K, V> currentLeaf;
    private int currentIdx;

    public LeafValueIterator() {
      this.currentLeaf = LeafNode.this;
      while (currentLeaf.prev != null) {
        currentLeaf = currentLeaf.prev;
      }

      currentIdx = 0;
    }

    @Override
    public boolean hasNext() {
      return currentLeaf != null && currentIdx < currentLeaf.keyCount;
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      V val = currentLeaf.values[currentIdx];
      currentIdx++;

      if (currentIdx >= currentLeaf.keyCount) {
        currentLeaf = currentLeaf.next;
        currentIdx = 0;
      }

      return val;
    }
  }

  /**
   * Provides sequential access to all keys stored across the entire B+ Tree in ascending sorted
   * order. Traverse the linked leaf structure from left to right, yielding only the keys without
   * their values.
   */
  private class LeafKeyIterator implements Iterator<K> {
    private LeafNode<K, V> currentLeaf;
    private int currentIdx;

    public LeafKeyIterator() {
      this.currentLeaf = LeafNode.this;
      while (currentLeaf.prev != null) {
        currentLeaf = currentLeaf.prev;
      }
      currentIdx = 0;
    }

    @Override
    public boolean hasNext() {
      return currentLeaf != null && currentIdx < currentLeaf.keyCount;
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      K key = currentLeaf.keys[currentIdx];
      currentIdx++;

      if (currentIdx >= currentLeaf.keyCount) {
        currentLeaf = currentLeaf.next;
        currentIdx = 0;
      }
      return key;
    }
  }

  /**
   * Provides sequential access to all key-value pairs stored across the entire B+ Tree in
   * key-sorted order.
   */
  private class LeafEntryIterator implements Iterator<Map.Entry<K, V>> {
    private LeafNode<K, V> currentLeaf;
    private int currentIdx;

    public LeafEntryIterator() {
      this.currentLeaf = LeafNode.this;
      while (currentLeaf.prev != null) {
        currentLeaf = currentLeaf.prev;
      }
      currentIdx = 0;
    }

    @Override
    public boolean hasNext() {
      return currentLeaf != null && currentIdx < currentLeaf.keyCount;
    }

    @Override
    public Map.Entry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      K key = currentLeaf.keys[currentIdx];
      V val = currentLeaf.values[currentIdx];
      currentIdx++;

      if (currentIdx >= currentLeaf.keyCount) {
        currentLeaf = currentLeaf.next;
        currentIdx = 0;
      }

      return new AbstractMap.SimpleEntry<>(key, val);
    }
  }

  /**
   * Provides sequential access to values within a specified key range in sorted order. Initially
   * positions itself at the starting key and stops at the ending key.
   */
  private class LeafRangeIterator implements Iterator<V> {
    private LeafNode<K, V> currentLeaf;
    private int currentIdx;
    private final K endKey;
    private boolean exhausted;

    public LeafRangeIterator(K startKey, K endKey) {
      this.endKey = endKey;
      this.exhausted = false;

      currentLeaf = LeafNode.this;
      while (currentLeaf.prev != null && currentLeaf.getMinKey() != null
          && startKey.compareTo(currentLeaf.getMinKey()) < 0) {
        currentLeaf = currentLeaf.prev;
      }

      currentIdx = 0;
      while (currentIdx < currentLeaf.keyCount &&
          currentLeaf.keys[currentIdx].compareTo(startKey) < 0) {
        currentIdx++;
      }

      if (currentIdx >= currentLeaf.keyCount
          || currentLeaf.keys[currentIdx].compareTo(endKey) > 0) {
        exhausted = true;
      }
    }

    @Override
    public boolean hasNext() {
      return !exhausted && currentLeaf != null && currentIdx < currentLeaf.keyCount
          && currentLeaf.keys[currentIdx].compareTo(endKey) <= 0;
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      V val = currentLeaf.values[currentIdx];
      currentIdx++;

      if (currentIdx >= currentLeaf.keyCount) {
        currentLeaf = currentLeaf.next;
        currentIdx = 0;
      }

      if (currentLeaf == null || currentIdx >= currentLeaf.keyCount
          || currentLeaf.keys[currentIdx].compareTo(endKey) > 0) {
        exhausted = true;
      }

      return val;
    }
  }

  /**
   * Fetches the next LeafNode after this LeafNode.
   * @return the LeafNode that comes directly after this LeafNode in the virtual Linked List.
   */
  LeafNode<K, V> getNext() { return next; }

  /**
   * Sets the next node of this LeafNode to be the one given.
   * @param next the new next node of this LeafNode.
   */
  void setNext(LeafNode<K, V> next) { this.next = next; }

  /**
   * Fetches the previous LeafNode that comes before this LeafNode.
   * @return the LeafNode object that comes directly before this LeafNode in the virtual Linked
   *         List.
   */
  LeafNode<K, V> getPrev() { return prev; }

  /**
   * Sets the previous node of this LeafNode to be the one given.
   * @param prev the new previous node of this LeafNode.
   */
  void setPrev(LeafNode<K, V> prev) { this.prev = prev; }

  /**
   * Fetches all the values stored within this LeafNode.
   * @return an Array of the values stored within this LeafNode.
   */
  V[] getValues() { return values; }
}