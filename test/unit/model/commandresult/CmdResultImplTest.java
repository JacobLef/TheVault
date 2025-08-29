package unit.model.commandresult;

import model.commandresult.CmdResult;
import model.commandresult.CmdResultImpl;
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
  }

  @Test
  void createResultWithStringProperty() {
    String value = "Account created successfully";
    CmdResultImpl result = CmdResultImpl.of(String.class, value);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(String.class));
  }

  @Test
  void createResultWithIntegerProperty() {
    Integer value = 1500;
    CmdResultImpl result = CmdResultImpl.of(Integer.class, value);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(Integer.class));
  }

  @Test
  void createResultWithDoubleProperty() {
    Double value = 250.75;
    CmdResultImpl result = CmdResultImpl.of(Double.class, value);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(Double.class));
  }

  @Test
  void createResultWithBooleanProperty() {
    Boolean value = true;
    CmdResultImpl result = CmdResultImpl.of(Boolean.class, value);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(Boolean.class));
  }

  @Test
  void createResultWithNullValue() {
    CmdResultImpl result = CmdResultImpl.of(String.class, null);

    assertFalse(result.hasProperty());
    assertNull(result.getProperty(String.class));
  }

  @Test
  void returnNullWhenRequestingWrongType() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "test");

    assertNull(result.getProperty(Integer.class));
    assertNull(result.getProperty(Double.class));
    assertNull(result.getProperty(Boolean.class));
  }

  @Test
  void throwExceptionWhenTypeIsNull() {
    assertThrows(NullPointerException.class, () -> {
      CmdResultImpl.of(null, "test");
    });
  }

  @Test
  void throwExceptionWhenGetPropertyTypeIsNull() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "test");

    assertThrows(NullPointerException.class, () -> {
      result.getProperty(null);
    });
  }

  @Test
  void handleComplexObjectTypes() {
    StringBuilder value = new StringBuilder("Account Details");
    CmdResultImpl result = CmdResultImpl.of(StringBuilder.class, value);

    assertTrue(result.hasProperty());
    assertEquals(value, result.getProperty(StringBuilder.class));
    assertNull(result.getProperty(String.class));
  }

  @Test
  void handleInheritanceWithExactTypeMatching() {
    String value = "test";
    CmdResultImpl result = CmdResultImpl.of(String.class, value);

    assertEquals(value, result.getProperty(String.class));
    assertNull(result.getProperty(Object.class));
  }

  @Test
  void handleEmptyStringProperty() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "");

    assertTrue(result.hasProperty());
    assertEquals("", result.getProperty(String.class));
  }

  @Test
  void handleZeroValueProperties() {
    CmdResultImpl result = CmdResultImpl.of(Integer.class, 0);

    assertTrue(result.hasProperty());
    assertEquals(Integer.valueOf(0), result.getProperty(Integer.class));
  }

  @Test
  void testEqualsAndHashCode() {
    CmdResultImpl result1 = CmdResultImpl.of(String.class, "test");
    CmdResultImpl result2 = CmdResultImpl.of(String.class, "test");
    CmdResultImpl result3 = CmdResultImpl.of(String.class, "different");
    CmdResultImpl empty1 = CmdResultImpl.empty();
    CmdResultImpl empty2 = CmdResultImpl.empty();

    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
    assertNotEquals(result1, result3);
    assertEquals(empty1, empty2);
    assertEquals(empty1.hashCode(), empty2.hashCode());
  }

  @Test
  void testToString() {
    CmdResultImpl result = CmdResultImpl.of(String.class, "test");
    CmdResultImpl empty = CmdResultImpl.empty();

    assertEquals("CmdResultImpl{prop=test, type=String}", result.toString());
    assertEquals("CmdResultImpl{empty}", empty.toString());
  }

  @Test
  void bankingAccountCreationResult() {
    String accountId = "ACC-2024-001";
    CmdResultImpl result = CmdResultImpl.of(String.class, accountId);

    assertTrue(result.hasProperty());
    assertEquals(accountId, result.getProperty(String.class));
  }

  @Test
  void bankingBalanceQueryResult() {
    Double balance = 2456.78;
    CmdResultImpl result = CmdResultImpl.of(Double.class, balance);

    assertTrue(result.hasProperty());
    assertEquals(balance, result.getProperty(Double.class));
  }

  @Test
  void bankingTransferStatusResult() {
    Boolean transferSuccess = true;
    CmdResultImpl result = CmdResultImpl.of(Boolean.class, transferSuccess);

    assertTrue(result.hasProperty());
    assertTrue(result.getProperty(Boolean.class));
  }

  @Test
  void bankingFailedOperationResult() {
    CmdResultImpl result = CmdResultImpl.empty();

    assertFalse(result.hasProperty());
    assertNull(result.getProperty(String.class));
    assertNull(result.getProperty(Double.class));
    assertNull(result.getProperty(Boolean.class));
  }

  @Test
  void bankingAccountListResult() {
    List<String> accounts = Arrays.asList("ACC001", "ACC002", "ACC003");
    CmdResultImpl result = CmdResultImpl.of(List.class, accounts);

    assertTrue(result.hasProperty());
    assertEquals(accounts, result.getProperty(List.class));
  }

  @Test
  void bankingTransactionResult() {
    Long transactionId = 987654321L;
    CmdResultImpl result = CmdResultImpl.of(Long.class, transactionId);

    assertTrue(result.hasProperty());
    assertEquals(transactionId, result.getProperty(Long.class));
  }
}