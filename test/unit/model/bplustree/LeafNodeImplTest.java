package unit.model.bplustree;

import model.bplustree.Node;
import model.bplustree.InternalNode;
import model.bplustree.InternalNodeImpl;
import model.bplustree.LeafNode;
import model.bplustree.LeafNodeImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprehensive test suite for LeafNodeImpl that verifies B+ tree invariants.
 */
public class LeafNodeImplTest {

  private LeafNode<Integer, String> leafNode;
  private LeafNode<Integer, String> emptyLeaf;

  @BeforeEach
  void setUp() {
    leafNode = new LeafNodeImpl<>();
    emptyLeaf = new LeafNodeImpl<>();
  }

  @Test
  void newLeafNodeIsEmptyAndProperlyInitialized() {
    assertTrue(leafNode.isLeaf());
    assertEquals(0, leafNode.size());
    assertEquals(0, leafNode.getKeyCount());
    assertNull(leafNode.getNext());
    assertNull(leafNode.getPrev());
    assertNull(leafNode.getParent());
    assertFalse(leafNode.isFull());
    assertTrue(leafNode.isUnderflow());
  }

  @Test
  void leafNodeWithNextPointerLinksCorrectly() {
    LeafNode<Integer, String> nextLeaf = new LeafNodeImpl<>();
    LeafNode<Integer, String> leafWithNext = new LeafNodeImpl<>(nextLeaf);

    assertEquals(nextLeaf, leafWithNext.getNext());
    assertEquals(leafWithNext, nextLeaf.getPrev());
  }

  @Test
  void insertUniqueKeyValuePairsWorksCorrectly() {
    assertTrue(leafNode.insertUnique(10, "ten"));
    assertEquals(1, leafNode.size());
    assertEquals("ten", leafNode.get(10));
    assertTrue(leafNode.contains(10));

    assertTrue(leafNode.insertUnique(5, "five"));
    assertTrue(leafNode.insertUnique(15, "fifteen"));
    assertEquals(3, leafNode.size());

    assertEquals(Integer.valueOf(5), leafNode.getMinKey());
    assertEquals(Integer.valueOf(15), leafNode.getMaxKey());
  }

  @Test
  void insertNullKeyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> leafNode.insertUnique(null, "value"));
  }

  @Test
  void insertDuplicateKeyUpdatesValue() {
    leafNode.insertUnique(10, "ten");
    assertFalse(leafNode.insertUnique(10, "updated_ten"));
    assertEquals(1, leafNode.size());
    assertEquals("updated_ten", leafNode.get(10));
  }

  @Test
  void insertIntoFullNodeThrowsException() {
    leafNode.insertUnique(1, "one");
    leafNode.insertUnique(2, "two");
    leafNode.insertUnique(3, "three");

    assertTrue(leafNode.isFull());
    assertThrows(IllegalStateException.class, () -> leafNode.insertUnique(4, "four"));
  }

  @Test
  void insertMaintainsSortedOrder() {
    leafNode.insertUnique(30, "thirty");
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");

    Comparable<Integer>[] keys = leafNode.getKeys();
    assertEquals(10, keys[0]);
    assertEquals(20, keys[1]);
    assertEquals(30, keys[2]);
  }

  @Test
  void deleteExistingKeyWorksCorrectly() {
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(30, "thirty");

    String deleted = leafNode.deleteKey(20);
    assertEquals("twenty", deleted);
    assertEquals(2, leafNode.size());
    assertNull(leafNode.get(20));
    assertFalse(leafNode.contains(20));

    assertEquals("ten", leafNode.get(10));
    assertEquals("thirty", leafNode.get(30));
  }

  @Test
  void deleteNonExistingKeyThrowsException() {
    leafNode.insertUnique(10, "ten");
    assertThrows(IllegalArgumentException.class, () -> leafNode.deleteKey(20));
  }

  @Test
  void deleteFromEmptyNodeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> emptyLeaf.deleteKey(10));
  }

  @Test
  void deleteOnlyKeyLeavesEmptyNode() {
    leafNode.insertUnique(10, "ten");
    leafNode.deleteKey(10);

    assertEquals(0, leafNode.size());
    assertNull(leafNode.get(10));
    assertTrue(leafNode.isUnderflow());
  }

  @Test
  void splitCreatesBalancedNodes() {
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(30, "thirty");

    Node.SplitResult<Integer, String> result = leafNode.split();
    LeafNode<Integer, String> newLeaf = (LeafNode<Integer, String>) result.newNode();
    Integer promotedKey = result.promotedKey();

    assertNotNull(promotedKey);

    int totalOriginalSize = 3;
    int leftSize = leafNode.size();
    int rightSize = newLeaf.size();
    assertEquals(totalOriginalSize, leftSize + rightSize);
    assertTrue(Math.abs(leftSize - rightSize) <= 1);

    assertEquals(newLeaf, leafNode.getNext());
    assertEquals(leafNode, newLeaf.getPrev());

    assertTrue(leafNode.getMaxKey().compareTo(newLeaf.getMinKey()) <= 0);
  }

  @Test
  void splitMaintainsParentRelationships() {
    InternalNode<Integer, String> parent = new InternalNodeImpl<>();
    leafNode.setParent(parent);

    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(30, "thirty");

    Node.SplitResult<Integer, String> result = leafNode.split();
    LeafNode<Integer, String> newLeaf = (LeafNode<Integer, String>) result.newNode();

    assertEquals(parent, newLeaf.getParent());
  }

  @Test
  void canMergeWithAdjacentCompatibleLeafNode() {
    LeafNode<Integer, String> otherLeaf = new LeafNodeImpl<>();

    leafNode.insertUnique(10, "ten");
    otherLeaf.insertUnique(20, "twenty");

    leafNode.setNext(otherLeaf);
    otherLeaf.setPrev(leafNode);

    assertTrue(leafNode.canMergeWith(otherLeaf));

    leafNode.insertUnique(15, "fifteen");
    otherLeaf.insertUnique(25, "twenty-five");
    otherLeaf.insertUnique(30, "thirty");

    assertFalse(leafNode.canMergeWith(otherLeaf));
  }

  @Test
  void cannotMergeWithNonAdjacentNode() {
    LeafNode<Integer, String> otherLeaf = new LeafNodeImpl<>();

    leafNode.insertUnique(10, "ten");
    otherLeaf.insertUnique(20, "twenty");

    assertFalse(leafNode.canMergeWith(otherLeaf));
  }

  @Test
  void cannotMergeWithInternalNode() {
    InternalNode<Integer, String> internalNode = new InternalNodeImpl<>();
    assertFalse(leafNode.canMergeWith(internalNode));
  }

  @Test
  void cannotMergeWithNull() {
    assertFalse(leafNode.canMergeWith(null));
  }

  @Test
  void mergeOperationCombinesNodesCorrectly() {
    LeafNode<Integer, String> otherLeaf = new LeafNodeImpl<>();

    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(15, "fifteen");
    otherLeaf.insertUnique(20, "twenty");

    leafNode.setNext(otherLeaf);
    otherLeaf.setPrev(leafNode);

    leafNode.mergeWith(otherLeaf, null);

    assertEquals(3, leafNode.size());
    assertEquals("ten", leafNode.get(10));
    assertEquals("fifteen", leafNode.get(15));
    assertEquals("twenty", leafNode.get(20));

    assertEquals(0, otherLeaf.size());
    assertNull(leafNode.getNext());
  }

  @Test
  void mergeIncompatibleNodesThrowsException() {
    LeafNode<Integer, String> otherLeaf = new LeafNodeImpl<>();

    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(15, "fifteen");
    leafNode.insertUnique(20, "twenty");

    otherLeaf.insertUnique(25, "twenty-five");
    otherLeaf.insertUnique(30, "thirty");

    leafNode.setNext(otherLeaf);
    otherLeaf.setPrev(leafNode);

    assertThrows(IllegalArgumentException.class,
        () -> leafNode.mergeWith(otherLeaf, null));
  }

  @Test
  void borrowFromLeftSiblingWorksCorrectly() {
    LeafNode<Integer, String> leftSibling = new LeafNodeImpl<>();

    leftSibling.insertUnique(5, "five");
    leftSibling.insertUnique(10, "ten");
    leftSibling.insertUnique(15, "fifteen");

    leafNode.insertUnique(20, "twenty");

    Integer newSeparator = leafNode.borrowFromLeft(leftSibling);

    assertEquals(Integer.valueOf(15), newSeparator);
    assertEquals(2, leafNode.size());
    assertEquals(2, leftSibling.size());
    assertEquals("fifteen", leafNode.get(15));
    assertEquals("twenty", leafNode.get(20));
    assertNull(leftSibling.get(15));

    assertEquals(Integer.valueOf(15), leafNode.getMinKey());
  }

  @Test
  void borrowFromRightSiblingWorksCorrectly() {
    LeafNode<Integer, String> rightSibling = new LeafNodeImpl<>();

    rightSibling.insertUnique(25, "twenty-five");
    rightSibling.insertUnique(30, "thirty");
    rightSibling.insertUnique(35, "thirty-five");

    leafNode.insertUnique(20, "twenty");

    Integer newSeparator = leafNode.borrowFromRight(rightSibling);

    assertEquals(Integer.valueOf(30), newSeparator);
    assertEquals(2, leafNode.size());
    assertEquals(2, rightSibling.size());
    assertEquals("twenty", leafNode.get(20));
    assertEquals("twenty-five", leafNode.get(25));
    assertNull(rightSibling.get(25));

    assertEquals(Integer.valueOf(30), rightSibling.getMinKey());
  }

  @Test
  void cannotBorrowFromSiblingWithMinimumKeys() {
    LeafNode<Integer, String> sibling = new LeafNodeImpl<>();
    sibling.insertUnique(10, "ten");

    assertNull(leafNode.borrowFromLeft(sibling));
    assertNull(leafNode.borrowFromRight(sibling));
  }

  @Test
  void cannotBorrowFromInternalNode() {
    InternalNode<Integer, String> internalNode = new InternalNodeImpl<>();

    assertNull(leafNode.borrowFromLeft(internalNode));
    assertNull(leafNode.borrowFromRight(internalNode));
  }

  @Test
  void cannotBorrowFromNull() {
    assertNull(leafNode.borrowFromLeft(null));
    assertNull(leafNode.borrowFromRight(null));
  }

  @Test
  void getOperationsWorkCorrectly() {
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(30, "thirty");
    leafNode.insertUnique(20, "twenty");

    assertEquals("ten", leafNode.get(10));
    assertEquals("twenty", leafNode.get(20));
    assertEquals("thirty", leafNode.get(30));
    assertNull(leafNode.get(40));
  }

  @Test
  void getWithNullKeyReturnsNull() {
    leafNode.insertUnique(10, "ten");
    assertNull(leafNode.get(null));
  }

  @Test
  void minAndMaxKeyOperations() {
    assertNull(emptyLeaf.getMinKey());
    assertNull(emptyLeaf.getMaxKey());

    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(30, "thirty");

    assertEquals(Integer.valueOf(10), leafNode.getMinKey());
    assertEquals(Integer.valueOf(30), leafNode.getMaxKey());
  }

  @Test
  void heightIsAlwaysOneForLeafNodes() {
    assertEquals(1, emptyLeaf.height());

    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");
    assertEquals(1, leafNode.height());
  }

  @Test
  void sizeReflectsNumberOfKeyValuePairs() {
    assertEquals(0, emptyLeaf.size());

    leafNode.insertUnique(10, "ten");
    assertEquals(1, leafNode.size());

    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(30, "thirty");
    assertEquals(3, leafNode.size());

    leafNode.deleteKey(20);
    assertEquals(2, leafNode.size());
  }

  @Test
  void containsWorksCorrectly() {
    assertFalse(emptyLeaf.contains(10));

    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");

    assertTrue(leafNode.contains(10));
    assertTrue(leafNode.contains(20));
    assertFalse(leafNode.contains(30));
  }

  @Test
  void clearResetsNodeToEmptyState() {
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");
    leafNode.setNext(new LeafNodeImpl<>());
    leafNode.setPrev(new LeafNodeImpl<>());
    leafNode.setParent(new InternalNodeImpl<>());

    leafNode.clear();

    assertEquals(0, leafNode.size());
    assertEquals(0, leafNode.getKeyCount());
    assertNull(leafNode.getNext());
    assertNull(leafNode.getPrev());
    assertNull(leafNode.getParent());
    assertNull(leafNode.get(10));
    assertFalse(leafNode.contains(10));
  }

  @Test
  void underflowAndFullConditions() {
    assertTrue(emptyLeaf.isUnderflow());
    assertFalse(emptyLeaf.isFull());

    leafNode.insertUnique(10, "ten");
    assertFalse(leafNode.isUnderflow());
    assertFalse(leafNode.isFull());

    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(30, "thirty");

    assertFalse(leafNode.isUnderflow());
    assertTrue(leafNode.isFull());
  }

  @Test
  void operationsOnUnsupportedMethodsThrowExceptions() {
    assertThrows(UnsupportedOperationException.class,
        () -> leafNode.insertKeyAndChild(10, new LeafNodeImpl<>())
    );
  }

  @Test
  void linkedListOperationsWorkCorrectly() {
    LeafNode<Integer, String> middle = new LeafNodeImpl<>();
    LeafNode<Integer, String> right = new LeafNodeImpl<>();

    leafNode.setNext(middle);
    middle.setPrev(leafNode);
    middle.setNext(right);
    right.setPrev(middle);

    assertEquals(middle, leafNode.getNext());
    assertEquals(leafNode, middle.getPrev());
    assertEquals(right, middle.getNext());
    assertEquals(middle, right.getPrev());
    assertNull(leafNode.getPrev());
    assertNull(right.getNext());
  }

  @Test
  void valuesArrayAccessWorksCorrectly() {
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");

    Object[] values = leafNode.getValues();
    assertNotNull(values);
    assertEquals("ten", values[0]);
    assertEquals("twenty", values[1]);
    assertEquals(2, values.length);
  }

  @Test
  void leafSpecificMethodsWorkCorrectly() {
    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");

    LeafNode<Integer, String> nextLeaf = new LeafNodeImpl<>();
    nextLeaf.insertUnique(30, "thirty");

    leafNode.setNext(nextLeaf);
    nextLeaf.setPrev(leafNode);

    assertEquals(nextLeaf, leafNode.getNext());
    assertEquals(leafNode, nextLeaf.getPrev());

    Object[] values = leafNode.getValues();
    assertEquals("ten", values[0]);
    assertEquals("twenty", values[1]);
  }

  @Test
  void chainedLeafOperations() {
    LeafNode<Integer, String> leaf1 = new LeafNodeImpl<>();
    LeafNode<Integer, String> leaf2 = new LeafNodeImpl<>();
    LeafNode<Integer, String> leaf3 = new LeafNodeImpl<>();

    leaf1.insertUnique(5, "five");
    leaf2.insertUnique(15, "fifteen");
    leaf3.insertUnique(25, "twenty-five");

    leaf1.setNext(leaf2);
    leaf2.setPrev(leaf1);
    leaf2.setNext(leaf3);
    leaf3.setPrev(leaf2);

    assertEquals(leaf2, leaf1.getNext());
    assertEquals(leaf3, leaf2.getNext());
    assertNull(leaf3.getNext());
    assertNull(leaf1.getPrev());
  }

  @Test
  void sequentialAccessThroughLinkedList() {
    LeafNode<Integer, String> leaf1 = new LeafNodeImpl<>();
    LeafNode<Integer, String> leaf2 = new LeafNodeImpl<>();
    LeafNode<Integer, String> leaf3 = new LeafNodeImpl<>();

    leaf1.insertUnique(5, "five");
    leaf1.insertUnique(10, "ten");

    leaf2.insertUnique(15, "fifteen");
    leaf2.insertUnique(20, "twenty");

    leaf3.insertUnique(25, "twenty-five");
    leaf3.insertUnique(30, "thirty");

    leaf1.setNext(leaf2);
    leaf2.setPrev(leaf1);
    leaf2.setNext(leaf3);
    leaf3.setPrev(leaf2);

    LeafNode<Integer, String> current = leaf1;
    int totalValues = 0;
    while (current != null) {
      totalValues += current.size();
      current = current.getNext();
    }

    assertEquals(6, totalValues);
  }

  @Test
  void splitPreservesLinkedListChain() {
    LeafNode<Integer, String> nextLeaf = new LeafNodeImpl<>();
    nextLeaf.insertUnique(40, "forty");

    leafNode.insertUnique(10, "ten");
    leafNode.insertUnique(20, "twenty");
    leafNode.insertUnique(30, "thirty");
    leafNode.setNext(nextLeaf);
    nextLeaf.setPrev(leafNode);

    Node.SplitResult<Integer, String> result = leafNode.split();
    LeafNode<Integer, String> newLeaf = (LeafNode<Integer, String>) result.newNode();

    assertEquals(newLeaf, leafNode.getNext());
    assertEquals(leafNode, newLeaf.getPrev());
    assertEquals(nextLeaf, newLeaf.getNext());
    assertEquals(newLeaf, nextLeaf.getPrev());
  }

  @Test
  void borrowingMaintainsSortedOrder() {
    LeafNode<Integer, String> leftSibling = new LeafNodeImpl<>();

    leftSibling.insertUnique(5, "five");
    leftSibling.insertUnique(10, "ten");
    leftSibling.insertUnique(15, "fifteen");

    leafNode.insertUnique(25, "twenty-five");

    leafNode.borrowFromLeft(leftSibling);

    Comparable<Integer>[] keys = leafNode.getKeys();
    assertEquals(15, keys[0]);
    assertEquals(25, keys[1]);

    Comparable<Integer>[] leftKeys = leftSibling.getKeys();
    assertEquals(5, leftKeys[0]);
    assertEquals(10, leftKeys[1]);
  }
}