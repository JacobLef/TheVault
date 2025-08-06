package model.user;

import model.types.TransactionType;

import java.time.LocalDateTime;

/**
 * An immutable instance of a Transaction which contains all the relevant information needed
 * to properly execute a Transaction.
 * @param fromUsername     The name of the user whose account is being withdrawn from.
 * @param toUsername       The name of the user whose account is being deposited into.
 * @param fromAccountName  The name of the account under the fromUserId.
 * @param toAccountName    The name of the account under the toUserId.
 * @param amount           The amount to withdraw from {@code fromAccount} to {@code toAccount}.
 * @param type             The type of transaction which this is.
 * @param createdAt        The time at which this Transaction was initiated.
 */
public record Transaction(
    String fromUsername,
    String toUsername,
    String fromAccountName,
    String toAccountName,
    double amount,
    TransactionType type,
    LocalDateTime createdAt
) {
  public Transaction {
    if (
        fromUsername.equals(toUsername) && TransactionType.ExternalTransfer.equals(type)
        || !fromUsername.equals(toUsername) && TransactionType.InternalTransfer.equals(type)
    ) {
      throw new IllegalArgumentException(
          "Incorrect transaction type marker for a Transaction of the following: " + this
      );
    }

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @Override
  public String toString() {
    return
        "Transaction: " + "\n"
        + "\tFrom (User, Account): (" + this.fromUsername + ", " + this.fromAccountName + ")\n"
        + "\tTo (User, Account): (" + this.toUsername + ", " + this.toAccountName + ")\n"
        + "\tAmount: " + this.amount + "\n"
        + "\tType: " + this.type + "\n"
        + "\tCreatedAt: " + this.createdAt + "\n";
  }
}
