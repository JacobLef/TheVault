package unit.model.bplustree;

import model.bplustree.BPlusTree;
import model.bplustree.BPlusTreeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is a tester class for BPlusTreeImpl objects.
 */
public class BPlusTreeImplTest {
  private BPlusTree<Integer, String> tree;
  private BPlusTree<String, Double> bankingTree;

  @BeforeEach
  void setUp() {
    tree = new BPlusTreeImpl<>();
    bankingTree = new BPlusTreeImpl<>();
  }

  @Test
  void debugRangeQuery() {
    bankingTree.insert("ACC001", 1000.00);
    bankingTree.insert("ACC005", 2000.00);
    bankingTree.insert("ACC010", 3000.00);
    bankingTree.insert("ACC015", 4000.00);
    bankingTree.insert("ACC020", 5000.00);

    // Debug: Check if insertions worked
    System.out.println("Size: " + bankingTree.size());
    System.out.println("ACC005: " + bankingTree.get("ACC005"));
    System.out.println("ACC010: " + bankingTree.get("ACC010"));
    System.out.println("ACC015: " + bankingTree.get("ACC015"));

    // Debug: Check min/max
    System.out.println("Min key: " + bankingTree.getMinKey());
    System.out.println("Max key: " + bankingTree.getMaxKey());

    Map<String, Double> range = bankingTree.rangeQuery("ACC005", "ACC015");
    System.out.println("Range query result size: " + range.size());
    System.out.println("Range contents: " + range);
  }

  @Test
  void insertAndRetrieveNewKeyValuePair() {
    assertTrue(tree.insert(5, "five"));
    assertEquals("five", tree.get(5));
    assertEquals(1, tree.size());
  }

  @Test
  void insertDuplicateKeyUpdatesValue() {
    assertTrue(tree.insert(5, "five"));
    assertFalse(tree.insert(5, "FIVE"));
    assertEquals("FIVE", tree.get(5));
    assertEquals(1, tree.size());
  }

  @Test
  void insertNullKeyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
  }

  @Test
  void getNonExistentKeyReturnsNull() {
    assertNull(tree.get(999));
  }

  @Test
  void getNullKeyReturnsNull() {
    assertNull(tree.get(null));
  }

  @Test
  void deleteExistingKeyReturnsValue() {
    tree.insert(5, "five");
    assertEquals("five", tree.delete(5));
    assertNull(tree.get(5));
    assertEquals(0, tree.size());
  }

  @Test
  void deleteNonExistentKeyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.delete(999));
  }

  @Test
  void deleteNullKeyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.delete(null));
  }

  @Test
  void containsReturnsTrueForExistingKeys() {
    assertFalse(tree.contains(5));
    tree.insert(5, "five");
    assertTrue(tree.contains(5));
    tree.delete(5);
    assertFalse(tree.contains(5));
  }

  @Test
  void containsNullReturnsFalse() {
    assertFalse(tree.contains(null));
  }

  @Test
  void emptyTreeReturnsZeroSize() {
    assertEquals(0, tree.size());
  }

  @Test
  void sizeTracksInsertionsAndDeletions() {
    tree.insert(1, "one");
    tree.insert(2, "two");
    tree.insert(3, "three");
    assertEquals(3, tree.size());

    tree.delete(2);
    assertEquals(2, tree.size());
  }

  @Test
  void emptyTreeHasHeightOfOne() {
    assertEquals(1, tree.height());
  }

  @Test
  void heightIncreasesWithLargeDatasets() {
    for (int i = 1; i <= 20; i++) {
      tree.insert(i, "value" + i);
    }
    assertTrue(tree.height() > 1);
  }

  @Test
  void emptyTreeReturnsNullForMinMaxKeys() {
    assertNull(tree.getMinKey());
    assertNull(tree.getMaxKey());
  }

  @Test
  void minMaxKeysReflectDataRange() {
    tree.insert(5, "five");
    tree.insert(1, "one");
    tree.insert(9, "nine");
    tree.insert(3, "three");

    assertEquals(Integer.valueOf(1), tree.getMinKey());
    assertEquals(Integer.valueOf(9), tree.getMaxKey());
  }

  @Test
  void clearEmptyTreeMaintainsEmptyState() {
    tree.clear();
    assertEquals(0, tree.size());
    assertNull(tree.getMinKey());
    assertNull(tree.getMaxKey());
  }

  @Test
  void clearRemovesAllDataAndResetsTree() {
    tree.insert(1, "one");
    tree.insert(2, "two");
    tree.insert(3, "three");

    tree.clear();
    assertEquals(0, tree.size());
    assertNull(tree.get(1));
    assertNull(tree.get(2));
    assertNull(tree.get(3));
  }

  @Test
  void rangeQueryOnEmptyTreeReturnsEmptyMap() {
    Map<Integer, String> result = tree.rangeQuery(1, 10);
    assertTrue(result.isEmpty());
  }

  @Test
  void rangeQueryWithNullKeysThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.rangeQuery(null, 10));
    assertThrows(IllegalArgumentException.class, () -> tree.rangeQuery(1, null));
    assertThrows(IllegalArgumentException.class, () -> tree.rangeQuery(null, null));
  }

  @Test
  void rangeQueryWithInvalidRangeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.rangeQuery(10, 1));
  }

  @Test
  void rangeQueryReturnsKeysWithinSpecifiedRange() {
    tree.insert(1, "one");
    tree.insert(3, "three");
    tree.insert(5, "five");
    tree.insert(7, "seven");
    tree.insert(9, "nine");

    Map<Integer, String> result = tree.rangeQuery(3, 7);
    assertEquals(3, result.size());
    assertEquals("three", result.get(3));
    assertEquals("five", result.get(5));
    assertEquals("seven", result.get(7));
  }

  @Test
  void rangeQueryWithSingleKeyReturnsOneEntry() {
    tree.insert(5, "five");
    Map<Integer, String> result = tree.rangeQuery(5, 5);
    assertEquals(1, result.size());
    assertEquals("five", result.get(5));
  }

  @Test
  void rangeQueryWithNoMatchesReturnsEmptyMap() {
    tree.insert(1, "one");
    tree.insert(10, "ten");
    Map<Integer, String> result = tree.rangeQuery(3, 7);
    assertTrue(result.isEmpty());
  }

  @Test
  void valueIteratorOnEmptyTreeHasNoElements() {
    Iterator<String> iterator = tree.iterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void valueIteratorReturnsValuesInKeyOrder() {
    tree.insert(3, "three");
    tree.insert(1, "one");
    tree.insert(2, "two");

    Iterator<String> iterator = tree.iterator();
    List<String> values = new ArrayList<>();
    while (iterator.hasNext()) {
      values.add(iterator.next());
    }

    assertEquals(3, values.size());
    assertEquals("one", values.get(0));
    assertEquals("two", values.get(1));
    assertEquals("three", values.get(2));
  }

  @Test
  void keyIteratorOnEmptyTreeHasNoElements() {
    Iterator<Integer> iterator = tree.keyIterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void keyIteratorReturnsKeysInAscendingOrder() {
    tree.insert(3, "three");
    tree.insert(1, "one");
    tree.insert(2, "two");

    Iterator<Integer> iterator = tree.keyIterator();
    List<Integer> keys = new ArrayList<>();
    while (iterator.hasNext()) {
      keys.add(iterator.next());
    }

    assertEquals(3, keys.size());
    assertEquals(Integer.valueOf(1), keys.get(0));
    assertEquals(Integer.valueOf(2), keys.get(1));
    assertEquals(Integer.valueOf(3), keys.get(2));
  }

  @Test
  void entryIteratorOnEmptyTreeHasNoElements() {
    Iterator<Map.Entry<Integer, String>> iterator = tree.entryIterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void entryIteratorReturnsEntriesInKeyOrder() {
    tree.insert(2, "two");
    tree.insert(1, "one");

    Iterator<Map.Entry<Integer, String>> iterator = tree.entryIterator();
    List<Map.Entry<Integer, String>> entries = new ArrayList<>();
    while (iterator.hasNext()) {
      entries.add(iterator.next());
    }

    assertEquals(2, entries.size());
    assertEquals(Integer.valueOf(1), entries.get(0).getKey());
    assertEquals("one", entries.get(0).getValue());
    assertEquals(Integer.valueOf(2), entries.get(1).getKey());
    assertEquals("two", entries.get(1).getValue());
  }

  @Test
  void rangeIteratorWithNullKeysThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.rangeIterator(null, 10));
    assertThrows(IllegalArgumentException.class, () -> tree.rangeIterator(1, null));
  }

  @Test
  void rangeIteratorWithInvalidRangeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tree.rangeIterator(10, 1));
  }

  @Test
  void rangeIteratorOnEmptyTreeHasNoElements() {
    Iterator<String> iterator = tree.rangeIterator(1, 10);
    assertFalse(iterator.hasNext());
  }

  @Test
  void rangeIteratorReturnsValuesInSpecifiedRange() {
    tree.insert(1, "one");
    tree.insert(3, "three");
    tree.insert(5, "five");
    tree.insert(7, "seven");

    Iterator<String> iterator = tree.rangeIterator(2, 6);
    List<String> values = new ArrayList<>();
    while (iterator.hasNext()) {
      values.add(iterator.next());
    }

    assertEquals(2, values.size());
    assertEquals("three", values.get(0));
    assertEquals("five", values.get(1));
  }

  @Test
  void largeDatasetInsertionMaintainsCorrectSize() {
    for (int i = 1; i <= 1000; i++) {
      assertTrue(tree.insert(i, "value" + i));
    }
    assertEquals(1000, tree.size());
    assertEquals(Integer.valueOf(1), tree.getMinKey());
    assertEquals(Integer.valueOf(1000), tree.getMaxKey());
  }

  @Test
  void largeDatasetDeletionMaintainsCorrectSize() {
    for (int i = 1; i <= 100; i++) {
      tree.insert(i, "value" + i);
    }

    for (int i = 2; i <= 100; i += 2) {
      assertEquals("value" + i, tree.delete(i));
    }

    assertEquals(50, tree.size());

    for (int i = 1; i <= 100; i++) {
      if (i % 2 == 1) {
        assertTrue(tree.contains(i));
      } else {
        assertFalse(tree.contains(i));
      }
    }
  }

  @Test
  void randomOrderInsertionMaintainsSortedOrder() {
    int[] randomOrder = {5, 2, 8, 1, 9, 3, 7, 4, 6};

    for (int key : randomOrder) {
      tree.insert(key, "value" + key);
    }

    assertEquals(9, tree.size());

    for (int key : randomOrder) {
      assertEquals("value" + key, tree.get(key));
    }

    Iterator<Integer> keyIterator = tree.keyIterator();
    int previousKey = 0;
    while (keyIterator.hasNext()) {
      int currentKey = keyIterator.next();
      assertTrue(currentKey > previousKey);
      previousKey = currentKey;
    }
  }

  @Test
  void bankingAccountLookupRetrievesCorrectBalances() {
    bankingTree.insert("ACC001", 1500.50);
    bankingTree.insert("ACC002", 2750.25);
    bankingTree.insert("ACC003", 500.00);

    assertEquals(Double.valueOf(1500.50), bankingTree.get("ACC001"));
    assertEquals(Double.valueOf(2750.25), bankingTree.get("ACC002"));
    assertEquals(Double.valueOf(500.00), bankingTree.get("ACC003"));
  }

  @Test
  void bankingBalanceUpdateOverwritesPreviousValue() {
    bankingTree.insert("ACC001", 1000.00);
    assertEquals(Double.valueOf(1000.00), bankingTree.get("ACC001"));

    assertFalse(bankingTree.insert("ACC001", 1250.00));
    assertEquals(Double.valueOf(1250.00), bankingTree.get("ACC001"));
    assertEquals(1, bankingTree.size());
  }

  @Test
  void bankingAccountRangeQueryReturnsAccountsInRange() {
    bankingTree.insert("ACC001", 1000.00);
    bankingTree.insert("ACC005", 2000.00);
    bankingTree.insert("ACC010", 3000.00);
    bankingTree.insert("ACC015", 4000.00);
    bankingTree.insert("ACC020", 5000.00);

    Map<String, Double> range = bankingTree.rangeQuery("ACC005", "ACC015");
    assertEquals(3, range.size());
    assertTrue(range.containsKey("ACC005"));
    assertTrue(range.containsKey("ACC010"));
    assertTrue(range.containsKey("ACC015"));
  }

  @Test
  void bankingAccountDeletionRemovesAccountAndReturnsBalance() {
    bankingTree.insert("ACC001", 1000.00);
    bankingTree.insert("ACC002", 2000.00);

    Double deletedBalance = bankingTree.delete("ACC001");
    assertEquals(Double.valueOf(1000.00), deletedBalance);
    assertFalse(bankingTree.contains("ACC001"));
    assertTrue(bankingTree.contains("ACC002"));
    assertEquals(1, bankingTree.size());
  }

  @Test
  void treeRemainsBalancedWithSequentialInsertions() {
    for (int i = 1; i <= 50; i++) {
      tree.insert(i, "value" + i);
    }

    int height = tree.height();
    assertTrue(height <= 10, "Tree height is too large: " + height);

    for (int i = 1; i <= 50; i++) {
      assertEquals("value" + i, tree.get(i));
    }
  }

  @Test
  void iteratorRemainsValidAfterTreeModification() {
    tree.insert(1, "one");
    tree.insert(2, "two");
    tree.insert(3, "three");

    Iterator<String> iterator = tree.iterator();
    assertTrue(iterator.hasNext());

    tree.insert(4, "four");

    assertDoesNotThrow(() -> {
      while (iterator.hasNext()) {
        iterator.next();
      }
    });
  }

  @Test
  void emptyRangeOperationsReturnEmptyResults() {
    Map<Integer, String> emptyRange = tree.rangeQuery(5, 5);
    assertTrue(emptyRange.isEmpty());

    Iterator<String> emptyIterator = tree.rangeIterator(5, 5);
    assertFalse(emptyIterator.hasNext());
  }

  @Test
  void singleElementOperationsWorkCorrectly() {
    tree.insert(42, "answer");

    assertEquals(1, tree.size());
    assertEquals(Integer.valueOf(42), tree.getMinKey());
    assertEquals(Integer.valueOf(42), tree.getMaxKey());
    assertEquals(1, tree.height());

    Map<Integer, String> singleRange = tree.rangeQuery(42, 42);
    assertEquals(1, singleRange.size());
    assertEquals("answer", singleRange.get(42));
  }

  @Test
  void complexBankingScenarioHandlesMultipleOperations() {
    String[] accounts = {"ACC001", "ACC002", "ACC003", "ACC004", "ACC005",
        "ACC006", "ACC007", "ACC008", "ACC009", "ACC010"};
    Double[] balances = {1000.0, 1500.5, 2000.25, 750.0, 3000.75,
        500.0, 4000.0, 1250.25, 2500.5, 1750.75};

    for (int i = 0; i < accounts.length; i++) {
      bankingTree.insert(accounts[i], balances[i]);
    }

    assertEquals(10, bankingTree.size());

    bankingTree.insert("ACC001", 1100.0);
    bankingTree.insert("ACC005", 2800.75);

    bankingTree.delete("ACC003");
    bankingTree.delete("ACC007");

    assertEquals(8, bankingTree.size());

    Map<String, Double> midRangeAccounts = bankingTree.rangeQuery("ACC002", "ACC006");
    assertEquals(4, midRangeAccounts.size());

    assertEquals(Double.valueOf(1100.0), bankingTree.get("ACC001"));
    assertEquals(Double.valueOf(2800.75), bankingTree.get("ACC005"));
    assertNull(bankingTree.get("ACC003"));
    assertNull(bankingTree.get("ACC007"));
  }
}