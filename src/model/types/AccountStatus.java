package model.types;

/**
 * Enumerates all possible Status' which an account can have. When an account has a certain Status,
 * its properties and behavior should reflect that Status, but the implementation of this is up
 * to those classes which use this Enumeration.
 */
public enum AccountStatus {
  Free, Frozen;

  AccountStatus makeStatus(String status) {
    return switch (status) {
      case "Free" -> Free;
      case "Frozen" -> Frozen;
      default -> throw new IllegalArgumentException("Unknown status " + status);
    };
  }
}
