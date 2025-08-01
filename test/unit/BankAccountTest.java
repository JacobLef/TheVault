package unit;

import model.types.AccountType;
import model.types.AccountStatus;
import model.user.BankAccount;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Collection of tests for BankAccount objects (record).
 */
class BankAccountTest {
  private BankAccount accountOne;

  @BeforeEach
  void setUp() {
    accountOne = new BankAccount(
        1,
        2,
        100.0,
        AccountType.CheckingAccount,
        AccountStatus.Free,
        LocalDateTime.of(2025, 1, 1, 0, 0)
    );
  }

  @Test
  public void gettersReturnCorrectValues() {
    // Since the getters are Java-defined and not defined by the actual implementor, these are
    // trivial to test and are left here as sanity checks
    assertEquals(1, accountOne.accountId());
    assertEquals(2, accountOne.userId());
    assertEquals(100.0, accountOne.balance(), 0.01);
    assertEquals(AccountStatus.Free, accountOne.status());
    assertEquals(AccountType.CheckingAccount, accountOne.type());

    LocalDateTime expectedTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    assertEquals(expectedTime, accountOne.createdAt());
  }

  @Test
  public void accountsWithSameUserIdCanBeMade() {
    // Sanity check : trivial to test this as an instance of one record does not affect another
    // instance since they do not know of each other
    assertDoesNotThrow(() -> new BankAccount(
        10,
        1,
        100.0,
        AccountType.SavingsAccount,
        AccountStatus.Frozen,
        LocalDateTime.of(2025, 1, 1, 0, 0)
    ));
  }

  @Test
  public void nullValueForTypeAndStatusThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new BankAccount(
          10,
          1,
          100.0,
          null,
          AccountStatus.Free,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });

    assertThrows(NullPointerException.class, () -> {
      new BankAccount(
          10,
          1,
          100.0,
          AccountType.CheckingAccount,
          null,
          LocalDateTime.of(2025, 1, 1, 0, 0)
      );
    });
  }
}