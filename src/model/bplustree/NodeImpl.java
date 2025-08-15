package model.bplustree;

/**
 * Abstract base implementation for all nodes in a B+ tree, providing common functionality
 * for key storage, parent relationships, and internal tree operations shared by
 * both internal and leaf nodes.
 *
 * <p>
 *  This class handles the structural aspects of B+ tree nodes while implementing
 *  common operations from the Node interface with shared state management.
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
  protected Node<K, V> parent;

  /**
   * Creates a new NodeImpl with empty key array and no parent.
   */
  @SuppressWarnings("unchecked")
  protected NodeImpl() {
    this.keys = (K[]) new Comparable[MAX_KEYS];
    this.keyCount = 0;
    this.parent = null;
  }

  @Override
  public K[] getKeys() { return keys; }

  @Override
  public int getKeyCount() { return keyCount; }

  @Override
  public Node<K, V> getParent() { return parent; }

  @Override
  public void setParent(Node<K, V> parent) {
    this.parent = parent;
  }

  @Override
  public boolean isFull() {
    return this.keyCount >= MAX_KEYS;
  }

  @Override
  public boolean isUnderflow() {
    return this.keyCount < MIN_KEYS;
  }

  @Override
  public Node<K, V> getLeftSibling() {
    if (parent == null || !(parent instanceof InternalNode)) {
      return null;
    }

    InternalNode<K, V> parentInternal = (InternalNodeImpl<K, V>) parent;
    Node<K, V>[] children = parentInternal.getChildren();

    for (int i = 1; i <= parent.getKeyCount(); i++) {
      if (children[i] == this) {
        return children[i - 1];
      }
    }
    return null;
  }

  @Override
  public Node<K, V> getRightSibling() {
    if (parent == null || !(parent instanceof InternalNode)) {
      return null;
    }

    InternalNode<K, V> parentInternal = (InternalNodeImpl<K, V>) parent;
    Node<K, V>[] children = parentInternal.getChildren();

    for (int i = 0; i < parent.getKeyCount(); i++) {
      if (children[i] == this) {
        return children[i + 1];
      }
    }
    return null;
  }

  @Override
  public int getIndexInParent() {
    if (parent == null || !(parent instanceof InternalNode)) {
      return -1;
    }

    InternalNode<K, V> parentInternal = (InternalNodeImpl<K, V>) parent;
    Node<K, V>[] children = parentInternal.getChildren();

    for (int i = 0; i <= parent.getKeyCount(); i++) {
      if (children[i] == this) {
        return i;
      }
    }
    return -1;
  }
}