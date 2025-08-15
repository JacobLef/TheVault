package model.bplustree;

/**
 * A leaf node in a B+ tree that stores key-value pairs and provides the actual data storage.
 * Leaf nodes are linked together to enable efficient sequential access and range queries.
 * This is the only type of node that contains actual values in the B+ tree structure.
 *
 * @param <K> The type of the Keys stored in this LeafNode (must be comparable).
 * @param <V> The type of the Values stored in this LeafNode.
 */
public class LeafNode<K extends Comparable<K>, V> extends NodeImpl<K, V> {
  private V[] values;
  private LeafNode<K, V> next;
  private LeafNode<K, V> prev;

  /**
   * Creates a new empty LeafNode with no linked neighbors.
   */
  @SuppressWarnings("unchecked")
  public LeafNode() {
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
  public LeafNode(LeafNode<K, V> next) {
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
  boolean isFull() {
    return this.keyCount >= MAX_KEYS;
  }

  @Override
  boolean isUnderflow() {
    return this.keyCount < MIN_KEYS;
  }

  @Override
  SplitResult<K, V> split() {
    LeafNode<K, V> newLeaf = new LeafNode<>();
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

    K promotedKey = newLeaf.keys[0];
    return new SplitResult<>(newLeaf, promotedKey);
  }

  @Override
  boolean canMergeWith(NodeImpl<K, V> other) {
    if (other == null || !other.isLeaf()) {
      return false;
    }

    LeafNode<K, V> otherLeaf = (LeafNode<K, V>) other;
    return (this.keyCount + otherLeaf.keyCount) <= MAX_KEYS;
  }

  @Override
  void mergeWith(NodeImpl<K, V> other, K separatorKey) {
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
  void insertKeyAndChild(K key, NodeImpl<K, V> rightChild) {
    throw new UnsupportedOperationException("Leaf nodes do not have children");
  }

  @Override
  K borrowFromLeft(NodeImpl<K, V> leftSibling) {
    if (!leftSibling.isLeaf() || leftSibling.getKeyCount() <= MIN_KEYS) {
      return null;
    }

    LeafNode<K, V> leftLeaf = (LeafNode<K, V>) leftSibling;

    for (int i = keyCount; i > 0; i--) {
      keys[i] = keys[i - 1];
      values[i] = values[i - 1];
    }

    keys[0] = leftLeaf.keys[leftLeaf.keyCount - 1];
    values[0] = leftLeaf.values[leftLeaf.keyCount - 1];

    leftLeaf.keys[leftLeaf.keyCount - 1] = null;
    leftLeaf.values[leftLeaf.keyCount - 1] = null;

    this.keyCount++;
    leftLeaf.keyCount--;

    return keys[0];
  }

  @Override
  K borrowFromRight(NodeImpl<K, V> rightSibling) {
    if (!rightSibling.isLeaf() || rightSibling.getKeyCount() <= MIN_KEYS) {
      return null;
    }

    LeafNode<K, V> rightLeaf = (LeafNode<K, V>) rightSibling;

    keys[keyCount] = rightLeaf.keys[0];
    values[keyCount] = rightLeaf.values[0];

    for (int i = 0; i < rightLeaf.keyCount - 1; i++) {
      rightLeaf.keys[i] = rightLeaf.keys[i + 1];
      rightLeaf.values[i] = rightLeaf.values[i + 1];
    }

    rightLeaf.keys[rightLeaf.keyCount - 1] = null;
    rightLeaf.values[rightLeaf.keyCount - 1] = null;

    this.keyCount++;
    rightLeaf.keyCount--;

    return rightLeaf.keys[0];
  }

  /**
   * Inserts a key-value pair into this leaf node.
   * Updates the value if the key already exists, otherwise inserts at the appropriate position.
   * @param key the key to insert.
   * @param value the value to associate with the key.
   * @return true if a new key was added, false if an existing key was updated.
   * @throws IllegalStateException if the node is full and requires splitting.
   */
  boolean insertUnique(K key, V value) {
    boolean keyExists = this.contains(key);
    if (keyExists) {
      for (int i = 0; i < keyCount; i++) {
        if (keys[i].equals(key)) {
          values[i] = value;
          break;
        }
      }
    } else {
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
    }
    return !keyExists;
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

  /**
   * Deletes a key-value pair from this leaf node.
   * @param key the key to delete.
   * @return the value that was associated with the deleted key.
   * @throws IllegalArgumentException if the key is not found.
   */
  V deleteKey(K key) throws IllegalArgumentException {
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

    V deletedValue = this.values[index];
    for (int i = index; i < keyCount - 1; i++) {
      this.values[i] = this.values[i + 1];
      this.keys[i] = this.keys[i + 1];
    }

    keys[keyCount - 1] = null;
    values[keyCount - 1] = null;
    keyCount--;

    return deletedValue;
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
  public boolean contains(K key) {
    return this.get(key) != null;
  }

  /**
   * Gets the next leaf node in the linked list.
   * @return the next leaf node or null if this is the last leaf.
   */
  LeafNode<K, V> getNext() {
    return next;
  }

  /**
   * Sets the next leaf node in the linked list.
   * @param next the next leaf node.
   */
  void setNext(LeafNode<K, V> next) {
    this.next = next;
  }

  /**
   * Gets the previous leaf node in the linked list.
   * @return the previous leaf node or null if this is the first leaf.
   */
  LeafNode<K, V> getPrev() {
    return prev;
  }

  /**
   * Sets the previous leaf node in the linked list.
   * @param prev the previous leaf node.
   */
  void setPrev(LeafNode<K, V> prev) {
    this.prev = prev;
  }

  /**
   * Gets the values array for this leaf node.
   * @return the array of values stored in this leaf node.
   */
  V[] getValues() {
    return values;
  }
}