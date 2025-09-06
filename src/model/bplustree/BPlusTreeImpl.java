package model.bplustree;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static model.bplustree.NodeImpl.MIN_KEYS;

/**
 * Concrete implementation of a B+ tree data structure optimized for banking and financial
 * applications. This implementation provides efficient storage, retrieval, and range query
 * operations with guaranteed logarithmic time complexity for all primary operations.
 *
 * <p>
 *  The BPlusTreeImpl maintains the fundamental B+ tree properties of balanced structure where all
 *  leaf nodes are at the same level ensuring consistent performance, ordered storage where keys are
 *  maintained in sorted order for efficient range queries, sequential access where leaf nodes are
 *  linked for fast sequential traversal, and high fanout where internal nodes have multiple children
 *  to minimize tree height.
 * </p>
 *
 * <p>
 *  Performance Characteristics include Search at O(log n) for efficient key lookup, Insert at
 *  O(log n) maintaining balance through node splitting, Delete at O(log n) preserving balance
 *  through redistribution and merging, Range Query at O(log n + k) where k is the number of
 *  results, and sequential Scan at O(k) with direct leaf-to-leaf traversal.
 * </p>
 *
 * <p>
 *  This implementation utilizes a segregated node interface hierarchy with separate LeafNode and
 *  InternalNode interfaces to provide type-safe operations and prevent inappropriate method calls at
 *  the node level. The BPlusTree interface itself provides the high-level tree operations while
 *  delegating to the appropriate node implementations. The tree automatically handles re-balancing
 *  operations to maintain optimal performance as data is inserted and deleted.
 * </p>
 *
 * <p>
 *  Thread Safety: This implementation is not inherently thread-safe. For concurrent access
 *  in banking applications, external synchronization or concurrent wrapper implementations
 *  should be used.
 * </p>
 *
 * @param <K> The type of keys stored in the tree (must implement Comparable)
 * @param <V> The type of values associated with the keys
 *
 * @see BPlusTree Interface for high-level tree operations
 * @see LeafNode Interface for data storage operations
 * @see InternalNode Interface for tree navigation operations
 * @see Node Base interface for common node operations
 */
public class BPlusTreeImpl<K extends Comparable<K>, V> implements BPlusTree<K,V> {
  private Node<K, V> root;

  /**
   * Constructs a new BPlusTreeImpl with an {@code LeafNodeImpl} as the root, which has no left or
   * right neighbors and other default constructor values.
   */
  public BPlusTreeImpl() {
    this.root = new LeafNodeImpl<>();
  }

  @Override
  public boolean insert(K key, V value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }

    return insertHelper(key, value);
  }

  /**
   * Helper method for inserting a key-value pair into the tree.
   * Handles node splitting and tree height increases.
   * @param key the key to be inserted
   * @param value the value to be inserted
   * @return true if it is a new key and false otherwise.
   */
  private boolean insertHelper(K key, V value) {
    LeafNode<K, V> targetLeaf = findLeaf(key);

    try {
      boolean isNewKey = targetLeaf.insertUnique(key, value);
      return isNewKey;
    } catch (IllegalStateException e) {
      boolean isNewKey = handleLeafOverflow(targetLeaf, key, value);
      return isNewKey;
    }
  }

  /**
   * Handles overflow when inserting into a full leaf node. This method will split the current
   * node and thus create a new node, where both adhere to BPlusTree properties.
   * @param fullLeaf  the leaf to be split.
   * @param key       the key to be added.
   * @param value     the value to be added.
   * @return true at all times because a new leaf is created and this is split.
   */
  private boolean handleLeafOverflow(LeafNode<K, V> fullLeaf, K key, V value) {
    LeafNodeImpl<K, V> tempLeaf = createTempLeafWithNewEntry(fullLeaf, key, value);

    Node.SplitResult<K, V> splitResult = tempLeaf.split();
    LeafNode<K, V> newRightLeaf = (LeafNode<K, V>) splitResult.newNode();
    K promotedKey = splitResult.promotedKey();

    copyLeafData(tempLeaf, fullLeaf);

    insertIntoParent(fullLeaf, newRightLeaf, promotedKey);

    return true;
  }

  /**
   * Creates a temporary leaf with all existing entries plus the new one.
   * @param originalLeaf the leaf to create a temporary leaf from.
   * @param key the key to be added.
   * @param value the value to be added.
   * @return the newly created, temporary leaf that contains the given entry.
   */
  private LeafNodeImpl<K, V> createTempLeafWithNewEntry(
     LeafNode<K, V> originalLeaf,
     K key,
     V value
  ) {
    LeafNodeImpl<K, V> tempLeaf = new LeafNodeImpl<>();

    K[] originalKeys = originalLeaf.getKeys();
    V[] originalValues = originalLeaf.getValues();

    List<Map.Entry<K, V>> allEntries = new ArrayList<>();

    for (int i = 0; i < originalLeaf.getKeyCount(); i++) {
      allEntries.add(Map.entry(originalKeys[i], originalValues[i]));
    }

    allEntries.add(Map.entry(key, value));
    allEntries.sort(Map.Entry.comparingByKey());

    for (Map.Entry<K, V> entry : allEntries) {
      try {
        tempLeaf.insertUnique(entry.getKey(), entry.getValue());
      } catch (IllegalStateException ignored) {
        // This should not happen in temp leaf creation
      }
    }

    return tempLeaf;
  }

  /**
   * Copies data from source leaf to target leaf.
   * @param source the leaf to copy data from.
   * @param target the leaf to copy data into.
   */
  private void copyLeafData(LeafNode<K, V> source, LeafNode<K, V> target) {
    target.clear();
    K[] sourceKeys = source.getKeys();
    V[] sourceValues = source.getValues();

    for (int i = 0; i < source.getKeyCount(); i++) {
      try {
        target.insertUnique(sourceKeys[i], sourceValues[i]);
      } catch (IllegalStateException ignored) {
        // Should not happen during copy
      }
    }
  }

  /**
   * Handles the insertion of a promoted key from a child node split by either creating a new
   * root (if inserting at tree root) or recursively inserting into the existing parent node,
   * triggering additional splits up the tree as needed to maintain B+ tree balance.
   * @param leftNode the original node which WAS split.
   * @param rightNode the newly created node as a result of splitting the {@code leftNode}.
   * @param promotedKey the key that gets pushed up from the split to separate the
   *        {@code leftNode} and {@code rightNode}.
   */
  private void insertIntoParent(Node<K, V> leftNode, Node<K, V> rightNode, K promotedKey) {
    if (leftNode.getParent() == null) {
      InternalNodeImpl<K, V> newRoot = new InternalNodeImpl<>(leftNode);
      newRoot.insertKeyAndChild(promotedKey, rightNode);
      leftNode.setParent(newRoot);
      rightNode.setParent(newRoot);
      this.root = newRoot;
      return;
    }

    InternalNode<K, V> parent = (InternalNode<K, V>) leftNode.getParent();
    rightNode.setParent(parent);

    if (!parent.isFull()) {
      parent.insertKeyAndChild(promotedKey, rightNode);
    } else {
      handleInternalOverflow(parent, promotedKey, rightNode);
    }
  }

  /**
   * Handles overflow in internal nodes by creating a temporary node with the new entry,
   * splitting it, and recursively inserting the promoted key into the parent hierarchy.
   * This maintains B+ tree balance when an internal node exceeds its maximum capacity.
   *
   * @param fullParent the internal node that has reached maximum capacity and needs splitting.
   * @param key the new key that caused the overflow and needs to be inserted.
   * @param rightChild the child node that should be positioned to the right of the new key.
   */
  private void handleInternalOverflow(InternalNode<K, V> fullParent, K key, Node<K, V> rightChild) {
    InternalNodeImpl<K, V> tempInternal = createTempInternalWithNewEntry(
        fullParent,
        key,
        rightChild
    );

    Node.SplitResult<K, V> splitResult = tempInternal.split();
    InternalNode<K, V> newRightInternal = (InternalNode<K, V>) splitResult.newNode();
    K promotedKey = splitResult.promotedKey();

    copyInternalData(tempInternal, fullParent);

    insertIntoParent(fullParent, newRightInternal, promotedKey);
  }

  /**
   * Creates a temporary internal node containing all existing key-child pairs from the
   * original node plus the new key-child pair, maintaining proper insertion order.
   * This temporary node is used for splitting operations when an internal node overflows.
   *
   * @param originalInternal the full internal node whose contents need to be copied.
   * @param key the new key that caused the overflow and needs to be inserted.
   * @param rightChild the child node that should be positioned to the right of the new key.
   * @return a new temporary internal node with all entries in sorted order.
   */
  private InternalNodeImpl<K, V> createTempInternalWithNewEntry(
      InternalNode<K, V> originalInternal,
      K key,
      Node<K, V> rightChild
  ) {
      K[] originalKeys = originalInternal.getKeys();
      Node<K, V>[] originalChildren = originalInternal.getChildren();

      InternalNodeImpl<K, V> tempInternal = new InternalNodeImpl<>(originalChildren[0]);

      for (int i = 0; i < originalInternal.getKeyCount(); i++) {
        tempInternal.insertKeyAndChild(originalKeys[i], originalChildren[i + 1]);
      }

      tempInternal.insertKeyAndChild(key, rightChild);
      return tempInternal;
  }

  /**
   * Transfers all keys and child pointers from a source internal node to a target internal node,
   * clearing the target first and reconstructing its structure to match the source exactly.
   * Used during split operations to copy the left portion back to the original node.
   *
   * @param source the internal node containing the data to be copied.
   * @param target the internal node that will receive the copied data (cleared first).
   */
  private void copyInternalData(InternalNode<K, V> source, InternalNode<K, V> target) {
    target.clear();
    K[] sourceKeys = source.getKeys();
    Node<K, V>[] sourceChildren = source.getChildren();

    target.setChild(0, sourceChildren[0]);

    for (int i = 0; i < source.getKeyCount(); i++) {
      target.insertKeyAndChild(sourceKeys[i], sourceChildren[i + 1]);
    }
  }

  @Override
  public V delete(K key) throws IllegalArgumentException {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }

    if (!contains(key)) {
      throw new IllegalArgumentException("Key not found: " + key);
    }

    LeafNode<K, V> targetLeaf = findLeaf(key);
    V deletedValue = targetLeaf.deleteKey(key);

    if (targetLeaf.isUnderflow() && targetLeaf != root) {
      handleLeafUnderflow(targetLeaf);
    }

    return deletedValue;
  }

  /**
   * Restores balance to an under-flowing leaf node by attempting to borrow keys from
   * siblings or merging with adjacent nodes when borrowing is not possible. Prioritizes
   * borrowing to maintain tree structure before resorting to merging operations.
   *
   * @param underflowLeaf the leaf node that has fallen below minimum key requirements
   *                      and needs re-balancing to maintain B+ tree properties.
   */
  private void handleLeafUnderflow(LeafNode<K, V> underflowLeaf) {
    Node<K, V> leftSibling = underflowLeaf.getLeftSibling();
    Node<K, V> rightSibling = underflowLeaf.getRightSibling();

    // Try borrowing from right sibling first
    if (rightSibling != null
        && rightSibling.isLeaf()
        && rightSibling.getKeyCount() > MIN_KEYS
    ) {
      K newSeparator = underflowLeaf.borrowFromRight(rightSibling);
      if (newSeparator != null) {
        updateParentSeparator(underflowLeaf, newSeparator);
        return;
      }
    }

    // Try borrowing from left sibling
    if (leftSibling != null
        && leftSibling.isLeaf()
        && leftSibling.getKeyCount() > MIN_KEYS
    ) {
      K newSeparator = underflowLeaf.borrowFromLeft(leftSibling);
      if (newSeparator != null) {
        updateParentSeparator(underflowLeaf, newSeparator);
        return;
      }
    }

    // Must merge
    if (leftSibling != null
        && leftSibling.isLeaf()
        && underflowLeaf.canMergeWith(leftSibling)
    ) {
      leftSibling.mergeWith(underflowLeaf, null);
      deleteFromParent(underflowLeaf);
    } else if (rightSibling != null
        && rightSibling.isLeaf()
        && underflowLeaf.canMergeWith(rightSibling)
    ) {
      underflowLeaf.mergeWith(rightSibling, null);
      deleteFromParent(rightSibling);
    }
  }

  /**
   * Updates the separator key in the parent node after a borrowing operation between
   * sibling nodes. The separator key serves as the boundary between child subtrees
   * and must be adjusted when keys are redistributed to maintain proper tree ordering.
   *
   * @param node the child node whose sibling borrowing operation requires a separator update.
   * @param newSeparator the new key value that should separate this node from its left sibling.
   */
  private void updateParentSeparator(Node<K, V> node, K newSeparator) {
    if (node.getParent() == null) return;

    int index = node.getIndexInParent();
    if (index > 0) {
      InternalNode<K, V> parent = (InternalNode<K, V>) node.getParent();
      parent.getKeys()[index - 1] = newSeparator;
    }
  }

  /**
   * Removes a node reference and its associated separator key from the parent node,
   * maintaining proper B+ tree structure by shifting remaining keys and children.
   * Handles special cases for root deletion and triggers re-balancing when necessary.
   *
   * @param nodeToDelete the child node to be removed from its parent's children array.
   */
  private void deleteFromParent(Node<K, V> nodeToDelete) {
    if (nodeToDelete.getParent() == null) {
      this.root = new LeafNodeImpl<>();
      return;
    }

    InternalNode<K, V> parent = (InternalNode<K, V>) nodeToDelete.getParent();
    int indexToDelete = nodeToDelete.getIndexInParent();

    K[] parentKeys = parent.getKeys();
    Node<K, V>[] parentChildren = parent.getChildren();

    if (indexToDelete > 0) {
      for (int i = indexToDelete - 1; i < parent.getKeyCount() - 1; i++) {
        parentKeys[i] = parentKeys[i + 1];
      }
    } else {
      for (int i = 0; i < parent.getKeyCount() - 1; i++) {
        parentKeys[i] = parentKeys[i + 1];
      }
    }
    parentKeys[parent.getKeyCount() - 1] = null;

    for (int i = indexToDelete; i < parent.getKeyCount(); i++) {
      parentChildren[i] = parentChildren[i + 1];
    }
    parentChildren[parent.getKeyCount()] = null;

    ((NodeImpl<K, V>) parent).keyCount--;

    if (parent.isUnderflow() && parent != root) {
      handleInternalUnderflow(parent);
    } else if (parent == root && parent.getKeyCount() == 0) {
      this.root = parentChildren[0];
      if (this.root != null) {
        this.root.setParent(null);
      }
    }
  }

  /**
   * Restores balance to an underflowing internal node by attempting to borrow keys from
   * sibling nodes or merging with adjacent nodes when borrowing is not possible. Uses
   * separator keys from the parent during merge operations to maintain proper tree structure.
   *
   * @param underflowInternal the internal node that has fallen below minimum key requirements
   *                          and needs rebalancing to maintain B+ tree properties
   */
  private void handleInternalUnderflow(InternalNode<K, V> underflowInternal) {
    Node<K, V> leftSibling = underflowInternal.getLeftSibling();
    Node<K, V> rightSibling = underflowInternal.getRightSibling();

    // Try borrowing from right sibling first
    if (rightSibling != null && !rightSibling.isLeaf() && rightSibling.getKeyCount() > MIN_KEYS) {
      K newSeparator = underflowInternal.borrowFromRight(rightSibling);
      if (newSeparator != null) {
        updateParentSeparator(underflowInternal, newSeparator);
        return;
      }
    }

    // Try borrowing from left sibling
    if (leftSibling != null && !leftSibling.isLeaf() && leftSibling.getKeyCount() > MIN_KEYS) {
      K newSeparator = underflowInternal.borrowFromLeft(leftSibling);
      if (newSeparator != null) {
        updateParentSeparator(underflowInternal, newSeparator);
        return;
      }
    }

    // Must merge
    if (leftSibling != null
        && !leftSibling.isLeaf()
        && underflowInternal.canMergeWith(leftSibling)
    ) {
      K separatorKey = getSeparatorKeyFromParent(leftSibling, underflowInternal);
      leftSibling.mergeWith(underflowInternal, separatorKey);
      deleteFromParent(underflowInternal);
    } else if (rightSibling != null
        && !rightSibling.isLeaf()
        && underflowInternal.canMergeWith(rightSibling)
    ) {
      K separatorKey = getSeparatorKeyFromParent(underflowInternal, rightSibling);
      underflowInternal.mergeWith(rightSibling, separatorKey);
      deleteFromParent(rightSibling);
    }
  }

  /**
   * Gets the separator key between two sibling nodes from their parent.
   */
  private K getSeparatorKeyFromParent(Node<K, V> leftChild, Node<K, V> rightChild) {
    if (leftChild.getParent() == null) return null;

    InternalNode<K, V> parent = (InternalNode<K, V>) leftChild.getParent();
    int leftIndex = leftChild.getIndexInParent();

    if (leftIndex >= 0 && leftIndex < parent.getKeyCount()) {
      return parent.getKeys()[leftIndex];
    }
    return null;
  }

  @Override
  public void clear() {
    this.root.clear();
    this.root = new LeafNodeImpl<>();
  }

  @Override
  public V get(K key) {
    if (key == null) {
      return null;
    }
    return this.root.get(key);
  }

  @Override
  public K getMinKey() {
    return this.root.getMinKey();
  }

  @Override
  public K getMaxKey() {
    return this.root.getMaxKey();
  }

  @Override
  public int height() {
    return this.root.height();
  }

  @Override
  public int size() {
    return this.root.size();
  }

  @Override
  public Map<K, V> rangeQuery(K startKey, K endKey) throws IllegalArgumentException {
    if (startKey == null || endKey == null) {
      throw new IllegalArgumentException("Start key and end key cannot be null");
    }

    if (startKey.compareTo(endKey) > 0) {
      throw new IllegalArgumentException("Start key cannot be greater than end key");
    }

    Map<K, V> result = new LinkedHashMap<>();

    LeafNode<K, V> currentLeaf = findLeaf(startKey);

    while (currentLeaf != null) {
      K[] keys = currentLeaf.getKeys();
      V[] values = currentLeaf.getValues();

      for (int i = 0; i < currentLeaf.getKeyCount(); i++) {
        K key = keys[i];

        if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
          result.put(key, values[i]);
        } else if (key.compareTo(endKey) > 0) {
          return result;
        }
      }

      currentLeaf = currentLeaf.getNext();
    }

    return result;
  }

  @Override
  public Iterator<V> iterator() {
    return new ValueIterator();
  }

  @Override
  public Iterator<K> keyIterator() {
    return new KeyIterator();
  }

  @Override
  public Iterator<Map.Entry<K, V>> entryIterator() {
    return new EntryIterator();
  }

  @Override
  public Iterator<V> rangeIterator(K startKey, K endKey) throws IllegalArgumentException {
    if (startKey == null || endKey == null) {
      throw new IllegalArgumentException("Start key and end key cannot be null");
    }

    if (startKey.compareTo(endKey) > 0) {
      throw new IllegalArgumentException("Start key cannot be greater than end key");
    }

    return new RangeIterator(startKey, endKey);
  }

  @Override
  public boolean contains(K key) {
    return this.root.contains(key);
  }

  /**
   * Finds the leaf node that should contain the given key.
   * @param key the target key.
   * @return the respective leaf node which should contain the given key.
   */
  private LeafNode<K, V> findLeaf(K key) {
    Node<K, V> current = root;

    while (!current.isLeaf()) {
      InternalNode<K, V> internal = (InternalNode<K, V>) current;
      K[] keys = internal.getKeys();
      Node<K, V>[] children = internal.getChildren();

      int childIndex = 0;
      for (int i = 0; i < internal.getKeyCount(); i++) {
        if (key.compareTo(keys[i]) < 0) {
          break;
        }
        childIndex = i + 1;
      }

      current = children[childIndex];
    }

    return (LeafNode<K, V>) current;
  }

  /**
   * Iterator for values in the tree.
   */
  private class ValueIterator implements Iterator<V> {
    private LeafNode<K, V> currentLeaf;
    private int currentIndex;

    public ValueIterator() {
      currentLeaf = findFirstLeaf();
      currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
      return currentLeaf != null && (currentIndex < currentLeaf.getKeyCount() || currentLeaf.getNext() != null);
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      if (currentIndex >= currentLeaf.getKeyCount()) {
        currentLeaf = currentLeaf.getNext();
        currentIndex = 0;
      }

      return currentLeaf.getValues()[currentIndex++];
    }
  }

  /**
   * Iterator for keys in the tree.
   */
  private class KeyIterator implements Iterator<K> {
    private LeafNode<K, V> currentLeaf;
    private int currentIndex;

    public KeyIterator() {
      currentLeaf = findFirstLeaf();
      currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
      return currentLeaf != null && (currentIndex < currentLeaf.getKeyCount() || currentLeaf.getNext() != null);
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      if (currentIndex >= currentLeaf.getKeyCount()) {
        currentLeaf = currentLeaf.getNext();
        currentIndex = 0;
      }

      return currentLeaf.getKeys()[currentIndex++];
    }
  }

  /**
   * Iterator for entries in the tree.
   */
  private class EntryIterator implements Iterator<Map.Entry<K, V>> {
    private LeafNode<K, V> currentLeaf;
    private int currentIndex;

    public EntryIterator() {
      currentLeaf = findFirstLeaf();
      currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
      return currentLeaf != null && (currentIndex < currentLeaf.getKeyCount() || currentLeaf.getNext() != null);
    }

    @Override
    public Map.Entry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      if (currentIndex >= currentLeaf.getKeyCount()) {
        currentLeaf = currentLeaf.getNext();
        currentIndex = 0;
      }

      K key = currentLeaf.getKeys()[currentIndex];
      V value = currentLeaf.getValues()[currentIndex];
      currentIndex++;

      return Map.entry(key, value);
    }
  }

  /**
   * Range iterator for values within a specific key range.
   */
  private class RangeIterator implements Iterator<V> {
    private LeafNode<K, V> currentLeaf;
    private int currentIndex;
    private final K endKey;

    public RangeIterator(K startKey, K endKey) {
      this.endKey = endKey;
      this.currentLeaf = findLeaf(startKey);
      this.currentIndex = 0;

      // Find the starting position within the leaf
      if (currentLeaf != null) {
        K[] keys = currentLeaf.getKeys();
        for (int i = 0; i < currentLeaf.getKeyCount(); i++) {
          if (keys[i].compareTo(startKey) >= 0) {
            currentIndex = i;
            break;
          }
        }
      }
    }

    @Override
    public boolean hasNext() {
      while (currentLeaf != null) {
        if (currentIndex < currentLeaf.getKeyCount()) {
          K currentKey = currentLeaf.getKeys()[currentIndex];
          return currentKey.compareTo(endKey) <= 0;
        } else {
          currentLeaf = currentLeaf.getNext();
          currentIndex = 0;
        }
      }
      return false;
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      return currentLeaf.getValues()[currentIndex++];
    }
  }

  /**
   * Finds the first leaf within this BPlusTree.
   * @return the first leaf.
   */
  private LeafNode<K, V> findFirstLeaf() {
    if (root == null || root.size() == 0) {
      return null;
    }

    Node<K, V> current = root;
    while (!current.isLeaf()) {
      InternalNode<K, V> internal = (InternalNode<K, V>) current;
      current = internal.getChildren()[0];
    }

    return (LeafNode<K, V>) current;
  }
}