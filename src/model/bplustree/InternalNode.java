package model.bplustree;

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
  private Node<K, V>[] children;

  /**
   * Creates a new empty InternalNode with an array of length {@code ORDER} children.
   */
  @SuppressWarnings("unchecked")
  public InternalNode() {
    super();
    this.children = new Node[ORDER];
  }

  /**
   * Creates a new InternalNode with the given first child and sets the parent relationship
   * bidirectionally.
   * @param firstChild the initial child node of this InternalNode.
   */
  @SuppressWarnings("unchecked")
  public InternalNode(Node<K, V> firstChild) {
    super();
    this.children = new Node[ORDER];
    this.children[0] = firstChild;
    if (firstChild != null) {
      firstChild.parent = this;
    }
  }

  boolean isLeaf() { return false; }

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
   * Fetches the children of this InternalNode.
   * @return an Array of the children of this InternalNode.
   */
  Node<K, V>[] getChildren() { return children; }

  /**
   * Sets the child of this InternalNode at the given Index.
   * @param index the index in the children array to set the given child at.
   * @param child the child who is now to be stored within this InternalNode.
   */
  void setChild(int index, Node<K, V> child) {
    children[index] = child;
    if (child != null) {
      child.parent = this;
    }
  }



}
