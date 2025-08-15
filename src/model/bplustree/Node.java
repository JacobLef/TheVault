package model.bplustree;

/**
 * Interface defining the contract for all nodes in a B+ tree.
 * Provides essential operations for tree traversal, data access, structural queries,
 * and tree management operations that are common to both internal and leaf nodes.
 *
 * <p>
 *   This interface is the top-level hierarchy for all Node interfaces and thus only provides
 *   base functionality which both data-carrying nodes (leaves) and traverse-helper nodes
 *   (internal) must offer. All other data or traverse specific functionality is purposefully
 *   separated into two sub-interfaces.
 * </p>
 *
 * @param <K> The type of the Keys stored by this Node (must be comparable).
 * @param <V> The type of the Values stored by this Node.
 *
 * @see LeafNode for data related functionality.
 * @see InternalNode for traverse related functionality.
 */
public interface Node<K extends Comparable<K>, V> {

  /**
   * Result of a node split operation containing both the new node and the key to promote.
   * @param <K> The type of keys in the split result.
   * @param <V> The type of values in the split result.
   */
  record SplitResult<K extends Comparable<K>, V>(Node<K, V> newNode, K promotedKey) { }

  /**
   * Determines if this Node is a leaf node.
   * @return {@code true} if this is a leaf node, {@code false} if internal node.
   */
  boolean isLeaf();

  /**
   * Retrieves the value associated with the given key.
   * @param key the key to search for.
   * @return the value associated with the key, or {@code null} if not found.
   */
  V get(K key);

  /**
   * Determines the minimum key stored in this subtree.
   * @return the minimum key value or {@code null} for an empty subtree.
   */
  K getMinKey();

  /**
   * Determines the maximum key stored in this subtree.
   * @return the maximum key value or {@code null} for an empty subtree.
   */
  K getMaxKey();

  /**
   * Calculates the height of this subtree.
   * @return the height of this subtree, with leaf nodes having height 1.
   */
  int height();

  /**
   * Counts the total number of key-value pairs stored in this subtree.
   * @return the total number of entries in this subtree.
   */
  int size();

  /**
   * Determines if the given key exists in this subtree.
   * @param key the key to search for.
   * @return {@code true} if the key exists, {@code false} otherwise.
   */
  boolean contains(K key);

  /**
   * Clears all keys, values, and relationships from this Node and its subtree.
   * Recursively clears all child nodes and resets this node to empty state.
   */
  void clear();

  /**
   * Fetches the keys array of this Node.
   * @return an array of all keys stored in this Node, in ascending order.
   */
  K[] getKeys();

  /**
   * Determines the number of keys currently stored in this Node.
   * @return the count of keys in this Node.
   */
  int getKeyCount();

  /**
   * Fetches the parent Node of this Node.
   * @return the parent Node or {@code null} if this is the root node.
   */
  Node<K, V> getParent();

  /**
   * Sets the parent Node of this Node.
   * @param parent the new parent Node for this Node.
   */
  void setParent(Node<K, V> parent);

  /**
   * Determines if this Node has reached its maximum key capacity.
   * @return {@code true} if this Node cannot accept more keys without splitting.
   */
  boolean isFull();

  /**
   * Determines if this Node has too few keys according to B+ tree rules.
   * @return {@code true} if this Node requires re-balancing due to underflow.
   */
  boolean isUnderflow();

  /**
   * Finds the left sibling of this Node within the parent's children.
   * @return the left sibling Node or {@code null} if none exists or no parent.
   */
  Node<K, V> getLeftSibling();

  /**
   * Finds the right sibling of this Node within the parent's children.
   * @return the right sibling Node or {@code null} if none exists or no parent.
   */
  Node<K, V> getRightSibling();

  /**
   * Determines the index position of this Node within its parent's children array.
   * @return the index of this Node in parent's children, or -1 if no parent or not found.
   */
  int getIndexInParent();

  /**
   * Splits this Node into two nodes when it exceeds maximum capacity.
   * This Node retains the left portion of keys, while a new node contains the right portion.
   * The specific splitting behavior differs between internal and leaf nodes.
   * @return a SplitResult containing the new right node and the key to promote to parent.
   */
  SplitResult<K, V> split();

  /**
   * Determines if this Node can be merged with another Node without exceeding capacity.
   * Both nodes must be of the same type (both internal or both leaf) and their combined
   * key count must not violate B+ tree capacity constraints.
   * @param other the Node to potentially merge with.
   * @return {@code true} if merging would not violate B+ tree capacity constraints.
   */
  boolean canMergeWith(Node<K, V> other);

  /**
   * Merges this Node with another Node, combining all keys and children/values.
   * The merging process differs between internal and leaf nodes. For internal nodes,
   * a separator key from the parent is incorporated. For leaf nodes, the linked list
   * structure is maintained.
   * @param other the Node to merge into this Node.
   * @param separatorKey the key from the parent that separates these two nodes.
   *                     May be null for leaf node merges where it's not used.
   */
  void mergeWith(Node<K, V> other, K separatorKey);

  /**
   * Inserts a key and corresponding right child pointer into this Node.
   * This operation is only applicable to internal nodes as they maintain child pointers.
   * Leaf nodes will throw UnsupportedOperationException since they store values, not children.
   * The key and child are inserted in the correct position to maintain sorted order.
   * @param key the key to insert, typically promoted from a child split operation.
   * @param rightChild the new child node to the right of the inserted key.
   * @throws UnsupportedOperationException if called on a leaf node.
   */
  void insertKeyAndChild(K key, Node<K, V> rightChild);

  /**
   * Borrows a key from the left sibling to resolve underflow condition.
   * This operation moves the rightmost key from the left sibling to this Node
   * and updates the parent's separator key accordingly. The borrowing behavior
   * differs between internal and leaf nodes.
   * @param leftSibling the left sibling node to borrow from.
   * @return the new separator key for the parent, or {@code null} if borrowing failed
   *         (e.g., left sibling has minimum keys or is incompatible type).
   */
  K borrowFromLeft(Node<K, V> leftSibling);

  /**
   * Borrows a key from the right sibling to resolve underflow condition.
   * This operation moves the leftmost key from the right sibling to this Node
   * and updates the parent's separator key accordingly. The borrowing behavior
   * differs between internal and leaf nodes.
   * @param rightSibling the right sibling node to borrow from.
   * @return the new separator key for the parent, or {@code null} if borrowing failed
   *         (e.g., right sibling has minimum keys or is incompatible type).
   */
  K borrowFromRight(Node<K, V> rightSibling);
}