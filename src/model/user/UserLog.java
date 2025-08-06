package model.user;

import java.util.*;

/**
 * Serves as a storage for all the information associated with a User, including the information
 * associated with their User account as well as all the names and information pertaining to the
 * accounts which they have opened.
 */
public record UserLog(
    User user,
    Map<String, BankAccount> accounts,
    List<Transaction> transactions
) {
  public UserLog {
    Objects.requireNonNull(user);
    if (accounts == null) {
      accounts = new HashMap<>();
    }
    if (transactions == null) {
      transactions = new ArrayList<>();
    }
  }
}
