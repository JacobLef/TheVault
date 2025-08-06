package model.user;

import model.types.AccountStatus;
import model.types.AccountType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An immutable instance of a BankAccount such that it contains all the relevant information
 * pertinent to a User's account but cannot be changed.
 * @param username    The name of the user.
 * @param accountName The name of the account.
 * @param balance     The balance of this account.
 * @param type        The type of Account which this BankAccount represents.
 * @param status      The status of this BankAccount.
 * @param createdAt   The time at which this BankAccount was created.
 */
public record BankAccount(
    String username,
    String accountName,
    double balance,
    AccountType type,
    AccountStatus status,
    LocalDateTime createdAt
) {
  public BankAccount {
    Objects.requireNonNull(type);
    Objects.requireNonNull(status);

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @Override
  public String toString() {
    return
        "Username: " + username + "\n"
        + "\tBank Account Name: " + accountName + "\n"
        + "\tBalance: " + balance + "\n"
        + "\tType: " + type + "\n"
        + "\tStatus: " + status + "\n"
        + "\tCreatedAt: " + createdAt + "\n";
  }
}
