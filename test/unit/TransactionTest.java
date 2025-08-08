package unit;

import java.time.LocalDateTime;
import model.user.Transaction;
import model.types.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Collection of tests for Transaction objects (record).
 */
class TransactionTest {
  private Transaction transaction;
  private LocalDateTime testTime;

  @BeforeEach
  void setUp() {
    testTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    this.transaction = new Transaction(
        "fromUser",
        "toUser",
        "fromAccount",
        "toAccount",
        100.0,
        TransactionType.EXTERNALTRANSFER,
        testTime
    );
  }

  @Test
  public void gettersReturnCorrectValues() {
    assertEquals("fromUser", transaction.fromUsername());
    assertEquals("toUser", transaction.toUsername());
    assertEquals("fromAccount", transaction.fromAccountName());
    assertEquals("toAccount", transaction.toAccountName());
    assertEquals(100.0, transaction.amount(), 0.01);
    assertEquals(TransactionType.EXTERNALTRANSFER, transaction.type());
    assertEquals(testTime, transaction.createdAt());
  }

  @Test
  public void fromAndToUsernameCanBeTheSameForInternalTransfer() {
    assertDoesNotThrow(() -> {
      new Transaction(
          "sameUser",
          "sameUser",
          "checking",
          "savings",
          100.0,
          TransactionType.INTERNALTRANSFER,
          testTime
      );
    });
  }

  @Test
  public void nullTimeDefaultsToNow() {
    Transaction txn = new Transaction(
        "user1",
        "user2",
        "account1",
        "account2",
        50.0,
        TransactionType.EXTERNALTRANSFER,
        null
    );

    assertNotNull(txn.createdAt());
    assertTrue(txn.createdAt().isAfter(LocalDateTime.now().minusMinutes(1)));
  }

  @Test
  public void convenienceConstructorSetsCurrentTime() {
    Transaction txn = new Transaction(
        "user1",
        "user2",
        "account1",
        "account2",
        75.0,
        TransactionType.EXTERNALTRANSFER
    );

    assertNotNull(txn.createdAt());
    assertTrue(txn.createdAt().isAfter(LocalDateTime.now().minusMinutes(1)));
  }

  @Test
  public void mismatchedTransactionTypeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          "sameUser",
          "sameUser",
          "account1",
          "account2",
          100.0,
          TransactionType.EXTERNALTRANSFER,
          testTime
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          "user1",
          "user2",
          "account1",
          "account2",
          100.0,
          TransactionType.INTERNALTRANSFER,
          testTime
      );
    });
  }

  @Test
  public void toStringContainsAllInformation() {
    String result = transaction.toString();

    assertTrue(result.contains("fromUser"));
    assertTrue(result.contains("toUser"));
    assertTrue(result.contains("fromAccount"));
    assertTrue(result.contains("toAccount"));
    assertTrue(result.contains("100.0"));
    assertTrue(result.contains("EXTERNALTRANSFER"));
  }

  @Test
  public void recordEqualityWorksCorrectly() {
    Transaction identical = new Transaction(
        "fromUser",
        "toUser",
        "fromAccount",
        "toAccount",
        100.0,
        TransactionType.EXTERNALTRANSFER,
        testTime
    );

    Transaction different = new Transaction(
        "fromUser",
        "toUser",
        "fromAccount",
        "toAccount",
        200.0,
        TransactionType.EXTERNALTRANSFER,
        testTime
    );

    assertEquals(transaction, identical);
    assertNotEquals(transaction, different);
    assertEquals(transaction.hashCode(), identical.hashCode());
  }
}