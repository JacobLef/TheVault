package model.bplustree;

import java.util.Arrays;

/**
 * A leaf node in a B+ tree that stores key-value pairs and provides the actual data storage.
 * Leaf nodes are linked together to enable efficient sequential access and range queries.
 * This is the only type of node that contains actual values in the B+ tree structure.
 *
 * @param <K> The type of the Keys stored in this LeafNode (must be comparable).
 * @param <V> The type of the Values stored in this LeafNode.
 */
public class LeafNodeImpl<K extends Comparable<K>, V>
    extends NodeImpl<K, V>
    implements LeafNode<K, V>
{
  private final V[] values;
  private LeafNode<K, V> next;
  private LeafNode<K, V> prev;

  /**
   * Creates a new empty LeafNode with no linked neighbors.
   */
  @SuppressWarnings("unchecked")
  public LeafNodeImpl() {
    super();
    this.values = (V[]) new Object[MAX_KEYS];
    this.next = null;
    this.prev = null;
  }

  /**
   * Creates a new LeafNode with the specified next node in the linked list.
   * @param next the next leaf node after this LeafNode in the linked list.
   */
  @SuppressWarnings("unchecked")
  public LeafNodeImpl(LeafNode<K, V> next) {
    super();
    this.values = (V[]) new Object[MAX_KEYS];
    this.next = next;
    this.prev = null;
    if (next != null) {
      next.setPrev(this);
    }
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public Node.SplitResult<K, V> split() {
    LeafNodeImpl<K, V> newLeaf = new LeafNodeImpl<>();
    int splitIndex = (keyCount + 1) / 2;
    int moveCount = keyCount - splitIndex;

    for (int i = 0; i < moveCount; i++) {
      newLeaf.keys[i] = this.keys[splitIndex + i];
      newLeaf.values[i] = this.values[splitIndex + i];
      this.keys[splitIndex + i] = null;
      this.values[splitIndex + i] = null;
    }

    newLeaf.keyCount = moveCount;
    this.keyCount = splitIndex;

    newLeaf.setNext(this.next);
    if (this.next != null) {
      this.next.setPrev(newLeaf);
    }
    this.next = newLeaf;
    newLeaf.setPrev(this);

    newLeaf.setParent(this.getParent());

    K promotedKey = newLeaf.getKeys()[0];
    return new Node.SplitResult<>(newLeaf, promotedKey);
  }

  @Override
  public boolean canMergeWith(Node<K, V> other) {
    if (other == null || !other.isLeaf()) {
      return false;
    }

    LeafNode<K, V> otherLeaf = (LeafNode<K, V>) other;

    if ((this.keyCount + otherLeaf.getKeyCount()) > MAX_KEYS) {
      return false;
    }

    return areAdjacentSiblings(otherLeaf);
  }

  /**
   * Checks if this node and the other node are adjacent siblings.
   * @param other the other leaf node to check
   * @return true if they are adjacent siblings
   */
  private boolean areAdjacentSiblings(LeafNode<K, V> other) {
    return this.next == other || this.prev == other;
  }

  @Override
  public void mergeWith(Node<K, V> other, K separatorKey) {
    if (!canMergeWith(other)) {
      throw new IllegalArgumentException("Cannot merge: nodes are not compatible");
    }

    LeafNode<K, V> otherLeaf = (LeafNode<K, V>) other;
    LeafNodeImpl<K, V> otherLeafImpl = (LeafNodeImpl<K, V>) other;

    for (int i = 0; i < otherLeaf.getKeyCount(); i++) {
      this.keys[this.keyCount + i] = otherLeaf.getKeys()[i];
      this.values[this.keyCount + i] = otherLeafImpl.values[i];
    }
    this.keyCount += otherLeaf.getKeyCount();

    this.next = otherLeaf.getNext();
    if (otherLeaf.getNext() != null) {
      otherLeaf.getNext().setPrev(this);
    }

    other.clear();
  }

  @Override
  public void insertKeyAndChild(K key, Node<K, V> rightChild) {
    throw new UnsupportedOperationException("Leaf nodes do not have children");
  }

  @Override
  public K borrowFromLeft(Node<K, V> leftSibling) {
    if (!canBorrowFromSibling(leftSibling)) {
      return null;
    }

    LeafNode<K, V> leftLeaf = (LeafNode<K, V>) leftSibling;
    LeafNodeImpl<K, V> leftLeafImpl = (LeafNodeImpl<K, V>) leftSibling;

    for (int i = keyCount; i > 0; i--) {
      keys[i] = keys[i - 1];
      values[i] = values[i - 1];
    }

    int lastIndex = leftLeaf.getKeyCount() - 1;
    keys[0] = leftLeaf.getKeys()[lastIndex];
    values[0] = leftLeafImpl.values[lastIndex];

    leftLeafImpl.keys[lastIndex] = null;
    leftLeafImpl.values[lastIndex] = null;

    this.keyCount++;
    leftLeafImpl.keyCount--;

    return keys[0];
  }

  @Override
  public K borrowFromRight(Node<K, V> rightSibling) {
    if (!canBorrowFromSibling(rightSibling)) {
      return null;
    }

    LeafNode<K, V> rightLeaf = (LeafNode<K, V>) rightSibling;
    LeafNodeImpl<K, V> rightLeafImpl = (LeafNodeImpl<K, V>) rightSibling;

    keys[keyCount] = rightLeaf.getKeys()[0];
    values[keyCount] = rightLeafImpl.values[0];

    for (int i = 0; i < rightLeaf.getKeyCount() - 1; i++) {
      rightLeafImpl.keys[i] = rightLeafImpl.keys[i + 1];
      rightLeafImpl.values[i] = rightLeafImpl.values[i + 1];
    }

    int lastIndex = rightLeaf.getKeyCount() - 1;
    rightLeafImpl.keys[lastIndex] = null;
    rightLeafImpl.values[lastIndex] = null;

    this.keyCount++;
    rightLeafImpl.keyCount--;

    return rightLeaf.getKeys()[0];
  }

  /**
   * Determines if this LeafNodeImpl can borrow from the given sibling.
   * @param sibling the potential sibling to borrow from
   * @return true if borrowing is possible
   */
  private boolean canBorrowFromSibling(Node<K, V> sibling) {
    return sibling != null && sibling.isLeaf() && sibling.getKeyCount() > MIN_KEYS;
  }

  @Override
  public boolean insertUnique(K key, V value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }

    int existingIndex = findKeyIndex(key);
    if (existingIndex >= 0) {
      values[existingIndex] = value;
      return false;
    }

    if (this.isFull()) {
      throw new IllegalStateException("Cannot insert into full leaf node - split required");
    }

    int insertIndex = getInsertionIndex(key);

    for (int i = keyCount; i > insertIndex; i--) {
      keys[i] = keys[i - 1];
      values[i] = values[i - 1];
    }

    keys[insertIndex] = key;
    values[insertIndex] = value;
    keyCount++;
    return true;
  }

  @Override
  public V deleteKey(K key) throws IllegalArgumentException {
    int index = findKeyIndex(key);
    if (index == -1) {
      throw new IllegalArgumentException("Key not found: " + key);
    }

    V deletedValue = this.values[index];

    for (int i = index; i < keyCount - 1; i++) {
      this.keys[i] = this.keys[i + 1];
      this.values[i] = this.values[i + 1];
    }

    keys[keyCount - 1] = null;
    values[keyCount - 1] = null;
    keyCount--;

    return deletedValue;
  }

  /**
   * Finds the index of a key in the keys array.
   * @param key the key to find
   * @return the index of the key, or -1 if not found
   */
  private int findKeyIndex(K key) {
    for (int i = 0; i < keyCount; i++) {
      if (keys[i].equals(key)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Finds the appropriate index for inserting a key while maintaining sorted order.
   * @param key the key to find insertion position for.
   * @return the index where the key should be inserted.
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
    if (key == null) {
      return null;
    }
    int index = findKeyIndex(key);
    return index >= 0 ? values[index] : null;
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
  public boolean contains(K key) {
    return this.get(key) != null;
  }

  @Override
  public LeafNode<K, V> getNext() {
    return next;
  }

  @Override
  public void setNext(LeafNode<K, V> next) {
    this.next = next;
  }

  @Override
  public LeafNode<K, V> getPrev() {
    return prev;
  }

  @Override
  public void setPrev(LeafNode<K, V> prev) {
    this.prev = prev;
  }

  @Override
  public V[] getValues() {
    return Arrays.copyOf(values, this.keyCount);
  }
}