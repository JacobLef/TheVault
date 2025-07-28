package model.types;

/**
 * Enumerates all possible types of Transactions which a user can initiate.
 */
public enum TransactionType {
  InternalTransfer, ExternalTransfer, Withdrawal, Deposit;

  TransactionType makeTransaction(String transactionId) {
    return switch (transactionId) {
      case "InternalTransfer" -> InternalTransfer;
      case "ExternalTransfer" -> ExternalTransfer;
      case "Withdrawal" -> Withdrawal;
      case "Deposit" -> Deposit;
      default -> throw new IllegalArgumentException("Unknown transaction type: " + transactionId);
    };
  }
}
