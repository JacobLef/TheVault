package model.data_engine;

/**
 * Base class for all nodes in a B+ tree, providing common functionality
 * for key storage, parent relationships, and node operations shared by
 * both internal and leaf nodes.
 * @param <K> The type of the Keys stored by this Node.
 * @param <V> The type of the Values stored by this Node.
 */
public abstract class Node<K extends Comparable<K>, V> implements BPlusTree<K, V> {

}
