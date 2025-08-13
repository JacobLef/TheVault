package model.bplustree;

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
  private V[] values;
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
    return false;
  }

  boolean isUnderflow() {
    return false;
  }

  boolean insertKey(K key, V value) {
    return false;
  }

  V removeKey(K key) {
    return null;
  }

  K findNextKey(K key) {
    return null;
  }

  Node<K, V> split() {
    return null;
  }

  boolean canMergeWith(Node<K, V> other) {
    return false;
  }

  void mergeWith(Node<K, V> other) {

  }

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
