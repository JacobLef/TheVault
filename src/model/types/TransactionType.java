package model.types;

/**
 * Enumerates all possible types of Transactions which a user can initiate.
 */
public enum TransactionType {
  INTERNALTRANSFER, EXTERNALTRANSFER, DEPOSIT, WITHDRAWAL;

  TransactionType makeTransaction(String transactionId) {
    return switch (transactionId.trim()) {
      case "InternalTransfer" -> INTERNALTRANSFER;
      case "ExternalTransfer" -> EXTERNALTRANSFER;
      case "Deposit" -> DEPOSIT;
      case "Withdrawal" -> WITHDRAWAL;
      default -> throw new IllegalArgumentException("Unknown transaction type: " + transactionId);
    };
  }
}
