package unit.model.bplustree;

import model.bplustree.Node;
import model.bplustree.InternalNode;
import model.bplustree.InternalNodeImpl;
import model.bplustree.LeafNode;
import model.bplustree.LeafNodeImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is a tester class for InternalNodeImpl objects.
 */
public class InternalNodeImplTest {

  private InternalNode<Integer, String> internalNode;
  private InternalNode<Integer, String> emptyInternal;
  private LeafNode<Integer, String> leftChild;
  private LeafNode<Integer, String> rightChild;
  private LeafNode<Integer, String> middleChild;

  @BeforeEach
  void setUp() {
    internalNode = new InternalNodeImpl<>();
    emptyInternal = new InternalNodeImpl<>();

    leftChild = new LeafNodeImpl<>();
    leftChild.insertUnique(5, "five");
    leftChild.insertUnique(10, "ten");

    rightChild = new LeafNodeImpl<>();
    rightChild.insertUnique(20, "twenty");
    rightChild.insertUnique(25, "twenty-five");

    middleChild = new LeafNodeImpl<>();
    middleChild.insertUnique(15, "fifteen");
  }

  @Test
  void newInternalNodeIsEmptyAndProperlyInitialized() {
    assertFalse(internalNode.isLeaf());
    assertEquals(0, internalNode.size());
    assertEquals(0, internalNode.getKeyCount());
    assertNull(internalNode.getParent());
    assertFalse(internalNode.isFull());
    assertTrue(internalNode.isUnderflow());
  }

  @Test
  void internalNodeWithFirstChildInitializesCorrectly() {
    InternalNode<Integer, String> nodeWithChild = new InternalNodeImpl<>(leftChild);

    assertEquals(leftChild, nodeWithChild.getChildren()[0]);
    assertEquals(nodeWithChild, leftChild.getParent());
    assertEquals(0, nodeWithChild.getKeyCount());
  }

  @Test
  void insertKeyAndChildWorksCorrectly() {
    internalNode.setChild(0, leftChild);

    internalNode.insertKeyAndChild(15, middleChild);
    Comparable<Integer>[] keys = internalNode.getKeys();

    assertEquals(1, internalNode.getKeyCount());
    assertEquals(15, keys[0]);
    assertEquals(leftChild, internalNode.getChildren()[0]);
    assertEquals(middleChild, internalNode.getChildren()[1]);
    assertEquals(internalNode, middleChild.getParent());
  }

  @Test
  void insertMultipleKeysMaintainsSortedOrder() {
    internalNode.setChild(0, leftChild);

    internalNode.insertKeyAndChild(20, rightChild);
    internalNode.insertKeyAndChild(15, middleChild);

    assertEquals(2, internalNode.getKeyCount());
    Comparable<Integer>[] keys = internalNode.getKeys();
    assertEquals(15, keys[0]);
    assertEquals(20, keys[1]);

    assertEquals(leftChild, internalNode.getChildren()[0]);
    assertEquals(middleChild, internalNode.getChildren()[1]);
    assertEquals(rightChild, internalNode.getChildren()[2]);
  }

  @Test
  void setChildEstablishesParentRelationship() {
    internalNode.setChild(0, leftChild);

    assertEquals(internalNode, leftChild.getParent());
    assertEquals(leftChild, internalNode.getChildren()[0]);
  }

  @Test
  void getDelegatesToAppropriateChild() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);
    internalNode.insertKeyAndChild(20, rightChild);

    assertEquals("five", internalNode.get(5));
    assertEquals("ten", internalNode.get(10));
    assertEquals("fifteen", internalNode.get(15));
    assertEquals("twenty", internalNode.get(20));
    assertEquals("twenty-five", internalNode.get(25));
    assertNull(internalNode.get(100));
  }

  @Test
  void containsDelegatesToAppropriateChild() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    assertTrue(internalNode.contains(5));
    assertTrue(internalNode.contains(15));
    assertFalse(internalNode.contains(100));
  }

  @Test
  void getMinKeyDelegatesToLeftmostChild() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    assertEquals(Integer.valueOf(5), internalNode.getMinKey());

    assertNull(emptyInternal.getMinKey());
  }

  @Test
  void getMaxKeyDelegatesToRightmostChild() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);
    internalNode.insertKeyAndChild(20, rightChild);

    assertEquals(Integer.valueOf(25), internalNode.getMaxKey());

    assertNull(emptyInternal.getMaxKey());
  }

  @Test
  void sizeSumsAllChildrenSizes() {
    assertEquals(0, emptyInternal.size());

    internalNode.setChild(0, leftChild);
    assertEquals(2, internalNode.size());

    internalNode.insertKeyAndChild(15, middleChild);
    assertEquals(3, internalNode.size());

    internalNode.insertKeyAndChild(20, rightChild);
    assertEquals(5, internalNode.size());
  }

  @Test
  void heightIsOnePlusMaxChildHeight() {
    assertEquals(1, emptyInternal.height());

    internalNode.setChild(0, leftChild);
    assertEquals(2, internalNode.height());

    InternalNode<Integer, String> childInternal = new InternalNodeImpl<>(leftChild);
    internalNode.setChild(0, childInternal);
    assertEquals(3, internalNode.height());
  }

  @Test
  void splitCreatesNewNodeAndPromotesMiddleKey() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);
    internalNode.insertKeyAndChild(20, rightChild);

    LeafNode<Integer, String> extraChild = new LeafNodeImpl<>();
    extraChild.insertUnique(30, "thirty");
    internalNode.insertKeyAndChild(25, extraChild);

    assertTrue(internalNode.isFull());

    Node.SplitResult<Integer, String> result = internalNode.split();
    InternalNode<Integer, String> newInternal = (InternalNode<Integer, String>) result.newNode();
    Integer promotedKey = result.promotedKey();

    assertEquals(20, promotedKey);

    Comparable<Integer>[] internalKeys = internalNode.getKeys();
    Comparable<Integer>[] newInternalKeys = newInternal.getKeys();
    assertEquals(1, internalNode.getKeyCount());
    assertEquals(15, internalKeys[0]);

    assertEquals(1, newInternal.getKeyCount());
    assertEquals(25, newInternalKeys[0]);

    for (int i = 0; i <= newInternal.getKeyCount(); i++) {
      if (newInternal.getChildren()[i] != null) {
        assertEquals(newInternal, newInternal.getChildren()[i].getParent());
      }
    }
  }

  @Test
  void splitMaintainsParentRelationships() {
    InternalNode<Integer, String> parent = new InternalNodeImpl<>();
    internalNode.setParent(parent);

    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);
    internalNode.insertKeyAndChild(20, rightChild);

    Node.SplitResult<Integer, String> result = internalNode.split();
    InternalNode<Integer, String> newInternal = (InternalNode<Integer, String>) result.newNode();

    assertEquals(parent, newInternal.getParent());
  }

  @Test
  void canMergeWithCompatibleInternalNode() {
    InternalNode<Integer, String> otherInternal = new InternalNodeImpl<>();

    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    otherInternal.setChild(0, rightChild);

    assertTrue(internalNode.canMergeWith(otherInternal));

    LeafNode<Integer, String> extraChild1 = new LeafNodeImpl<>();
    LeafNode<Integer, String> extraChild2 = new LeafNodeImpl<>();
    internalNode.insertKeyAndChild(25, extraChild1);
    otherInternal.insertKeyAndChild(35, extraChild2);

    assertFalse(internalNode.canMergeWith(otherInternal));
  }

  @Test
  void cannotMergeWithLeafNode() {
    assertFalse(internalNode.canMergeWith(leftChild));
  }

  @Test
  void mergeOperationCombinesNodesCorrectly() {
    InternalNode<Integer, String> otherInternal = new InternalNodeImpl<>();

    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    otherInternal.setChild(0, rightChild);

    int separatorKey = 18;
    internalNode.mergeWith(otherInternal, separatorKey);
    Comparable<Integer>[] internalKeys = internalNode.getKeys();

    assertEquals(2, internalNode.getKeyCount());
    assertEquals(15, internalKeys[0]);
    assertEquals(18, internalKeys[1]);

    assertEquals(leftChild, internalNode.getChildren()[0]);
    assertEquals(middleChild, internalNode.getChildren()[1]);
    assertEquals(rightChild, internalNode.getChildren()[2]);

    assertEquals(internalNode, rightChild.getParent());

    assertEquals(0, otherInternal.getKeyCount());
  }

  @Test
  void borrowFromLeftSiblingWorksCorrectly() {
    InternalNode<Integer, String> leftSibling = new InternalNodeImpl<>();

    leftSibling.setChild(0, leftChild);
    leftSibling.insertKeyAndChild(10, middleChild);
    leftSibling.insertKeyAndChild(15, rightChild);

    LeafNode<Integer, String> currentChild = new LeafNodeImpl<>();
    currentChild.insertUnique(25, "twenty-five");
    internalNode.setChild(0, currentChild);
    internalNode.insertKeyAndChild(30, new LeafNodeImpl<>());

    Integer borrowedKey = internalNode.borrowFromLeft(leftSibling);

    assertEquals(15, borrowedKey);
    assertEquals(2, internalNode.getKeyCount());
    assertEquals(1, leftSibling.getKeyCount());

    assertEquals(internalNode, internalNode.getChildren()[0].getParent());
  }

  @Test
  void borrowFromRightSiblingWorksCorrectly() {
    InternalNode<Integer, String> rightSibling = new InternalNodeImpl<>();

    rightSibling.setChild(0, middleChild);
    rightSibling.insertKeyAndChild(20, rightChild);
    LeafNode<Integer, String> extraChild = new LeafNodeImpl<>();
    extraChild.insertUnique(30, "thirty");
    rightSibling.insertKeyAndChild(25, extraChild);

    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(10, new LeafNodeImpl<>());

    Integer newSeparator = internalNode.borrowFromRight(rightSibling);

    assertNotNull(newSeparator);
    assertEquals(2, internalNode.getKeyCount());
    assertEquals(1, rightSibling.getKeyCount());

    assertEquals(internalNode, internalNode.getChildren()[2].getParent());
  }

  @Test
  void cannotBorrowFromSiblingWithMinimumKeys() {
    InternalNode<Integer, String> sibling = new InternalNodeImpl<>();
    sibling.setChild(0, leftChild);

    assertNull(internalNode.borrowFromLeft(sibling));
    assertNull(internalNode.borrowFromRight(sibling));
  }

  @Test
  void cannotBorrowFromLeafSibling() {
    assertNull(internalNode.borrowFromLeft(leftChild));
    assertNull(internalNode.borrowFromRight(leftChild));
  }

  @Test
  void clearResetsNodeAndRecursivelyClearsChildren() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);
    internalNode.setParent(new InternalNodeImpl<>());

    internalNode.clear();

    assertEquals(0, internalNode.getKeyCount());
    assertNull(internalNode.getParent());
    assertNull(internalNode.getChildren()[0]);
  }

  @Test
  void underflowAndFullConditions() {
    assertTrue(emptyInternal.isUnderflow());
    assertFalse(emptyInternal.isFull());

    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    assertFalse(internalNode.isUnderflow());
    assertFalse(internalNode.isFull());

    internalNode.insertKeyAndChild(20, rightChild);
    internalNode.insertKeyAndChild(25, new LeafNodeImpl<>());

    assertFalse(internalNode.isUnderflow());
    assertTrue(internalNode.isFull());
  }

  @Test
  void operationsWithNullChildrenHandleGracefully() {
    assertNull(emptyInternal.get(10));
    assertFalse(emptyInternal.contains(10));
    assertEquals(0, emptyInternal.size());
    assertEquals(1, emptyInternal.height());
  }

  @Test
  void parentChildRelationshipsAreMaintained() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    assertEquals(internalNode, leftChild.getParent());
    assertEquals(internalNode, middleChild.getParent());

    internalNode.setChild(0, null);
    assertNull(internalNode.getChildren()[0]);
  }

  @Test
  void arrayBoundsAreRespected() {
    internalNode.setChild(0, leftChild);

    for (int i = 1; i < 4; i++) {
      LeafNode<Integer, String> child = new LeafNodeImpl<>();
      child.insertUnique(i * 10, "value" + i);
      internalNode.insertKeyAndChild(i * 10, child);
    }

    assertEquals(3, internalNode.getKeyCount());
    assertTrue(internalNode.isFull());
  }

  @Test
  void getChildrenReturnsCorrectArray() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    Node<Integer, String>[] children = internalNode.getChildren();

    assertEquals(leftChild, children[0]);
    assertEquals(middleChild, children[1]);
    assertNull(children[2]);
  }

  @Test
  void complexNavigationScenario() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(12, middleChild);
    internalNode.insertKeyAndChild(18, rightChild);

    assertEquals("five", internalNode.get(5));
    assertEquals("ten", internalNode.get(10));
    assertEquals("fifteen", internalNode.get(15));
    assertEquals("twenty", internalNode.get(20));
    assertEquals("twenty-five", internalNode.get(25));
  }

  @Test
  void internalNodeCanHoldInternalChildren() {
    InternalNode<Integer, String> childInternal1 = new InternalNodeImpl<>();
    InternalNode<Integer, String> childInternal2 = new InternalNodeImpl<>();

    childInternal1.setChild(0, leftChild);
    childInternal2.setChild(0, rightChild);

    internalNode.setChild(0, childInternal1);
    internalNode.insertKeyAndChild(15, childInternal2);

    assertEquals(childInternal1, internalNode.getChildren()[0]);
    assertEquals(childInternal2, internalNode.getChildren()[1]);
    assertEquals(internalNode, childInternal1.getParent());
    assertEquals(internalNode, childInternal2.getParent());
  }

  @Test
  void internalNodeCanHoldMixedChildren() {
    InternalNode<Integer, String> childInternal = new InternalNodeImpl<>();
    childInternal.setChild(0, leftChild);

    internalNode.setChild(0, childInternal);
    internalNode.insertKeyAndChild(15, middleChild);
    internalNode.insertKeyAndChild(20, rightChild);

    assertEquals(childInternal, internalNode.getChildren()[0]);
    assertEquals(middleChild, internalNode.getChildren()[1]);
    assertEquals(rightChild, internalNode.getChildren()[2]);
  }

  @Test
  void internalSpecificMethodsWorkCorrectly() {
    internalNode.setChild(0, leftChild);
    internalNode.insertKeyAndChild(15, middleChild);

    Node<Integer, String>[] children = internalNode.getChildren();
    assertNotNull(children);
    assertEquals(leftChild, children[0]);
    assertEquals(middleChild, children[1]);

    LeafNode<Integer, String> newChild = new LeafNodeImpl<>();
    newChild.insertUnique(35, "thirty-five");
    internalNode.setChild(2, newChild);

    assertEquals(newChild, internalNode.getChildren()[2]);
    assertEquals(internalNode, newChild.getParent());
  }

  @Test
  void siblingRelationshipsWorkCorrectly() {
    InternalNode<Integer, String> parent = new InternalNodeImpl<>();
    InternalNode<Integer, String> sibling1 = new InternalNodeImpl<>();
    InternalNode<Integer, String> sibling2 = new InternalNodeImpl<>();

    parent.setChild(0, sibling1);
    parent.insertKeyAndChild(50, sibling2);

    sibling1.setParent(parent);
    sibling2.setParent(parent);

    assertEquals(sibling2, sibling1.getRightSibling());
    assertEquals(sibling1, sibling2.getLeftSibling());
    assertEquals(0, sibling1.getIndexInParent());
    assertEquals(1, sibling2.getIndexInParent());
  }
}