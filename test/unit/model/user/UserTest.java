package unit.model.user;

import model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Collection of tests for User objects (record).
 */
class UserTest {
  private User user;
  private LocalDateTime testTime;

  @BeforeEach
  void setUp() {
    testTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    this.user = new User(
        "Test User",
        "Test Password",
        "test@gmail.com",
        testTime
    );
  }

  @Test
  public void gettersReturnAppropriateValues() {
    assertEquals("Test User", this.user.username());
    assertEquals("Test Password", this.user.password());
    assertEquals("test@gmail.com", this.user.email());
    assertEquals(testTime, this.user.createdAt());
  }

  @Test
  public void nullFieldsThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new User(null, "pass", "test@gmail.com", testTime);
    });

    assertThrows(NullPointerException.class, () -> {
      new User("Test User", null, "test@gmail.com", testTime);
    });

    assertThrows(NullPointerException.class, () -> {
      new User("Test User", "pass", null, testTime);
    });
  }

  @Test
  public void convenienceConstructorSetsCurrentTime() {
    User userWithDefaultTime = new User("username", "password", "email@test.com");

    assertNotNull(userWithDefaultTime.createdAt());
    assertTrue(userWithDefaultTime.createdAt().isAfter(LocalDateTime.now().minusMinutes(1)));
  }

  @Test
  public void usernameAndEmailAreTrimmed() {
    User userWithSpaces = new User(
        "  spaced username  ",
        "password",
        "  spaced@email.com  "
    );

    assertEquals("spaced username", userWithSpaces.username());
    assertEquals("spaced@email.com", userWithSpaces.email());
    assertEquals("password", userWithSpaces.password());
  }

  @Test
  public void toStringContainsAllInformation() {
    String result = user.toString();

    assertTrue(result.contains("Test User"));
    assertTrue(result.contains("Test Password"));
    assertTrue(result.contains("test@gmail.com"));
    assertTrue(result.contains(testTime.toString()));
  }

  @Test
  public void customEqualsIgnoresCreatedAt() {
    User sameUserDifferentTime = new User(
        "Test User",
        "Test Password",
        "test@gmail.com",
        LocalDateTime.of(2024, 12, 31, 23, 59)
    );

    User differentUser = new User(
        "Different User",
        "Test Password",
        "test@gmail.com",
        testTime
    );

    assertEquals(user, sameUserDifferentTime);
    assertNotEquals(user, differentUser);
  }
}