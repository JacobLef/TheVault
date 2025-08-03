package unit;

import model.Bank;
import model.Managed;
import model.data_engine.DataBase;
import model.types.AccountType;
import model.types.UserProperty;
import model.user.BankAccount;
import model.user.User;
import model.user.UserLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

/*
 * Methods to Test:
 * createUser => DONE
 * updateUser => DONE
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
  private static Managed staticTestBank;

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

    staticTestBank = new Bank(DataBase.getInstance(), "Static Test Bank", 12);
    staticTestBank.createUser("First User", "First Password", "first@gmail.com");
    staticTestBank.createUser("Second User", "Second Password", "second@gmail.com");
  }

  @ParameterizedTest
  @MethodSource("invalidUsernameProvider")
  public void allMethodsThrowExceptionForInvalidUsername(Executable exe) {
    assertThrows(IllegalArgumentException.class, exe);
  }

  private static Stream<Executable> invalidUsernameProvider() {
    assertTrue(staticTestBank.userExists("First User", "First Password"));
    String invalid = "Invalid Username";
    staticTestBank.createAccount(
        "First User",
        "First Password",
        "Account One",
        AccountType.CheckingAccount,
        1000.0
    );
    staticTestBank.createAccount(
        "Second User",
        "Second Password",
        "Account Two",
        AccountType.CheckingAccount,
        100.0
    );

    return Stream.of(
        () -> staticTestBank.createAccount(
            invalid,
            "First Password",
            "Account Name",
            AccountType.CheckingAccount
          ),
        () -> staticTestBank.withdraw(invalid, "First Password", "First Account", 10),
        () -> staticTestBank.deposit(invalid, "First Password", "First Account", 50),
        () -> staticTestBank.deleteAccount(invalid, "First Password", "First Account"),
        () -> staticTestBank.transfer(
            invalid, "Second User",
            "First Password", "Second Password",
            "Account One", "Account Two",
            5.0
        ),
        () -> staticTestBank.transfer(
            "First User", invalid,
            "First Password", "Second Password",
            "Account One", "Account Two",
            5.0
        ),
        () -> staticTestBank.getBalance(invalid, "First Password", "Account One"),
        () -> staticTestBank.getAccountFor(invalid, "First Password", "Account One"),
        () -> staticTestBank.getAccountsFor(invalid, "First Password")
    );
  }

  @ParameterizedTest
  @MethodSource("invalidPasswordProvider")
  public void allMethodsThrowExceptionForInvalidPassword(Executable exe) {
    assertThrows(IllegalArgumentException.class, exe);
  }

  private static Stream<Executable> invalidPasswordProvider() {
    assertTrue(staticTestBank.userExists("First User", "First Password"));
    String invalid = "Invalid Username";
    return Stream.of(
        () -> staticTestBank.createAccount(
            invalid,
            "First Password",
            "Account Name",
            AccountType.CheckingAccount
          ),
        () -> staticTestBank.withdraw(invalid, "First Password", "First Account", 10),
        () -> staticTestBank.deposit(invalid, "First Password", "First Account", 50),
        () -> staticTestBank.deleteAccount(invalid, "First Password", "First Account"),
        () -> staticTestBank.transfer(
            invalid, "Second User",
            "First Password", "Second Password",
            "Account One", "Account Two",
            5.0
        ),
        () -> staticTestBank.transfer(
            "First User", invalid,
            "First Password", "Second Password",
            "Account One", "Account Two",
            5.0
        ),
        () -> staticTestBank.getBalance(invalid, "First Password", "Account One"),
        () -> staticTestBank.getAccountFor(invalid, "First Password", "Account One"),
        () -> staticTestBank.getAccountsFor(invalid, "First Password")
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
    double[] balances = { 100.0, 10.0, 0.0 };

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
  public void nameUpdateRetainsInformation() {
    assertTrue(this.setBank.userExists("First User", "First Password"));
    assertFalse(this.setBank.userExists("New User", "First Password"));

    List<BankAccount> originalAccounts = this.setBank.getAccountsFor("First User", "First Password");

    this.setBank.updateUser(
        "First User",
        "First Password",
        UserProperty.USERNAME,
        "New User"
    );

    List<BankAccount> newAccounts = this.setBank.getAccountsFor("New User", "First Password");
    assertTrue(this.setBank.userExists("New User", "First Password"));
    assertFalse(this.setBank.userExists("First User", "First Password"));

    int size = newAccounts.size();
    int originalSize = originalAccounts.size();
    if (size != originalSize) {
      fail("When updating the name of a user, the banks were not properly transferred");
    }
    for (int i = 0; i < size; i++) {
      assertEquals(originalAccounts.get(i), newAccounts.get(i));
    }
  }

  @Test
  public void cannotChangeNameToNonUnique() {
    assertTrue(this.setBank.userExists("Second User", "Second Password"));
    assertThrows(IllegalArgumentException.class, () -> {
      this.setBank.updateUser(
          "First User",
          "First Password",
          UserProperty.USERNAME,
          "Second User"
      );
    });
  }

  @Test
  public void passwordUpdateRetainsInformation() {
    assertTrue(this.setBank.userExists("First User", "First Password"));
    assertFalse(this.setBank.userExists("First User", "New Password"));

    List<BankAccount> originalAccounts = this.setBank.getAccountsFor("First User", "First Password");

    this.setBank.updateUser(
        "First User",
        "First Password",
        UserProperty.PASSWORD,
        "New Password"
    );

    List<BankAccount> newAccounts = this.setBank.getAccountsFor("First User", "New Password");
    assertTrue(this.setBank.userExists("First User", "New Password"));
    assertFalse(this.setBank.userExists("First User", "First Password"));

    int size = newAccounts.size();
    int originalSize = originalAccounts.size();
    if (size != originalSize) {
      fail("When updating the password of a user, the banks were not properly transferred");
    }
    for (int i = 0; i < size; i++) {
      assertEquals(originalAccounts.get(i), newAccounts.get(i));
    }
  }

  @Test
  public void emailUpdateRetainsInformation() {
    List<BankAccount> originalAccounts = this.setBank.getAccountsFor("First User", "First Password");
    String originalEmail = this.setBank.getUser("First User", "First Password").email();

    this.setBank.updateUser(
        "First User",
        "First Password",
        UserProperty.EMAIL,
        "new@gmail.com"
    );

    List<BankAccount> newAccounts = this.setBank.getAccountsFor("New User", "First Password");
    String newEmail = this.setBank.getUser("New User", "First Password").email();
    assertTrue(this.setBank.userExists("First User", "First Password"));

    int size = newAccounts.size();
    int originalSize = originalAccounts.size();
    if (size != originalSize) {
      fail("When updating the email of a user, the banks were not properly transferred");
    }
    assertEquals(originalEmail, newEmail);
    for (int i = 0; i < size; i++) {
      assertEquals(originalAccounts.get(i), newAccounts.get(i));
    }
  }

  @Test
  public void cannotUpdateEmailToNonUnique() {
    assertThrows(IllegalArgumentException.class, () -> {
      this.setBank.updateUser(
          "First User",
          "First Password",
          UserProperty.EMAIL,
          "second@gmail.com"
      );
    });
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
  public void oneUserCanHaveCheckingAndSavings() {
    String[] names = { "First Account", "Second Account" };
    AccountType[] types = { AccountType.SavingsAccount, AccountType.CheckingAccount };
    double[] balances = { 100.0, 5.0 };

    for (int i = 0; i < names.length; i++) {
      int finalI = i;
      assertDoesNotThrow(() -> setBank.createAccount(
            "First User",
            "First Password",
            names[finalI],
            types[finalI],
            balances[finalI]
        )
      );
    }
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

  @ParameterizedTest
  @MethodSource("nullFieldsProvider")
  public void createAccountExpectsNonNullFields(
      String userName,
      String password,
      String accountName,
      AccountType type
  ) {
    assertThrows(NullPointerException.class, () -> {
      this.modifiableBank.createAccount(
          userName,
          password,
          accountName,
          type,
          1000.0
      );
    });
  }

  private static Stream<Arguments> nullFieldsProvider() {
    return Stream.of(
        Arguments.of(null, "First Password", "First Account", AccountType.SavingsAccount),
        Arguments.of("First User", null, "First Account", AccountType.CheckingAccount),
        Arguments.of("First User", "First Password", null, AccountType.SavingsAccount),
        Arguments.of("First User", "First Password", "First Account", null)
    );
  }
}