package unit;

import java.time.LocalDateTime;

import model.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Collection of tests for User objects (record).
 */
class UserTest {
  private User user;

  @BeforeEach
  void setUp() {
    this.user = new User(
        10,
        "Test User",
        "Test Password",
        "test@gmail.com",
        LocalDateTime.of(2025, 1, 1, 0, 0));
  }

  @Test
  public void gettersReturnAppropriateValues() {
    assertEquals(10, this.user.userId());
    assertEquals("Test User", this.user.username());
    assertEquals("Test Password", this.user.password());
    assertEquals("test@gmail.com", this.user.email());
    assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), this.user.createdAt());
  }

  @Test
  public void nullFieldsThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new User(10, null, "pass", "test@gmail.com", LocalDateTime.of(2025, 1, 1, 0, 0));
    });

    assertThrows(NullPointerException.class, () -> {
      new User(1, "Test User", null, "test@gmail.com", LocalDateTime.of(2025, 1, 1, 0, 0));
    });

    assertThrows(NullPointerException.class, () -> {
      new User(1, "Test User", "pass", null, LocalDateTime.of(2025, 1, 1, 0, 0));
    });

    assertThrows(NullPointerException.class, () -> {
      new User(1, "Test", "pass", "test@gmail.com", null);
    });
  }
}