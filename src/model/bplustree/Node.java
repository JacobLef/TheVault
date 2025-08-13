package model.bplustree;

/**
 * Base class for all nodes in a B+ tree, providing common functionality
 * for key storage, parent relationships, and node operations shared by
 * both internal and leaf nodes.
 * @param <K> The type of the Keys stored by this Node.
 * @param <V> The type of the Values stored by this Node.
 */
public abstract class Node<K extends Comparable<K>, V> implements BPlusTree<K, V> {
  protected static final int ORDER = 4;
  protected static final int MAX_KEYS = ORDER - 1;
  protected static final int MIN_KEYS = (ORDER + 1) / 2 - 1;

  protected K[] keys;
  protected int keyCount;
  protected Node<K, V> parent;

  /**
   * Creates a new Node with the specified leaf status.
   */
  @SuppressWarnings("unchecked")
  protected Node() {
    this.keys = (K[]) new Comparable[MAX_KEYS];
    this.keyCount = 0;
    this.parent = null;
  }

  /**
   * Fetches the keys of this Node.
   * @return an Array of all the keys stored in this Node, in ascending order.
   */
  K[] getKeys() { return keys; }

  /**
   * Determines the number of keys stored in this Node.
   * @return the number of keys.
   */
  int getKeyCount() { return keyCount; }

  /**
   * Fetches the parent Node of this Node.
   * @return the Node corresponding to the parent or {@code null} if no parent exists.
   */
  Node<K, V> getParent() { return parent; }

  /**
   * Determines if this Node is a Leaf.
   * @return true if it is and false otherwise.
   */
  abstract boolean isLeaf();

  /**
   * Determines if this Node has its maximum number of keys already.
   * @return true if it does and false otherwise.
   */
  abstract boolean isFull();

  /**
   * Determines if this Node has too many keys with respect to B+ Tree rules.
   * @return true if it is and false otherwise.
   */
  abstract boolean isUnderflow();

  /**
   * Inserts the given (key, value) pair into this Node.
   * @param key   the key to be inserted.
   * @param value the value to be inserted.
   * @return true if the insertion was unique and false if it updated an already present entry.
   */
  abstract boolean insertKey(K key, V value);

  /**
   * Removes the given (key, value) pair from this Node.
   * @param key the key to be removed.
   * @return the value associated with the given key.
   */
  abstract V removeKey(K key);

  /**
   * Finds the next key in the sequence after the given key in this Node.
   * @param key the key whose successor is to be found.
   * @return the next Key or {@code null} if none.
   */
  abstract K findNextKey(K key);

  /**
   * Splits this Node and updates this Node to account for the split. For implementation
   * purposes, this Node will always take the left half of the split and the returned node is
   * always the right half of the split node.
   * @return the other half of this Node after the split.
   */
  abstract Node<K, V> split();

  /**
   * Determines if this Node can merge with another Node. To be merged, the newly merged Node
   * must maintain all B+ tree properties regarding the combination of the keys and
   * children or values (depending on type of Node).
   * @param other the mode to be tested for merging.
   * @return true if it can be merged and false otherwise.
   */
  abstract boolean canMergeWith(Node<K, V> other);

  /**
   * Merges this Node with the given Node, such that only one combined Node exists and all values
   * stored within the given Node are also reflected in this Node.
   * @param other the node to be merged with this Node.
   */
  abstract void mergeWith(Node<K, V> other);
}
