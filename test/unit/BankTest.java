package unit;

import model.Bank;
import model.Managed;
import model.data_engine.DataBase;
import model.types.AccountType;
import model.user.BankAccount;
import model.user.User;
import model.user.UserLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Methods to Test:
 * createUser => DONE
 * updateUser
 * deleteUser => DONE
 * createAccount
 * deleteAccount
 * withdraw
 * deposit
 * transfer
 * getBalance
 * getUser
 * getAccountFor
 * getAccountsFor
 * accountExists
 * userExists
 */

/**
 * This is a tester class for Bank objects.
 */
class BankTest {
  private Managed modifiableBank;
  private Managed setBank;

  @BeforeEach
  void setUp() {
    this.modifiableBank = new Bank(DataBase.getInstance(), "Modifiable Test Bank", 0);
    this.modifiableBank.createUser("First User", "First Password", "first@gmail.com");
    this.setBank = new Bank(DataBase.getInstance(), "Set Test Bank", 1);
    this.setBank.createUser("First User", "First Password", "first@gmail.com");
    this.setBank.createUser("Second User", "Second Password", "second@gmail.com");
    this.setBank.createAccount(
        "First User",
        "First Password",
        "First Account1",
        AccountType.CheckingAccount,
        100.0
    );
    this.setBank.createAccount(
        "Second User",
        "Second Password",
        "Second Account1",
        AccountType.SavingsAccount
    );
  }

  @Test
  public void createAccountDoesntModifyInformation() {
    assertFalse(this.modifiableBank.userExists(
        "Second User",
        "Second Password"
        )
    );

    this.modifiableBank.createUser(
        "Second User",
        "Second Password",
        "second@gmail.com"
    );
    assertTrue(this.modifiableBank.userExists("Second User", "Second Password"));
    User secondUser = this.modifiableBank.getUser("Second User", "Second Password");
    assertEquals("Second User", secondUser.username());
    assertEquals("Second Password", secondUser.password());
    assertEquals("second@gmail.com", secondUser.email());
  }

  @Test
  public void duplicateUserNameFails() {
    assertThrows(IllegalArgumentException.class, () -> {
      this.modifiableBank.createUser(
          "First User",
          "Different Password",
          "different@gmail.com"
      );
    });
  }

  @Test
  public void duplicateEmailAlwaysThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      this.modifiableBank.createUser(
          "Different Name",
          "Different Password",
          "first@gmail.com"
      );
    });

    assertThrows(IllegalArgumentException.class, () -> {
      this.modifiableBank.createUser(
          "Different  User",
          "First Password",
          "first@gmail.com"
      );
    });
  }

  @Test
  public void duplicatePasswordsDifferentNamesAreAllowed() {
    assertDoesNotThrow(() -> {
      this.modifiableBank.createUser(
          "Different User",
          "First Password",
          "different@gmail.com"
      );
    });
  }

  @Test
  public void nullUserPropertyFieldsThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      this.modifiableBank.createUser(null, "pass", "pass@gmail.com");
    });
    assertThrows(NullPointerException.class, () -> {
      this.modifiableBank.createUser("First User", null, "first@gmail.com");
    });
    assertThrows(NullPointerException.class, () -> {
      this.modifiableBank.createUser("First User", "", null);
    });
  }

  @Test
  public void deletedUserNoLongerExistsAndLogIsCorrect() {
    assertTrue(this.setBank.userExists("First User", "First Password"));
    assertFalse(this.setBank.getAccountsFor("First User", "First Password").isEmpty());

    UserLog log = this.setBank.deleteUser("First User", "First Password");
    assertFalse(this.setBank.userExists("First User", "First Password"));
    assertThrows(IllegalArgumentException.class, () -> this.setBank.getAccountsFor(
        "First User",
        "First Password"
        )
    );
    assertTrue(log.transactions().isEmpty());
    assertFalse(log.accounts().isEmpty());

    // There is only one account under this user
    BankAccount acc = log.accounts().get("First Account1");
    assertEquals(100.0, acc.balance());
    assertEquals(AccountType.CheckingAccount, acc.type());
  }

  @Test
  public void complicatedLogAfterDeletion() {
    String[] names = { "First Account", "Second Account", "Third Account" };
    AccountType[] types = {
        AccountType.CheckingAccount,
        AccountType.SavingsAccount,
        AccountType.SavingsAccount
    };
    double[] balances = {
        100.0,
        10.0,
        0.0
    };

    for (int i = 0; i < names.length; i++) {
      this.modifiableBank.createAccount(
          "First User",
          "First Password",
          names[i],
          types[i],
          balances[i]
      );
    }

    assertTrue(this.setBank.userExists("First User", "First Password"));
    UserLog log = this.setBank.deleteUser("First User", "First Password");
    assertFalse(this.setBank.userExists("First User", "First Password"));

    User user = log.user();
    Map<String, BankAccount> accounts = log.accounts();

    assertEquals("First User", user.username());
    assertEquals("First Password", user.password());
    assertEquals("first@gmail.com", user.email());

    int idx = 0;
    for (Map.Entry<String, BankAccount> acc : accounts.entrySet()) {
      assertEquals(names[idx], acc.getKey());
      assertEquals(types[idx], acc.getValue().type());
      assertEquals(balances[idx], acc.getValue().balance());
      idx++;
    }
  }

  @Test
  public void canUpdateName() {

  }

  @Test
  public void cannotChangeNameToNonUnique() {

  }

  @Test
  public void canUpdatePassword() {

  }

  @Test
  public void canChangePasswordToNonUnique() {

  }

  @Test
  public void canUpdateEmail() {

  }

  @Test
  public void cannotUpdateEmailToNonUnique() {

  }

  @Test
  public void oneUserCanHaveAnyUniquelyNamedAccounts() {
    this.modifiableBank.createAccount(
        "First User",
        "First Password",
        "Account One",
        AccountType.SavingsAccount,
        100.0
    );
    assertDoesNotThrow(() -> this.modifiableBank.createAccount(
        "First User",
        "First Password",
        "Account Two",
        AccountType.SavingsAccount,
        100.0
    ));
  }

  @Test
  public void defaultBalanceIsZero() {
    this.modifiableBank.createAccount(
        "First User",
        "First Password",
        "Account One",
        AccountType.valueOf("Savings")
    );
    this.modifiableBank.createAccount(
        "First User",
        "First Password",
        "Account Two",
        AccountType.valueOf("Checking")
    );

    BankAccount firstAccount = this.modifiableBank.getAccountFor(
        "First User",
        "First Password",
        "Account One"
    );
    BankAccount secondAccount = this.modifiableBank.getAccountFor(
        "First User",
        "First Password",
        "Account Two"
    );
    assertEquals(0.0, firstAccount.balance());
    assertEquals(0.0, secondAccount.balance());
  }
}