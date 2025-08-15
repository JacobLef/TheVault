package model.bplustree;

/**
 * Interface for leaf nodes in a B+ tree that store key-value pairs and provide actual data storage.
 * Leaf nodes are linked together to enable efficient sequential access and range queries.
 * This is the only type of node that contains actual values in the B+ tree structure.
 *
 * @param <K> The type of the Keys stored in this LeafNode (must be comparable).
 * @param <V> The type of the Values stored in this LeafNode.
 */
public interface LeafNode<K extends Comparable<K>, V> extends Node<K, V> {

  /**
   * Inserts a key-value pair into this leaf node.
   * Updates the value if the key already exists, otherwise inserts at the appropriate position.
   * @param key the key to insert.
   * @param value the value to associate with the key.
   * @return true if a new key was added, false if an existing key was updated.
   * @throws IllegalStateException if the node is full and requires splitting.
   */
  boolean insertUnique(K key, V value);

  /**
   * Deletes a key-value pair from this leaf node.
   * @param key the key to delete.
   * @return the value that was associated with the deleted key.
   * @throws IllegalArgumentException if the key is not found.
   */
  V deleteKey(K key) throws IllegalArgumentException;

  /**
   * Gets the next leaf node in the linked list.
   * @return the next leaf node or null if this is the last leaf.
   */
  LeafNode<K, V> getNext();

  /**
   * Sets the next leaf node in the linked list.
   * @param next the next leaf node.
   */
  void setNext(LeafNode<K, V> next);

  /**
   * Gets the previous leaf node in the linked list.
   * @return the previous leaf node or null if this is the first leaf.
   */
  LeafNode<K, V> getPrev();

  /**
   * Sets the previous leaf node in the linked list.
   * @param prev the previous leaf node.
   */
  void setPrev(LeafNode<K, V> prev);

  /**
   * Gets the values array for this leaf node.
   * @return the array of values stored in this leaf node.
   */
  V[] getValues();
}