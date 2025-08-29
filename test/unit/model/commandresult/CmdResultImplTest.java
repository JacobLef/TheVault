package unit.model.commandresult;

import model.commandresult.CmdResult;
import model.commandresult.CmdResultImpl;
import model.types.CmdResultType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test suite for the concise factory-based {@link CmdResultImpl} implementation.
 */
public class CmdResultImplTest {

  @Test
  void createEmptyResult() {
    CmdResult result = CmdResultImpl.empty();

    assertFalse(result.hasProperty());
    assertNull(result.getProperty(String.class));
    assertNull(result.getProperty(Integer.class));
    assertEquals(CmdResultType.NONE, result.getKind());
  }

  @Test
  void createResultWithStringProperty() {
    String value = "Account created successfully";
    CmdResultImpl result = CmdResultImpl.of(String.class, value, CmdResultType.USER_INFO);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(String.class));
    assertEquals(CmdResultType.USER_INFO, result.getKind());
  }

  @Test
  void createResultWithIntegerProperty() {
    Integer value = 1500;
    CmdResultImpl result = CmdResultImpl.of(Integer.class, value, CmdResultType.BALANCE);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(Integer.class));
    assertEquals(CmdResultType.BALANCE, result.getKind());
  }

  @Test
  void createResultWithDoubleProperty() {
    Double value = 250.75;
    CmdResultImpl result = CmdResultImpl.of(Double.class, value, CmdResultType.BALANCE);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(Double.class));
    assertEquals(CmdResultType.BALANCE, result.getKind());
  }

  @Test
  void createResultWithBooleanProperty() {
    Boolean value = true;
    CmdResultImpl result = CmdResultImpl.of(Boolean.class, value, CmdResultType.BOOLEAN_FLAG);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(Boolean.class));
    assertEquals(CmdResultType.BOOLEAN_FLAG, result.getKind());
  }

  @Test
  void createResultWithNullValue() {
    CmdResultImpl result = CmdResultImpl.of(String.class, null, CmdResultType.USER_INFO);

    assertFalse(result.hasProperty());
    assertNull(result.getProperty(String.class));
    assertEquals(CmdResultType.USER_INFO, result.getKind());
  }

  @Test
  void returnNullWhenRequestingWrongType() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "test", CmdResultType.USER_INFO);

    assertNull(result.getProperty(Integer.class));
    assertNull(result.getProperty(Double.class));
    assertNull(result.getProperty(Boolean.class));
  }

  @Test
  void throwExceptionWhenTypeIsNull() {
    assertThrows(NullPointerException.class, () -> {
      CmdResultImpl.of(null, "test", CmdResultType.USER_INFO);
    });
  }

  @Test
  void throwExceptionWhenGetPropertyTypeIsNull() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "test", CmdResultType.USER_INFO);

    assertThrows(NullPointerException.class, () -> {
      result.getProperty(null);
    });
  }

  @Test
  void handleComplexObjectTypes() {
    StringBuilder value = new StringBuilder("Account Details");
    CmdResultImpl result = CmdResultImpl.of(StringBuilder.class, value, CmdResultType.USER_INFO);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(StringBuilder.class));
    assertNull(result.getProperty(String.class));
    assertEquals(CmdResultType.USER_INFO, result.getKind());
  }

  @Test
  void handleInheritanceWithExactTypeMatching() {
    String value = "test";
    CmdResultImpl result = CmdResultImpl.of(String.class, value, CmdResultType.USER_INFO);

    assertEquals(value, result.getProperty(String.class));
    assertNull(result.getProperty(Object.class));
  }

  @Test
  void handleEmptyStringProperty() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "", CmdResultType.USER_INFO);

    assertTrue(result.hasProperty());
    assertEquals("", result.getProperty(String.class));
  }

  @Test
  void handleZeroValueProperties() {
    CmdResultImpl result = CmdResultImpl.of(Integer.class, 0, CmdResultType.BALANCE);

    assertTrue(result.hasProperty());
    assertEquals(Integer.valueOf(0), result.getProperty(Integer.class));
  }

  @Test
  void testEqualsAndHashCode() {
    CmdResultImpl result1 = CmdResultImpl.of(String.class, "test", CmdResultType.USER_INFO);
    CmdResultImpl result2 = CmdResultImpl.of(String.class, "test", CmdResultType.USER_INFO);
    CmdResultImpl result3 = CmdResultImpl.of(String.class, "different", CmdResultType.USER_INFO);
    CmdResultImpl result4 = CmdResultImpl.of(String.class, "test", CmdResultType.ACCOUNT);
    CmdResultImpl empty1 = CmdResultImpl.empty();
    CmdResultImpl empty2 = CmdResultImpl.empty();

    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
    assertNotEquals(result1, result3);
    assertNotEquals(result1, result4);
    assertEquals(empty1, empty2);
    assertEquals(empty1.hashCode(), empty2.hashCode());
  }

  @Test
  void testToString() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "test", CmdResultType.USER_INFO);
    CmdResultImpl empty = CmdResultImpl.empty();

    assertEquals("CmdResultImpl{prop=test, type=String}", result.toString());
    assertEquals("CmdResultImpl{empty}", empty.toString());
  }

  @Test
  void bankingAccountCreationResult() {
    String accountId = "ACC-2024-001";
    CmdResultImpl result = CmdResultImpl.of(String.class, accountId, CmdResultType.ACCOUNT);

    assertTrue(result.hasProperty());
    assertEquals(accountId, result.getProperty(String.class));
    assertEquals(CmdResultType.ACCOUNT, result.getKind());
  }

  @Test
  void bankingBalanceQueryResult() {
    Double balance = 2456.78;
    CmdResultImpl result = CmdResultImpl.of(Double.class, balance, CmdResultType.BALANCE);

    assertTrue(result.hasProperty());
    assertEquals(balance, result.getProperty(Double.class));
    assertEquals(CmdResultType.BALANCE, result.getKind());
  }

  @Test
  void bankingTransferStatusResult() {
    Boolean transferSuccess = true;
    CmdResultImpl result = CmdResultImpl.of(
        Boolean.class,
        transferSuccess,
        CmdResultType.BOOLEAN_FLAG
    );

    assertTrue(result.hasProperty());
    assertTrue(result.getProperty(Boolean.class));
    assertEquals(CmdResultType.BOOLEAN_FLAG, result.getKind());
  }

  @Test
  void bankingFailedOperationResult() {
    CmdResultImpl result = CmdResultImpl.empty();

    assertFalse(result.hasProperty());
    assertNull(result.getProperty(String.class));
    assertNull(result.getProperty(Double.class));
    assertNull(result.getProperty(Boolean.class));
    assertEquals(CmdResultType.NONE, result.getKind());
  }

  @Test
  void bankingAccountListResult() {
    List<String> accounts = Arrays.asList("ACC001", "ACC002", "ACC003");
    CmdResultImpl result = CmdResultImpl.of(List.class, accounts, CmdResultType.ACCOUNT_LIST);

    assertTrue(result.hasProperty());
    assertEquals(accounts, result.getProperty(List.class));
    assertEquals(CmdResultType.ACCOUNT_LIST, result.getKind());
  }

  @Test
  void bankingTransactionResult() {
    Long transactionId = 987654321L;
    CmdResultImpl result = CmdResultImpl.of(Long.class, transactionId, CmdResultType.TRANSACTION);

    assertTrue(result.hasProperty());
    assertEquals(transactionId, result.getProperty(Long.class));
    assertEquals(CmdResultType.TRANSACTION, result.getKind());
  }

  @Test
  void testDifferentCmdResultTypes() {
    String value = "test";
    CmdResultImpl userInfo = CmdResultImpl.of(String.class, value, CmdResultType.USER_INFO);
    CmdResultImpl account = CmdResultImpl.of(String.class, value, CmdResultType.ACCOUNT);

    assertEquals(CmdResultType.USER_INFO, userInfo.getKind());
    assertEquals(CmdResultType.ACCOUNT, account.getKind());
    assertNotEquals(userInfo, account);
  }

  @Test
  void testAllCmdResultTypes() {
    CmdResult none = CmdResultImpl.of(String.class, "test", CmdResultType.NONE);
    CmdResult account = CmdResultImpl.of(String.class, "test", CmdResultType.ACCOUNT);
    CmdResult accountList = CmdResultImpl.of(
        List.class,
        List.of("acc1"),
        CmdResultType.ACCOUNT_LIST
    );
    CmdResult bankAccount = CmdResultImpl.of(String.class, "test", CmdResultType.BANK_ACCOUNT);
    CmdResult bankAccountList = CmdResultImpl.of(
        List.class,
        List.of("acc1"),
        CmdResultType.BANK_ACCOUNT_LIST
    );
    CmdResult userInfo = CmdResultImpl.of(String.class, "test", CmdResultType.USER_INFO);
    CmdResult userLogs = CmdResultImpl.of(String.class, "test", CmdResultType.USER_LOGS);
    CmdResult balance = CmdResultImpl.of(Double.class, 100.0, CmdResultType.BALANCE);
    CmdResult booleanFlag = CmdResultImpl.of(Boolean.class, true, CmdResultType.BOOLEAN_FLAG);
    CmdResult transaction = CmdResultImpl.of(Long.class, 123L, CmdResultType.TRANSACTION);

    assertEquals(CmdResultType.NONE, none.getKind());
    assertEquals(CmdResultType.ACCOUNT, account.getKind());
    assertEquals(CmdResultType.ACCOUNT_LIST, accountList.getKind());
    assertEquals(CmdResultType.BANK_ACCOUNT, bankAccount.getKind());
    assertEquals(CmdResultType.BANK_ACCOUNT_LIST, bankAccountList.getKind());
    assertEquals(CmdResultType.USER_INFO, userInfo.getKind());
    assertEquals(CmdResultType.USER_LOGS, userLogs.getKind());
    assertEquals(CmdResultType.BALANCE, balance.getKind());
    assertEquals(CmdResultType.BOOLEAN_FLAG, booleanFlag.getKind());
    assertEquals(CmdResultType.TRANSACTION, transaction.getKind());
  }
}