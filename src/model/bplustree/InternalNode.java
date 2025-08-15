package model.bplustree;

/**
 * An internal node in a B+ tree that contains only keys and child pointers for navigation.
 * Internal nodes guide searches toward the appropriate leaf nodes but store no actual values.
 * Each internal node maintains the B+ tree's balanced structure by directing traversal
 * operations to the correct subtrees.
 *
 * @param <K> The type of the Keys stored in this InternalNode (must be comparable).
 * @param <V> InternalNode's do not store values, so this is ignored for all specification purposes.
 */
public class InternalNode<K extends Comparable<K>, V> extends NodeImpl<K, V> {
  private final NodeImpl<K, V>[] children;

  /**
   * Creates a new empty InternalNode with no children.
   */
  @SuppressWarnings("unchecked")
  public InternalNode() {
    super();
    this.children = new NodeImpl[ORDER];
  }

  /**
   * Creates a new InternalNode with the specified first child.
   * @param firstChild the initial child node of this InternalNode.
   */
  @SuppressWarnings("unchecked")
  public InternalNode(NodeImpl<K, V> firstChild) {
    super();
    this.children = new NodeImpl[ORDER];
    this.children[0] = firstChild;
    if (firstChild != null) {
      firstChild.parent = this;
    }
  }

  @Override
  public boolean isLeaf() {
    return false;
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
    InternalNode<K, V> newInternal = new InternalNode<>();
    int splitIndex = keyCount / 2;
    K promotedKey = keys[splitIndex];
    int moveCount = keyCount - splitIndex - 1;

    for (int i = 0; i < moveCount; i++) {
      newInternal.keys[i] = this.keys[splitIndex + 1 + i];
      this.keys[splitIndex + 1 + i] = null;
    }

    for (int i = 0; i <= moveCount; i++) {
      newInternal.children[i] = this.children[splitIndex + 1 + i];
      if (newInternal.children[i] != null) {
        newInternal.children[i].parent = newInternal;
      }
      this.children[splitIndex + 1 + i] = null;
    }

    newInternal.keyCount = moveCount;
    this.keys[splitIndex] = null;
    this.keyCount = splitIndex;

    newInternal.parent = this.parent;

    return new SplitResult<>(newInternal, promotedKey);
  }

  @Override
  boolean canMergeWith(NodeImpl<K, V> other) {
    if (other == null || other.isLeaf()) {
      return false;
    }

    InternalNode<K, V> otherInternal = (InternalNode<K, V>) other;
    return (this.keyCount + otherInternal.keyCount + 1) <= MAX_KEYS;
  }

  @Override
  void mergeWith(NodeImpl<K, V> other, K separatorKey) {
    if (!canMergeWith(other)) {
      throw new IllegalArgumentException("Cannot merge: combined size exceeds maximum");
    }

    InternalNode<K, V> otherInternal = (InternalNode<K, V>) other;

    this.keys[this.keyCount] = separatorKey;
    this.keyCount++;

    for (int i = 0; i < otherInternal.keyCount; i++) {
      this.keys[this.keyCount + i] = otherInternal.keys[i];
    }

    for (int i = 0; i <= otherInternal.keyCount; i++) {
      this.children[this.keyCount + i] = otherInternal.children[i];
      if (this.children[this.keyCount + i] != null) {
        this.children[this.keyCount + i].parent = this;
      }
    }

    this.keyCount += otherInternal.keyCount;

    otherInternal.clear();
  }

  @Override
  void insertKeyAndChild(K key, NodeImpl<K, V> rightChild) {
    int insertIndex = 0;
    for (int i = 0; i < keyCount; i++) {
      if (key.compareTo(keys[i]) < 0) {
        break;
      }
      insertIndex = i + 1;
    }

    for (int i = keyCount; i > insertIndex; i--) {
      keys[i] = keys[i - 1];
      children[i + 1] = children[i];
    }

    keys[insertIndex] = key;
    children[insertIndex + 1] = rightChild;
    if (rightChild != null) {
      rightChild.parent = this;
    }
    keyCount++;
  }

  @Override
  K borrowFromLeft(NodeImpl<K, V> leftSibling) {
    if (leftSibling.isLeaf() || leftSibling.getKeyCount() <= MIN_KEYS) {
      return null;
    }

    InternalNode<K, V> leftInternal = (InternalNode<K, V>) leftSibling;

    for (int i = keyCount; i > 0; i--) {
      keys[i] = keys[i - 1];
    }
    for (int i = keyCount + 1; i > 0; i--) {
      children[i] = children[i - 1];
    }

    K borrowedKey = leftInternal.keys[leftInternal.keyCount - 1];
    keys[0] = borrowedKey;
    children[0] = leftInternal.children[leftInternal.keyCount];
    if (children[0] != null) {
      children[0].parent = this;
    }

    leftInternal.keys[leftInternal.keyCount - 1] = null;
    leftInternal.children[leftInternal.keyCount] = null;
    leftInternal.keyCount--;
    this.keyCount++;

    return borrowedKey;
  }

  @Override
  K borrowFromRight(NodeImpl<K, V> rightSibling) {
    if (rightSibling.isLeaf() || rightSibling.getKeyCount() <= MIN_KEYS) {
      return null;
    }

    InternalNode<K, V> rightInternal = (InternalNode<K, V>) rightSibling;

    K borrowedKey = rightInternal.keys[0];
    keys[keyCount] = borrowedKey;
    children[keyCount + 1] = rightInternal.children[0];
    if (children[keyCount + 1] != null) {
      children[keyCount + 1].parent = this;
    }

    for (int i = 0; i < rightInternal.keyCount - 1; i++) {
      rightInternal.keys[i] = rightInternal.keys[i + 1];
    }
    for (int i = 0; i < rightInternal.keyCount; i++) {
      rightInternal.children[i] = rightInternal.children[i + 1];
    }

    rightInternal.keys[rightInternal.keyCount - 1] = null;
    rightInternal.children[rightInternal.keyCount] = null;

    rightInternal.keyCount--;
    this.keyCount++;

    return rightInternal.getMinKey();
  }

  /**
   * Finds the appropriate child node for the given key.
   * @param key the key to find the child for.
   * @return the child node that should contain the key.
   */
  private NodeImpl<K, V> findChild(K key) {
    int childIndex = 0;
    for (int i = 0; i < keyCount; i++) {
      if (key.compareTo(keys[i]) < 0) {
        break;
      }
      childIndex = i + 1;
    }
    return children[childIndex];
  }

  @Override
  public void clear() {
    for (int i = 0; i < keyCount; i++) {
      keys[i] = null;
    }
    for (int i = 0; i <= keyCount; i++) {
      if (children[i] != null) {
        children[i].clear();
        children[i] = null;
      }
    }
    keyCount = 0;
    this.parent = null;
  }

  @Override
  public V get(K key) {
    NodeImpl<K, V> child = findChild(key);
    return child.get(key);
  }

  @Override
  public K getMinKey() {
    if (children[0] != null) {
      return children[0].getMinKey();
    }
    return null;
  }

  @Override
  public K getMaxKey() {
    if (children[keyCount] != null) {
      return children[keyCount].getMaxKey();
    }
    return null;
  }

  @Override
  public int height() {
    if (children[0] != null) {
      return 1 + children[0].height();
    }
    return 1;
  }

  @Override
  public int size() {
    int totalSize = 0;
    for (int i = 0; i <= keyCount; i++) {
      if (children[i] != null) {
        totalSize += children[i].size();
      }
    }
    return totalSize;
  }

  @Override
  public boolean contains(K key) {
    NodeImpl<K, V> child = findChild(key);
    return child.contains(key);
  }

  /**
   * Gets the children array of this internal node.
   * @return the array of child nodes.
   */
  NodeImpl<K, V>[] getChildren() {
    return children;
  }

  /**
   * Sets a child node at the specified index.
   * @param index the index to set the child at.
   * @param child the child node to set.
   */
  void setChild(int index, NodeImpl<K, V> child) {
    children[index] = child;
    if (child != null) {
      child.parent = this;
    }
  }
}