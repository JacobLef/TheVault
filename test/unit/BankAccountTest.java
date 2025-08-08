package unit;

import model.types.AccountType;
import model.types.AccountStatus;
import model.user.BankAccount;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Collection of tests for BankAccount objects (record).
 */
class BankAccountTest {
  private BankAccount accountOne;
  private LocalDateTime testTime;

  @BeforeEach
  void setUp() {
    testTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    accountOne = new BankAccount(
        "testuser",
        "Checking Account",
        100.0,
        AccountType.CheckingAccount,
        AccountStatus.Free,
        testTime
    );
  }

  @Test
  public void gettersReturnCorrectValues() {
    assertEquals("testuser", accountOne.username());
    assertEquals("Checking Account", accountOne.accountName());
    assertEquals(100.0, accountOne.balance(), 0.01);
    assertEquals(AccountStatus.Free, accountOne.status());
    assertEquals(AccountType.CheckingAccount, accountOne.type());
    assertEquals(testTime, accountOne.createdAt());
  }

  @Test
  public void accountsWithSameUsernameCanBeMade() {
    assertDoesNotThrow(() -> new BankAccount(
        "testuser",
        "Savings Account",
        200.0,
        AccountType.SavingsAccount,
        AccountStatus.Frozen,
        testTime
    ));
  }

  @Test
  public void nullValueForTypeAndStatusThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new BankAccount(
          "testuser",
          "Test Account",
          100.0,
          null,
          AccountStatus.Free,
          testTime
      );
    });

    assertThrows(NullPointerException.class, () -> {
      new BankAccount(
          "testuser",
          "Test Account",
          100.0,
          AccountType.CheckingAccount,
          null,
          testTime
      );
    });
  }

  @Test
  public void nullCreatedAtDefaultsToNow() {
    BankAccount account = new BankAccount(
        "testuser",
        "Test Account",
        50.0,
        AccountType.SavingsAccount,
        AccountStatus.Free,
        null
    );

    assertNotNull(account.createdAt());
    // Should be close to current time
    assertTrue(account.createdAt().isAfter(LocalDateTime.now().minusMinutes(1)));
  }

  @Test
  public void toStringContainsAllInformation() {
    String result = accountOne.toString();

    assertTrue(result.contains("testuser"));
    assertTrue(result.contains("Checking Account"));
    assertTrue(result.contains("100.0"));
    assertTrue(result.contains("CheckingAccount"));
    assertTrue(result.contains("Free"));
  }

  @Test
  public void recordEqualityWorksCorrectly() {
    BankAccount identical = new BankAccount(
        "testuser",
        "Checking Account",
        100.0,
        AccountType.CheckingAccount,
        AccountStatus.Free,
        testTime
    );

    BankAccount different = new BankAccount(
        "testuser",
        "Savings Account",
        100.0,
        AccountType.CheckingAccount,
        AccountStatus.Free,
        testTime
    );

    assertEquals(accountOne, identical);
    assertNotEquals(accountOne, different);
    assertEquals(accountOne.hashCode(), identical.hashCode());
  }
}
