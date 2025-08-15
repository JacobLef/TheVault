package model.bplustree;

/**
 * Interface defining the contract for all nodes in a B+ tree.
 * Provides essential operations for tree traversal, data access, and structural queries
 * that are common to both internal and leaf nodes.
 *
 * @param <K> The type of the Keys stored by this Node (must be comparable).
 * @param <V> The type of the Values stored by this Node.
 */
public interface Node<K extends Comparable<K>, V> {

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
}