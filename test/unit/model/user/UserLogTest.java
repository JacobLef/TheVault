package unit.model.user;

import model.types.AccountStatus;
import model.types.AccountType;
import model.types.TransactionType;
import model.user.BankAccount;
import model.user.Transaction;
import model.user.User;
import model.user.UserLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a tester class for UserLog objects (record).
 */
class UserLogTest {
  private UserLog log;
  private User testUser;
  private Map<String, BankAccount> testAccounts;
  private List<Transaction> testTransactions;

  @BeforeEach
  void setUp() {
    testUser = new User("testuser", "password", "test@email.com");

    testAccounts = new HashMap<>();
    testAccounts.put("checking", new BankAccount(
        "testuser", "checking", 100.0, AccountType.CheckingAccount,
        AccountStatus.Free, LocalDateTime.now()
    ));
    testAccounts.put("savings", new BankAccount(
        "testuser", "savings", 500.0, AccountType.SavingsAccount,
        AccountStatus.Free, LocalDateTime.now()
    ));

    testTransactions = new ArrayList<>();
    testTransactions.add(new Transaction(
        "testuser", "otheruser", "checking", "checking",
        50.0, TransactionType.EXTERNALTRANSFER
    ));

    log = new UserLog(testUser, testAccounts, testTransactions);
  }

  @Test
  public void gettersReturnCorrectValues() {
    assertEquals(testUser, log.user());
    assertEquals(testAccounts, log.accounts());
    assertEquals(testTransactions, log.transactions());
  }

  @Test
  public void nullUserThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new UserLog(null, testAccounts, testTransactions);
    });
  }

  @Test
  public void nullAccountsDefaultsToEmptyMap() {
    UserLog logWithNullAccounts = new UserLog(testUser, null, testTransactions);

    assertNotNull(logWithNullAccounts.accounts());
    assertTrue(logWithNullAccounts.accounts().isEmpty());
    assertEquals(HashMap.class, logWithNullAccounts.accounts().getClass());
  }

  @Test
  public void nullTransactionsDefaultsToEmptyList() {
    UserLog logWithNullTransactions = new UserLog(testUser, testAccounts, null);

    assertNotNull(logWithNullTransactions.transactions());
    assertTrue(logWithNullTransactions.transactions().isEmpty());
    assertEquals(ArrayList.class, logWithNullTransactions.transactions().getClass());
  }

  @Test
  public void allNullCollectionsDefaultToEmpty() {
    UserLog logWithNullCollections = new UserLog(testUser, null, null);

    assertNotNull(logWithNullCollections.accounts());
    assertNotNull(logWithNullCollections.transactions());
    assertTrue(logWithNullCollections.accounts().isEmpty());
    assertTrue(logWithNullCollections.transactions().isEmpty());
  }

  @Test
  public void recordEqualityWorksCorrectly() {
    UserLog identicalLog = new UserLog(testUser, testAccounts, testTransactions);
    UserLog differentLog = new UserLog(
        new User("different", "password", "different@email.com"),
        testAccounts,
        testTransactions
    );

    assertEquals(log, identicalLog);
    assertNotEquals(log, differentLog);
    assertEquals(log.hashCode(), identicalLog.hashCode());
  }

  @Test
  public void emptyCollectionsAreAllowed() {
    UserLog emptyLog = new UserLog(
        testUser,
        new HashMap<>(),
        new ArrayList<>()
    );

    assertNotNull(emptyLog.accounts());
    assertNotNull(emptyLog.transactions());
    assertTrue(emptyLog.accounts().isEmpty());
    assertTrue(emptyLog.transactions().isEmpty());
  }
}