package model.bplustree;

/**
 * Abstract base implementation for all nodes in a B+ tree, providing common functionality
 * for key storage, parent relationships, and internal tree operations shared by
 * both internal and leaf nodes.
 *
 * <p>
 *  This class handles the structural aspects of B+ tree nodes while delegating
 *  data access methods to the Node interface.
 * </p>
 * @param <K> The type of the Keys stored by this Node.
 * @param <V> The type of the Values stored by this Node.
 */
public abstract class NodeImpl<K extends Comparable<K>, V> implements Node<K, V> {
  protected static final int ORDER = 4;
  protected static final int MAX_KEYS = ORDER - 1;
  protected static final int MIN_KEYS = (ORDER + 1) / 2 - 1;

  protected K[] keys;
  protected int keyCount;
  protected NodeImpl<K, V> parent;

  /**
   * Creates a new NodeImpl with empty key array and no parent.
   */
  @SuppressWarnings("unchecked")
  protected NodeImpl() {
    this.keys = (K[]) new Comparable[MAX_KEYS];
    this.keyCount = 0;
    this.parent = null;
  }

  /**
   * Result of a node split operation containing both the new node and the key to promote.
   * @param <K> The type of keys in the split result.
   * @param <V> The type of values in the split result.
   */
  public record SplitResult<K extends Comparable<K>, V>(NodeImpl<K, V> newNode, K promotedKey) { }

  /**
   * Fetches the keys array of this NodeImpl.
   * @return an array of all keys stored in this NodeImpl, in ascending order.
   */
  K[] getKeys() { return keys; }

  /**
   * Determines the number of keys currently stored in this NodeImpl.
   * @return the count of keys in this NodeImpl.
   */
  int getKeyCount() { return keyCount; }

  /**
   * Fetches the parent NodeImpl of this NodeImpl.
   * @return the parent NodeImpl or {@code null} if this is the root node.
   */
  NodeImpl<K, V> getParent() { return parent; }

  /**
   * Sets the parent NodeImpl of this NodeImpl.
   * @param parent the new parent NodeImpl for this NodeImpl.
   */
  void setParent(NodeImpl<K, V> parent) { this.parent = parent; }

  /**
   * Determines if this NodeImpl has reached its maximum key capacity.
   * @return {@code true} if this NodeImpl cannot accept more keys without splitting.
   */
  abstract boolean isFull();

  /**
   * Determines if this NodeImpl has too few keys according to B+ tree rules.
   * @return {@code true} if this NodeImpl requires re-balancing due to underflow.
   */
  abstract boolean isUnderflow();

  /**
   * Splits this NodeImpl into two nodes when it exceeds maximum capacity.
   * This NodeImpl retains the left portion of keys, while a new node contains the right portion.
   * @return a SplitResult containing the new right node and the key to promote to parent.
   */
  abstract SplitResult<K, V> split();

  /**
   * Determines if this NodeImpl can be merged with another NodeImpl without exceeding capacity.
   * @param other the NodeImpl to potentially merge with.
   * @return {@code true} if merging would not violate B+ tree capacity constraints.
   */
  abstract boolean canMergeWith(NodeImpl<K, V> other);

  /**
   * Merges this NodeImpl with another NodeImpl, combining all keys and children.
   * @param other the NodeImpl to merge into this NodeImpl.
   * @param separatorKey the key from the parent that separates these two nodes.
   */
  abstract void mergeWith(NodeImpl<K, V> other, K separatorKey);

  /**
   * Inserts a key and corresponding right child pointer into this NodeImpl.
   * Only applicable to internal nodes; leaf nodes throw UnsupportedOperationException.
   * @param key the key to insert, typically promoted from a child split.
   * @param rightChild the new child node to the right of the inserted key.
   * @throws UnsupportedOperationException if called on a leaf node.
   */
  abstract void insertKeyAndChild(K key, NodeImpl<K, V> rightChild);

  /**
   * Borrows a key from the left sibling to resolve underflow condition.
   * @param leftSibling the left sibling node to borrow from.
   * @return the new separator key for the parent, or {@code null} if borrowing failed.
   */
  abstract K borrowFromLeft(NodeImpl<K, V> leftSibling);

  /**
   * Borrows a key from the right sibling to resolve underflow condition.
   * @param rightSibling the right sibling node to borrow from.
   * @return the new separator key for the parent, or {@code null} if borrowing failed.
   */
  abstract K borrowFromRight(NodeImpl<K, V> rightSibling);

  /**
   * Finds the left sibling of this NodeImpl within the parent's children.
   * @return the left sibling NodeImpl or {@code null} if none exists or no parent.
   */
  NodeImpl<K, V> getLeftSibling() {
    if (parent == null || !(parent instanceof InternalNode)) {
      return null;
    }

    InternalNode<K, V> parentInternal = (InternalNode<K, V>) parent;
    NodeImpl<K, V>[] children = parentInternal.getChildren();

    for (int i = 1; i <= parent.keyCount; i++) {
      if (children[i] == this) {
        return children[i - 1];
      }
    }
    return null;
  }

  /**
   * Finds the right sibling of this NodeImpl within the parent's children.
   * @return the right sibling NodeImpl or {@code null} if none exists or no parent.
   */
  NodeImpl<K, V> getRightSibling() {
    if (parent == null || !(parent instanceof InternalNode)) {
      return null;
    }

    InternalNode<K, V> parentInternal = (InternalNode<K, V>) parent;
    NodeImpl<K, V>[] children = parentInternal.getChildren();

    for (int i = 0; i < parent.keyCount; i++) {
      if (children[i] == this) {
        return children[i + 1];
      }
    }
    return null;
  }

  /**
   * Determines the index position of this NodeImpl within its parent's children array.
   * @return the index of this NodeImpl in parent's children, or -1 if no parent or not found.
   */
  int getIndexInParent() {
    if (parent == null || !(parent instanceof InternalNode)) {
      return -1;
    }

    InternalNode<K, V> parentInternal = (InternalNode<K, V>) parent;
    NodeImpl<K, V>[] children = parentInternal.getChildren();

    for (int i = 0; i <= parent.keyCount; i++) {
      if (children[i] == this) {
        return i;
      }
    }
    return -1;
  }
}