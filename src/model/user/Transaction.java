package model.user;

import model.types.TransactionType;

import java.time.LocalDateTime;

/**
 * An immutable instance of a Transaction which contains all the relevant information needed
 * to properly execute a Transaction.
 * @param transactionId  The ID of this Transaction.
 * @param fromUserId     The ID of the user whose account is being withdrawn from.
 * @param toUserId       The ID of the user whose account is being deposited into.
 * @param fromAccountId  The ID of the account under the fromUserId.
 * @param toAccountId    The ID of the account under the toUserId.
 * @param amount         The amount to withdraw from {@code fromAccount} to {@code toAccount}.
 * @param type           The type of transaction which this is.
 * @param createdAt      The time at which this Transaction was initiated.
 */
public record Transaction(
    int transactionId,
    int fromUserId,
    int toUserId,
    int fromAccountId,
    int toAccountId,
    double amount,
    TransactionType type,
    LocalDateTime createdAt
) {
  public Transaction {
    if (
        fromUserId == toUserId && TransactionType.ExternalTransfer.equals(type)
        || fromUserId != toUserId && TransactionType.InternalTransfer.equals(type)
    ) {
      throw new IllegalArgumentException(
          "Incorrect transaction type marker for a Transaction of the following: " + this
      );
    }

    if (fromAccountId < 0 || toAccountId < 0 || toUserId < 0 || transactionId < 0) {
      throw new IllegalArgumentException(
          "Id's for transactions, users, and accounts cannot be negative: " + this
      );
    }

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @Override
  public String toString() {
    return
        "Transaction ID: " + this.transactionId + "\n"
        + "\tFrom (User, Account): (" + this.fromUserId + ", " + this.fromAccountId + ")\n"
        + "\tTo (User, Account): (" + this.toUserId + ", " + this.toAccountId + ")\n"
        + "\tAmount: " + this.amount + "\n"
        + "\tType: " + this.type + "\n"
        + "\tCreatedAt: " + this.createdAt + "\n";
  }
}
