package model.user;

import model.types.AccountStatus;
import model.types.AccountType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An immutable instance of a BankAccount such that it contains all the relevant information
 * pertinent to a User's account but cannot be changed.
 * @param accountId   The ID of the account.
 * @param userId      The ID of the user who uses this account.
 * @param balance     The balance of this account.
 * @param type        The type of Account which this BankAccount represents.
 * @param status      The status of this BankAccount.
 * @param createdAt   The time at which this BankAccount was created.
 */
public record BankAccount(
    int accountId,
    int userId,
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
}
