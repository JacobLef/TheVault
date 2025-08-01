package model.types;

/**
 * Enumerates all possible types of Transactions which a user can initiate.
 */
public enum TransactionType {
  InternalTransfer, ExternalTransfer;

  TransactionType makeTransaction(String transactionId) {
    return switch (transactionId.trim()) {
      case "InternalTransfer" -> InternalTransfer;
      case "ExternalTransfer" -> ExternalTransfer;
      default -> throw new IllegalArgumentException("Unknown transaction type: " + transactionId);
    };
  }
}
