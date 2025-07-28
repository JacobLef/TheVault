package model.user;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an immutable instance of a User such that the user's id, username, email, and the
 * time at which their account was created can all be accessed.
 * @param userId    The ID of this User.
 * @param username  The username of this User.
 * @param email     The email of this User.
 * @param createdAt The time at which the User made their account.
 */
public record User(int userId, String username, String email, LocalDateTime createdAt) {
  public User {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    Objects.requireNonNull(username);
    Objects.requireNonNull(email);
    username = username.trim();
    email = email.trim();
  }
}

