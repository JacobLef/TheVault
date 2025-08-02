package model.user;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an immutable instance of a User such that the user's id, username, email, and the
 * time at which their account was created can all be accessed.
 * @param userId    The ID of this User.
 * @param username  The username of this User's account.
 * @param password  The password of this User's account.
 * @param email     The email of this User.
 * @param createdAt The time at which the User made their account.
 */
public record User(
    int userId,
    String username,
    String password,
    String email,
    LocalDateTime createdAt
) {
  public User {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    Objects.requireNonNull(username);
    Objects.requireNonNull(email);
    Objects.requireNonNull(password);
    username = username.trim();
    email = email.trim();
  }

  @Override
  public String toString() {
    return
        "User Id: " + userId + "\n"
        + "\tUsername: " + username + "\n"
        + "\tEmail: " + email + "\n"
        + "\tPassword: " + password + "\n"
        + "\tCreated At: " + createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return this.userId == user.userId && this.username.equals(user.username)
        && this.email.equals(user.email) && this.password.equals(user.password);
  }
}

