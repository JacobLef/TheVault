package model.bplustree;

/**
 * Interface for internal nodes in a B+ tree that contain only keys and child pointers.
 * Internal nodes guide searches toward the appropriate leaf nodes but store no actual values.
 * Each internal node maintains the B+ tree's balanced structure by directing traversal operations
 * to the correct subtrees.
 *
 * <p>
 *   Internal nodes serve as the navigation layer of the B+ tree, containing separator keys that
 *   determine which child subtree should be searched for a given key. They maintain references
 *   to both leaf nodes and other internal nodes as children, enabling the tree's hierarchical
 *   structure and ensuring balanced access patterns.
 * </p>
 *
 * <p>
 *   This interface provides type-safe operations specific to internal nodes, including child
 *   management and tree restructuring operations, while preventing inappropriate data storage
 *   operations that should only be performed on leaf nodes.
 * </p>
 *
 * @param <K> The type of keys used for navigation (must implement Comparable)
 * @param <V> The type parameter for tree values (not stored in internal nodes)
 */
public interface InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
  /**
   * Gets the children array of this internal node.
   *
   * @return the array of child nodes.
   */
  Node<K, V>[] getChildren();

  /**
   * Sets a child node at the specified index.
   *
   * @param index the index to set the child at.
   * @param child the child node to set.
   */
  void setChild(int index, Node<K, V> child);
}
