package unit;

import java.time.LocalDateTime;

import model.user.Transaction;
import model.types.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


/**
 * Collection of tests for Transaction objects (record).
 */
class TransactionTest {
  private Transaction transaction;

  @BeforeEach
  void setUp() {
    this.transaction = new Transaction(
        10,
        5,
        4,
        1,
        2,
        100.0,
        TransactionType.ExternalTransfer,
        LocalDateTime.of(2025, 1, 1, 0, 0)
    );
  }

  @Test
  public void gettersReturnCorrectValues() {
    // Sanity checks : trivial since they are getters, but important in case the Record is later
    // changed to be an explicitly defined class
    assertEquals(10, transaction.transactionId());
    assertEquals(5, transaction.fromUserId());
    assertEquals(4, transaction.toUserId());
    assertEquals(1, transaction.fromAccountId());
    assertEquals(2, transaction.toAccountId());
    assertEquals(100.0, transaction.amount());
    assertEquals(TransactionType.ExternalTransfer, transaction.type());
    assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), transaction.createdAt());
  }

  @Test
  public void fromAndToIdCanBeTheSame() {
    assertDoesNotThrow(() -> {
      new Transaction(
          10,
          5,
          5,
          1,
          2,
          100.0,
          TransactionType.InternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertDoesNotThrow(() -> {
      new Transaction(
          10,
          5,
          5,
          1,
          1,
          100.0,
          TransactionType.InternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });
  }

  @Test
  public void nullTimeDefaultsToNow() {
    assertEquals(
        LocalDateTime.now(),
        new Transaction(
            10,
            5,
            5,
            1,
            1,
            100.0,
            TransactionType.InternalTransfer,
            null
        ).createdAt()
    );
  }

  @Test
  public void mismatchedTransactionTypeOrUserIdsThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          5,
          5,
          1,
          2,
          10.70,
          TransactionType.ExternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          5,
          4,
          1,
          3,
          100.0,
          TransactionType.InternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });
  }

  @Test
  public void negativeIdOrAmountThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          5,
          4,
          1,
          1,
          -10.0,
          TransactionType.ExternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          5,
          4,
          1,
          -1,
          10.0,
          TransactionType.ExternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          5,
          4,
          -1,
          1,
          10.0,
          TransactionType.ExternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          4,
          -4,
          1,
          1,
          10.0,
          TransactionType.InternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          10,
          -5,
          4,
          1,
          1,
          10.0,
          TransactionType.ExternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Transaction(
          -10,
          5,
          5,
          1,
          1,
          10.0,
          TransactionType.InternalTransfer,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });
  };
}