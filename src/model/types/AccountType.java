package model.types;

/**
 * Enumerates all possible types of Accounts which can be stored within a Bank. Each kind of Account
 * has different properties.
 */
public enum AccountType {
  CheckingAccount, SavingsAccount;

  AccountType makeType(String type) {
    return switch (type) {
      case "Checking" -> CheckingAccount;
      case "Savings" -> SavingsAccount;
      default -> throw new IllegalArgumentException("Unknown AccountType: " + type);
    };
  }
}
