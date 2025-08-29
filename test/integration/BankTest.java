package integration;

import model.Bank;
import model.Managed;
import model.data_engine.DataBase;
import model.security.PasswordService;
import model.security.PasswordServiceImpl;
import model.types.AccountStatus;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This is a tester class for Bank objects.
 */
class BankTest {
  private Managed modifiableBank;
  private Managed setBank;
  private static Managed staticTestBank;
  private static PasswordService testPasswordService;

  @BeforeEach
  void setUp() {
    this.modifiableBank = new Bank(
        DataBase.getInstance(),
        new PasswordServiceImpl(4),
        "Modifiable Test Bank",
        0
    );
    this.modifiableBank.createUser("First User", "First Password", "first@gmail.com");
    this.setBank = new Bank(
        DataBase.getInstance(),
        new PasswordServiceImpl(4),
        "Set Test Bank", 1
    );
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

    testPasswordService = new PasswordServiceImpl(4);
    staticTestBank = new Bank(DataBase.getInstance(), testPasswordService, "Static Test Bank", 12);
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
        () -> staticTestBank.getAccountsFor(invalid, "First Password"),
        () -> staticTestBank.accountExists(invalid,  "First Account1")
    );
  }

  @ParameterizedTest
  @MethodSource("invalidPasswordProvider")
  public void allMethodsThrowExceptionForInvalidPassword(Executable exe) {
    assertThrows(IllegalArgumentException.class, exe);
  }

  private static Stream<Executable> invalidPasswordProvider() {
    assertTrue(staticTestBank.userExists("First User", "First Password"));
    String invalidPassword = "Invalid Password";
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
            "First User",
            invalidPassword,
            "Account Name",
            AccountType.CheckingAccount
        ),
        () -> staticTestBank.withdraw("First User", invalidPassword, "Account One", 10),
        () -> staticTestBank.deposit("First User", invalidPassword, "Account One", 50),
        () -> staticTestBank.deleteAccount("First User", invalidPassword, "Account One"),
        () -> staticTestBank.transfer(
            "First User", "Second User",
            invalidPassword, "Second Password",
            "Account One", "Account Two",
            5.0
        ),
        () -> staticTestBank.transfer(
            "First User", "Second User",
            "First Password", invalidPassword,
            "Account One", "Account Two",
            5.0
        ),
        () -> staticTestBank.getBalance("First User", invalidPassword, "Account One"),
        () -> staticTestBank.getAccountFor("First User", invalidPassword, "Account One"),
        () -> staticTestBank.getAccountsFor("First User", invalidPassword)
    );
  }

  @Test
  public void createAccountDefaultStatusIsFree() {
    this.modifiableBank.createAccount(
        "First User", "First Password", "First Account", AccountType.CheckingAccount
    );
    BankAccount acc = this.modifiableBank.getAccountFor("First User", "First Password", "First Account");
    assertEquals(AccountStatus.Free, acc.status());
  }

  @Test
  public void createUserDoesntModifyInformation() {
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
    assertNotEquals("Second Password", secondUser.password());
    assertTrue(secondUser.password().startsWith("$2a$"));
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

    assertTrue(this.modifiableBank.userExists("First User", "First Password"));
    UserLog log = this.modifiableBank.deleteUser("First User", "First Password");
    assertFalse(this.modifiableBank.userExists("First User", "First Password"));

    User user = log.user();
    Map<String, BankAccount> accounts = log.accounts();

    assertEquals("First User", user.username());
    assertTrue(user.password().startsWith("$2a$"));
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

    List<BankAccount> newAccounts = this.setBank.getAccountsFor("First User", "First Password");
    String newEmail = this.setBank.getUser("First User", "First Password").email();
    assertTrue(this.setBank.userExists("First User", "First Password"));

    int size = newAccounts.size();
    int originalSize = originalAccounts.size();
    if (size != originalSize) {
      fail("When updating the email of a user, the banks were not properly transferred");
    }
    assertEquals("new@gmail.com", newEmail);
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

  @Test
  public void accountDeletionLeavesBehindNoInformation() {
    assertDoesNotThrow(() -> this.setBank.getAccountFor(
            "First User",
            "First Password",
            "First Account1"
        )
    );

    this.setBank.deleteAccount("First User", "First Password", "First Account1");
    assertThrows(IllegalStateException.class, () -> this.setBank.getAccountFor(
            "First User",
            "First Password",
            "First Account1"
        )
    );
  }

  @Test
  public void deletingNonExistentAccountThrowsException() {
    assertTrue(this.modifiableBank.userExists("First User", "First Password"));
    assertThrows(IllegalArgumentException.class, () -> this.setBank.getAccountFor(
            "First User",
            "First Password",
            "Non Existent Account"
        )
    );
  }

  @Test
  public void canWithdrawUntilZeroBalance() {
    double[] withdrawals = { 45.75, 10.25, 4.0, 0.0, 20.0, 15.50, 4.50 };
    for (int i = 0; i < withdrawals.length; i++) {
      int finalI = i;
      final double[] actualWithdrawal = new double[1];
      assertDoesNotThrow(() -> {
        actualWithdrawal[0] = this.setBank.withdraw(
            "First User",
            "First Password",
            "First Account1",
            withdrawals[finalI]
        );
      });
      assertEquals(withdrawals[i], actualWithdrawal[0], 0.0);
    }
  }

  @Test
  public void cannotWithdrawAtZeroBalance() {
    assertThrows(IllegalArgumentException.class, () -> this.setBank.withdraw(
        "Second User",
        "Second Password",
        "Second Account1",
        1.0
    ));
  }

  @Test
  public void cannotWithdrawMoreThanBalance() {
    this.setBank.deposit("Second User", "Second Password", "Second Account1", 1.0);
    assertThrows(IllegalArgumentException.class, () -> this.setBank.withdraw(
        "Second User", "Second Password", "Second Account1", 1.5
    ));
  }

  @Test
  public void cannotWithdrawNegativeBalance() {
    assertThrows(IllegalArgumentException.class, () -> this.setBank.withdraw(
        "First User", "First Password", "First Account1", -1.0
    ));
  }

  @Test
  public void canDepositAnyAmountOfNonNegativeMoney() {
    double[] deposits = { 45.75, 10.25, 4.0, 0.0, 20.0, 15.50, 4.5 };
    this.modifiableBank.createAccount(
        "First User",
        "First Password",
        "First Account",
        AccountType.valueOf("Savings")
    );
    this.modifiableBank.createAccount(
        "First User",
        "First Password",
        "Second Account",
        AccountType.valueOf("Checking"),
        10.0
    );

    double expectedFirst = 0;
    double expectedSecond = 10.0;
    for (int i = 0; i < deposits.length ; i++) {
      int finalI = i;
      expectedFirst += deposits[i];
      expectedSecond += deposits[i];
      assertDoesNotThrow(() -> this.modifiableBank.deposit(
          "First User", "First Password", "First Account", deposits[finalI]
      ));
      assertDoesNotThrow(() -> this.modifiableBank.deposit(
          "First User", "First Password", "Second Account", deposits[finalI]
      ));
      BankAccount accOne = this.modifiableBank.getAccountFor(
          "First User", "First Password", "First Account"
      );
      BankAccount accTwo = this.modifiableBank.getAccountFor(
          "First User", "First Password", "Second Account"
      );
      assertEquals(expectedFirst, accOne.balance());
      assertEquals(expectedSecond, accTwo.balance());
    }
  }

  @Test
  public void cannotDepositNegativeMoney() {
    assertThrows(IllegalArgumentException.class, () -> this.setBank.deposit(
        "First User", "First Password", "First Account1", -1.0
    ));
  }

  @Test
  public void depositOfNegativeBalanceDoesNotChangeCurrentBalance() {
    double originalBal = this.setBank.getAccountFor(
        "First User", "First Password", "First Account1"
    ).balance();
    try {
      this.setBank.deposit(
          "First User", "First Password", "First Account1", -1.0
      );
    } catch (IllegalArgumentException ignored) {
      double newBal = this.setBank.getAccountFor(
          "First User", "First Password", "First Account1"
      ).balance();
      assertEquals(originalBal, newBal, 0.0);
    }
  }

  @Test
  public void transferProperlyUpdatesTheFromAndToAccountBalances() {
    double originalFromAmt = this.setBank.getBalance(
        "First User", "First Password", "First Account1"
    );
    double originalToAmt = this.setBank.getBalance(
        "Second User", "Second Password", "Second Account1"
    );
    double transferAmt = 10.0;

    assertDoesNotThrow(() -> this.modifiableBank.transfer(
        "First User",
        "Second User",
        "First Password",
        "Second Password",
        "First Account1",
        "Second Account1",
        transferAmt
    ));

    double newFromAmt = this.setBank.getBalance(
        "First User", "First Password", "First Account1"
    );
    double newToAmt = this.setBank.getBalance(
        "Second User", "Second Password", "Second Account1"
    );
    assertEquals(originalFromAmt - transferAmt, newFromAmt, 0.0);
    assertEquals(originalToAmt + transferAmt, newToAmt, 0.0);
  }

  @Test
  public void canTransferBetweenTwoAccountsSameUser() {
    this.setBank.createAccount(
        "First User",
        "First Password",
        "First Account2",
        AccountType.valueOf("Savings")
    );

    double originalFromAmount = this.setBank.getBalance(
        "First User", "First Password", "First Account1"
    );
    double originalToAmount = this.setBank.getBalance(
        "First User", "First Password", "First Account2"
    );
    double transferAmount = 10.0;

    assertDoesNotThrow(() -> this.setBank.transfer(
        "First User",
        "First User",
        "First Password",
        "First Password",
        "First Account1",
        "First Account2",
        transferAmount
    ));

    assertEquals(originalFromAmount - transferAmount, this.setBank.getBalance(
        "First User", "First Password", "First Account1"
    ), 0.0);
    assertEquals(originalToAmount + transferAmount, this.setBank.getBalance(
        "First User", "First Password", "First Account2"
    ), 0.0);
  }

  @Test
  public void cannotTransferFromOrTwoNonExistentAccount() {
    assertThrows(IllegalArgumentException.class, () -> this.setBank.transfer(
        "First User",
        "Second User",
        "First Password",
        "Second Password",
        "First Account1",
        "Invalid Account",
        100.0
    ));
    assertThrows(IllegalArgumentException.class, () -> this.setBank.transfer(
        "First User",
        "Second User",
        "First Password",
        "Second Password",
        "Invalid Account",
        "Second Account1",
        10.0
    ));
    assertThrows(IllegalArgumentException.class, () -> this.setBank.transfer(
        "First User",
        "Second User",
        "First Password",
        "Second Password",
        "Invalid Account",
        "Invalid Account",
        10.0
    ));
  }

  @Test
  public void getBalanceWorksForDefaultedAccounts() {
    assertEquals(0, this.setBank.getBalance(
        "Second User", "Second Password", "Second Account1"
    ));
  }

  @Test
  public void getBalanceWorksAfterBalanceUpdates() {
    assertEquals(0, this.setBank.getBalance(
        "Second User", "Second Password", "Second Account1"
    ), 0.0);
    this.setBank.deposit(
        "Second User", "Second Password", "Second Account1", 1.0
    );
    assertEquals(1.0, this.setBank.getBalance(
        "Second User", "Second Password", "Second Account1"
    ), 0.0);
    this.setBank.withdraw(
        "Second User", "Second Password", "Second Account1", 1.0
    );
    assertEquals(0.0, this.setBank.getBalance(
        "Second User", "Second Password", "Second Account1"
    ), 0.0);
  }

  @Test
  public void getBalanceThrowsExceptionIfInvalidAccount() {
    assertThrows(IllegalArgumentException.class, () -> this.setBank.getBalance(
        "Second User", "Second Password", "Invalid account"
    ));
  }

  @Test
  public void getUserReturnedObjectPreservesData() {
    String username = "Second User";
    String password = "Second Password";
    String email = "second@gmail.com";

    this.modifiableBank.createUser(username, password, email);
    User user = this.modifiableBank.getUser("Second User", "Second Password");
    assertEquals(username, user.username());
    assertNotEquals(password, user.password());
    assertTrue(user.password().startsWith("$2a$"));
    assertEquals(email, user.email());
  }

  @Test
  public void getAccountPreservesData() {
    String accountName = "First Account";
    AccountType type = AccountType.CheckingAccount;
    double balance = 100.0;

    this.modifiableBank.createAccount(
        "First User", "First Password", accountName, type, balance
    );
    BankAccount acc = this.modifiableBank.getAccountFor("First User", "First Password", accountName);
    assertEquals(balance, acc.balance());
    assertEquals(type, acc.type());
    assertEquals(AccountStatus.Free, acc.status());
  }

  @Test
  public void getAccountThrowsExceptionIfInvalidAccount() {
    assertThrows(IllegalArgumentException.class, () -> this.setBank.getAccountFor(
        "First User", "First Password", "Invalid Account"
    ));
    assertThrows(IllegalArgumentException.class, () -> this.setBank.getAccountFor(
        "Second User", "Second Password", "Invalid Account"
    ));
  }

  @Test
  public void getAccountsCollectsAllAccountsUnderUser() {
    this.setBank.createAccount(
        "First User", "First Password", "First Account2", AccountType.SavingsAccount
    );
    this.setBank.createAccount(
        "First User", "First Password", "First Account3", AccountType.CheckingAccount, 15.0
    );
    List<BankAccount> accs = this.setBank.getAccountsFor("First User", "First Password");
    assertEquals(3, accs.size());
    BankAccount first = accs.get(0), second = accs.get(1), third = accs.get(2);
    assertEquals(100.0, first.balance(), 0.0);
    assertEquals(0, second.balance(), 0.0);
    assertEquals(15.0, third.balance(), 0.0);
    assertEquals(AccountStatus.Free, first.status());
    assertEquals(AccountStatus.Free, second.status());
    assertEquals(AccountStatus.Free, third.status());
    assertEquals(AccountType.CheckingAccount, first.type());
    assertEquals(AccountType.SavingsAccount, second.type());
    assertEquals(AccountType.CheckingAccount, third.type());
  }

  @Test
  public void getAccountsReturnsEmptyListIfNoAccounts() {
    assertTrue(this.modifiableBank.getAccountsFor("First User", "First Password").isEmpty());
  }

  @Test
  public void accountExistsDoesNotThrowErrorForUnknownAccount() {
    assertDoesNotThrow(() -> this.modifiableBank.accountExists(
        "First User", "Invalid"
    ));
  }

  @Test
  public void accountExistsCorrectlyIdentifiesAccountPresence() {
    assertTrue(this.setBank.accountExists("First User", "First Account1"));
    assertTrue(this.setBank.accountExists("Second User", "Second Account1"));
    assertFalse(this.setBank.accountExists("First User", "Invalid"));
  }

  @Test
  public void userExistsDoesNotThrowErrorForUnknownUser() {
    assertDoesNotThrow(() -> this.modifiableBank.userExists("Unknown", "First Password"));
    assertDoesNotThrow(() -> this.modifiableBank.userExists("First User", "Unknown"));
  }

  @Test
  public void userExistsCorrectlyIdentifiesAccount() {
    assertTrue(this.setBank.userExists("First User", "First Password"));
    assertTrue(this.setBank.userExists("Second User", "Second Password"));
  }

  @ParameterizedTest
  @MethodSource("userExistsCorrectlyIdentifiesAccountAfterModification")
  public void accountExistsCorrectlyIdentifiesAccountAfterModification(
      UserProperty prop,
      String newVal
  ) {
    assertTrue(this.setBank.userExists("First User", "First Password"));
    this.setBank.updateUser("First User", "First Password", prop, newVal);
    assertFalse(this.setBank.userExists("First User", "First Password"));
    if (prop == UserProperty.EMAIL) {
      assertTrue(this.setBank.userExists("First User", "First Password"));
    } else {
      assertTrue(this.setBank.userExists(
          prop == UserProperty.USERNAME ? newVal : "First User",
          prop == UserProperty.PASSWORD ? newVal : "First Password"
      ));
    }
  }

  private static Stream<Arguments> userExistsCorrectlyIdentifiesAccountAfterModification() {
    return Stream.of(
        Arguments.of(UserProperty.PASSWORD, "New Password"),
        Arguments.of(UserProperty.USERNAME, "New User"),
        Arguments.of(UserProperty.EMAIL, "New Email")
    );
  }

  @Test
  public void passwordHashingWorksCorrectly() {
    String plaintext = "TestPassword123";
    User user = this.modifiableBank.createUser("TestUser", plaintext, "test@example.com");

    assertNotEquals(plaintext, user.password());
    assertTrue(user.password().startsWith("$2a$"));
    assertTrue(this.modifiableBank.userExists("TestUser", plaintext));
  }

  @Test
  public void passwordUpdateChangesHash() {
    User originalUser = this.setBank.getUser("First User", "First Password");
    String originalHash = originalUser.password();

    this.setBank.updateUser("First User", "First Password", UserProperty.PASSWORD, "NewPassword");

    User updatedUser = this.setBank.getUser("First User", "NewPassword");
    String newHash = updatedUser.password();

    assertNotEquals(originalHash, newHash);
    assertTrue(newHash.startsWith("$2a$"));
    assertFalse(this.setBank.userExists("First User", "First Password"));
    assertTrue(this.setBank.userExists("First User", "NewPassword"));
  }

  @Test
  public void userLogContainsHashedPasswordsNotPlaintext() {
    UserLog log = this.setBank.deleteUser("First User", "First Password");
    User deletedUser = log.user();

    assertNotEquals("First Password", deletedUser.password());
    assertTrue(deletedUser.password().startsWith("$2a$"));
  }
}