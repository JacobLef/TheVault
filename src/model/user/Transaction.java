package model.user;

import model.types.TransactionType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An immutable instance of a Transaction which contains all the relevant information needed
 * to properly execute a Transaction.
 * @param transactionId   The ID of this Transaction.
 * @param fromAccount     The ID of the user whose account is being withdrawn from.
 * @param toAccount       The ID of the user whose account is being deposited into.
 * @param amount          The amount to withdraw from {@code fromAccount} to {@code toAccount}.
 * @param type            The type of transaction which this is.
 * @param createdAt       The time at which this Transaction was initiated.
 */
public record Transaction(
    String transactionId,
    int fromAccount,
    int toAccount,
    double amount,
    TransactionType type,
    LocalDateTime createdAt
) {
  public Transaction {
    Objects.requireNonNull(transactionId);
    if (
        fromAccount == toAccount && TransactionType.ExternalTransfer.equals(type)
        || fromAccount != toAccount && TransactionType.InternalTransfer.equals(type)
    ) {
      throw new IllegalArgumentException(
          "Incorrect transaction type marker for a Transaction of the following: " + this
      );
    }
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }

    Objects.requireNonNull(transactionId);
    transactionId = transactionId.trim();
  }

  @Override
  public String toString() {
    return "";
  }
}
