package unit;

import model.data_engine.table.ColumnDefinition;
import model.data_engine.table.ColumnDefinitionImpl;
import model.data_engine.table.TableSchemaImpl;
import model.types.DataType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * This is a tester class for TableSchemaImpl objects.
 */
class TableSchemaImplTest {
  private TableSchemaImpl basicSchema;
  private TableSchemaImpl usersSchema;
  private TableSchemaImpl accountsSchema;

  @BeforeEach
  void setUp() {
    basicSchema = new TableSchemaImpl.Builder()
        .tableName("basic_table")
        .addColumn("id", DataType.INTEGER, false, true, true)
        .addColumn("name", DataType.STRING, false, false, false)
        .build();

    usersSchema = new TableSchemaImpl.Builder()
        .tableName("users")
        .addColumn("username", DataType.STRING, false, true, true)
        .addColumn("password", DataType.STRING, false, false, false)
        .addColumn("email", DataType.STRING, true, true, false)
        .addColumn("createdAt", DataType.DATETIME, false, false, false)
        .build();

    accountsSchema = new TableSchemaImpl.Builder()
        .tableName("accounts")
        .addColumn("accountId", DataType.INTEGER, false, true, true)
        .addColumn("ownerUsername", DataType.STRING, false, false, false)
        .addColumn("balance", DataType.DOUBLE, false, false, false)
        .addColumn("isActive", DataType.BOOLEAN, false, false, false)
        .build();
  }

  @Test
  public void builderCreatesValidSchema() {
    assertNotNull(basicSchema);
    assertEquals("basic_table", basicSchema.getTableName());
  }

  @Test
  public void builderWithSinglePrimaryKeySucceeds() {
    assertDoesNotThrow(() -> {
      new TableSchemaImpl.Builder()
          .tableName("test")
          .addColumn("pk", DataType.INTEGER, false, true, true)
          .addColumn("data", DataType.STRING, true, false, false)
          .build();
    });
  }

  @Test
  public void builderWithNoPrimaryKeyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new TableSchemaImpl.Builder()
          .tableName("test")
          .addColumn("col1", DataType.STRING, false, false, false)
          .addColumn("col2", DataType.INTEGER, false, false, false)
          .build();
    });
  }

  @Test
  public void builderWithMultiplePrimaryKeysThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new TableSchemaImpl.Builder()
          .tableName("test")
          .addColumn("pk1", DataType.INTEGER, false, true, true)
          .addColumn("pk2", DataType.STRING, false, true, true)
          .build();
    });
  }

  @Test
  public void getColumnReturnsCorrectColumn() {
    ColumnDefinition idColumn = basicSchema.getColumn("id");
    ColumnDefinition nameColumn = basicSchema.getColumn("name");

    assertNotNull(idColumn);
    assertNotNull(nameColumn);
    assertEquals("id", idColumn.name());
    assertEquals("name", nameColumn.name());
    assertEquals(DataType.INTEGER, idColumn.type());
    assertEquals(DataType.STRING, nameColumn.type());
    assertTrue(idColumn.primaryKey());
    assertFalse(nameColumn.primaryKey());
  }

  @Test
  public void getColumnThrowsExceptionForNonExistentColumn() {
    assertThrows(IllegalArgumentException.class, () -> {
      basicSchema.getColumn("nonexistent");
    });
  }

  @Test
  public void getColumnThrowsExceptionForNullColumnName() {
    assertThrows(NullPointerException.class, () -> {
      basicSchema.getColumn(null);
    });
  }

  @Test
  public void getColumnIsCaseSensitive() {
    assertThrows(IllegalArgumentException.class, () -> {
      basicSchema.getColumn("ID");
    });

    assertDoesNotThrow(() -> {
      basicSchema.getColumn("id");
    });
  }

  @Test
  public void getPrimaryKeyColumnReturnsCorrectColumn() {
    ColumnDefinition primaryKey = usersSchema.getPrimaryKeyColumn();

    assertNotNull(primaryKey);
    assertEquals("username", primaryKey.name());
    assertTrue(primaryKey.primaryKey());
    assertEquals(DataType.STRING, primaryKey.type());
  }

  @Test
  public void getPrimaryKeyColumnForDifferentSchemas() {
    ColumnDefinition basicPK = basicSchema.getPrimaryKeyColumn();
    ColumnDefinition accountsPK = accountsSchema.getPrimaryKeyColumn();

    assertEquals("id", basicPK.name());
    assertEquals("accountId", accountsPK.name());
    assertTrue(basicPK.primaryKey());
    assertTrue(accountsPK.primaryKey());
  }

  @Test
  public void isValidRecordReturnsTrueForValidRecord() {
    Map<String, Object> validRecord = Map.of(
        "id", 123,
        "name", "Test Name"
    );

    assertTrue(basicSchema.isValidRecord(validRecord));
  }

  @Test
  public void isValidRecordReturnsFalseForNullRequiredField() {
    Map<String, Object> invalidRecord = Map.of(
        "id", 123
    );

    assertFalse(basicSchema.isValidRecord(invalidRecord));
  }

  @Test
  public void isValidRecordReturnsTrueForNullNullableField() {
    Map<String, Object> validRecord = Map.of(
        "username", "testuser",
        "password", "password123",
        "createdAt", LocalDateTime.now()
    );

    assertTrue(usersSchema.isValidRecord(validRecord));
  }

  @Test
  public void isValidRecordReturnsFalseForWrongDataType() {
    Map<String, Object> invalidRecord = Map.of(
        "id", "not_an_integer",
        "name", "Test Name"
    );

    assertFalse(basicSchema.isValidRecord(invalidRecord));
  }

  @Test
  public void isValidRecordThrowsExceptionForNullRecord() {
    assertThrows(NullPointerException.class, () -> {
      basicSchema.isValidRecord(null);
    });
  }

  @ParameterizedTest
  @MethodSource("validIntegerValuesProvider")
  public void isValidRecordAcceptsValidIntegerTypes(Object integerValue) {
    Map<String, Object> record = Map.of(
        "id", integerValue,
        "name", "Test"
    );

    assertTrue(basicSchema.isValidRecord(record));
  }

  private static Stream<Object> validIntegerValuesProvider() {
    return Stream.of(
        123,
        456L,
        Integer.valueOf(789),
        Long.valueOf(999L)
    );
  }

  @ParameterizedTest
  @MethodSource("validDoubleValuesProvider")
  public void isValidRecordAcceptsValidDoubleTypes(Object doubleValue) {
    Map<String, Object> record = Map.of(
        "accountId", 1,
        "ownerUsername", "user",
        "balance", doubleValue,
        "isActive", true
    );

    assertTrue(accountsSchema.isValidRecord(record));
  }

  private static Stream<Object> validDoubleValuesProvider() {
    return Stream.of(
        123.45,
        456.78f,
        Double.valueOf(789.12),
        Float.valueOf(999.99f)
    );
  }

  @ParameterizedTest
  @MethodSource("validDateTimeValuesProvider")
  public void isValidRecordAcceptsValidDateTimeTypes(Object dateTimeValue) {
    Map<String, Object> record = Map.of(
        "username", "testuser",
        "password", "password",
        "createdAt", dateTimeValue
    );

    assertTrue(usersSchema.isValidRecord(record));
  }

  private static Stream<Object> validDateTimeValuesProvider() {
    return Stream.of(
        LocalDateTime.now(),
        new Date(),
        LocalDateTime.of(2025, 1, 1, 12, 0),
        new Date(System.currentTimeMillis())
    );
  }

  @Test
  public void isValidRecordReturnsFalseForInvalidStringType() {
    Map<String, Object> invalidRecord = Map.of(
        "id", 123,
        "name", 456
    );

    assertFalse(basicSchema.isValidRecord(invalidRecord));
  }

  @Test
  public void isValidRecordReturnsFalseForInvalidBooleanType() {
    Map<String, Object> invalidRecord = Map.of(
        "accountId", 1,
        "ownerUsername", "user",
        "balance", 100.0,
        "isActive", "not_boolean"
    );

    assertFalse(accountsSchema.isValidRecord(invalidRecord));
  }

  @Test
  public void isValidRecordHandlesEmptyRecord() {
    Map<String, Object> emptyRecord = Map.of();

    assertFalse(basicSchema.isValidRecord(emptyRecord));
    assertFalse(usersSchema.isValidRecord(emptyRecord));
  }

  @Test
  public void isValidRecordHandlesExtraFields() {
    Map<String, Object> recordWithExtra = Map.of(
        "id", 123,
        "name", "Test Name",
        "extraField", "extraValue"
    );

    assertTrue(basicSchema.isValidRecord(recordWithExtra));
  }

  @Test
  public void isValidRecordWithAllRequiredFields() {
    Map<String, Object> completeRecord = Map.of(
        "username", "testuser",
        "password", "hashedpassword",
        "email", "test@example.com",
        "createdAt", LocalDateTime.now()
    );

    assertTrue(usersSchema.isValidRecord(completeRecord));
  }

  @Test
  public void isValidRecordWithMissingNullableField() {
    Map<String, Object> recordMissingEmail = Map.of(
        "username", "testuser",
        "password", "hashedpassword",
        "createdAt", LocalDateTime.now()
    );

    assertTrue(usersSchema.isValidRecord(recordMissingEmail));
  }

  @Test
  public void isValidRecordWithNullNullableField() {
    Map<String, Object> recordWithNullEmail = new HashMap<>();
    recordWithNullEmail.put("username", "testuser");
    recordWithNullEmail.put("password", "hashedpassword");
    recordWithNullEmail.put("email", null);
    recordWithNullEmail.put("createdAt", LocalDateTime.now());

    assertTrue(usersSchema.isValidRecord(recordWithNullEmail));
  }

  @Test
  public void isValidRecordWithMissingRequiredField() {
    Map<String, Object> recordMissingPassword = Map.of(
        "username", "testuser",
        "createdAt", LocalDateTime.now()
    );

    assertFalse(usersSchema.isValidRecord(recordMissingPassword));
  }

  @Test
  public void builderMethodsReturnBuilderInstance() {
    TableSchemaImpl.Builder builder = new TableSchemaImpl.Builder();

    assertSame(builder, builder.tableName("test"));
    assertSame(builder, builder.addColumn("col", DataType.STRING, false, false, false));
  }

  @Test
  public void builderCanCreateComplexSchema() {
    TableSchemaImpl complexSchema = new TableSchemaImpl.Builder()
        .tableName("complex_table")
        .addColumn("id", DataType.INTEGER, false, true, true)
        .addColumn("name", DataType.STRING, false, true, false)
        .addColumn("balance", DataType.DOUBLE, false, false, false)
        .addColumn("isActive", DataType.BOOLEAN, true, false, false)
        .addColumn("lastLogin", DataType.DATETIME, true, false, false)
        .build();

    assertNotNull(complexSchema);
    assertEquals("complex_table", complexSchema.getTableName());
    assertDoesNotThrow(() -> complexSchema.getColumn("id"));
    assertDoesNotThrow(() -> complexSchema.getColumn("name"));
    assertDoesNotThrow(() -> complexSchema.getColumn("balance"));
    assertDoesNotThrow(() -> complexSchema.getColumn("isActive"));
    assertDoesNotThrow(() -> complexSchema.getColumn("lastLogin"));
  }

  @Test
  public void primaryKeyColumnNameIsCorrectlyIdentified() {
    assertEquals("id", basicSchema.getPrimaryKeyColumnName());
    assertEquals("username", usersSchema.getPrimaryKeyColumnName());
    assertEquals("accountId", accountsSchema.getPrimaryKeyColumnName());
  }

  @Test
  public void schemaIsImmutableAfterCreation() {
    List<ColumnDefinition> originalColumns = basicSchema.getColumns();
    int originalSize = originalColumns.size();

    assertThrows(UnsupportedOperationException.class, () -> {
      originalColumns.add(new ColumnDefinitionImpl("newCol", DataType.STRING, false, false, false));
    });

    assertEquals(originalSize, basicSchema.getColumns().size());
  }

  @Test
  public void getTableNameReturnsCorrectName() {
    assertEquals("basic_table", basicSchema.getTableName());
    assertEquals("users", usersSchema.getTableName());
    assertEquals("accounts", accountsSchema.getTableName());
  }

  @Test
  public void getColumnsReturnsAllColumns() {
    List<ColumnDefinition> columns = basicSchema.getColumns();
    assertEquals(2, columns.size());

    List<ColumnDefinition> userColumns = usersSchema.getColumns();
    assertEquals(4, userColumns.size());
  }

  @Test
  public void isValidRecordWithMixedValidAndInvalidTypes() {
    Map<String, Object> mixedRecord = new HashMap<>();
    mixedRecord.put("accountId", 123);
    mixedRecord.put("ownerUsername", "validuser");
    mixedRecord.put("balance", "not_a_double");
    mixedRecord.put("isActive", true);

    assertFalse(accountsSchema.isValidRecord(mixedRecord));
  }

  @Test
  public void isValidRecordWithAllNullValues() {
    Map<String, Object> allNullRecord = new HashMap<>();
    allNullRecord.put("accountId", null);
    allNullRecord.put("ownerUsername", null);
    allNullRecord.put("balance", null);
    allNullRecord.put("isActive", null);

    assertFalse(accountsSchema.isValidRecord(allNullRecord));
  }

  @Test
  public void builderWithDuplicateColumnNamesThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
          new TableSchemaImpl.Builder()
              .tableName("test")
              .addColumn("duplicate", DataType.STRING, false, false, false)
              .addColumn("duplicate", DataType.INTEGER, false, false, true)
              .build();
    });
  }

  @Test
  public void builderWithEmptyColumnListThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new TableSchemaImpl.Builder()
          .tableName("empty_table")
          .build();
    });
  }

  @ParameterizedTest
  @MethodSource("invalidDataTypeProvider")
  public void isValidRecordReturnsFalseForInvalidDataTypes(
      String columnName,
      Object invalidValue,
      DataType expectedType
  ) {
    TableSchemaImpl testSchema = new TableSchemaImpl.Builder()
        .tableName("test")
        .addColumn("pk", DataType.INTEGER, false, false, true)
        .addColumn(columnName, expectedType, false, false, false)
        .build();

    Map<String, Object> invalidRecord = Map.of(
        "pk", 1,
        columnName, invalidValue
    );

    assertFalse(testSchema.isValidRecord(invalidRecord));
  }

  private static Stream<Arguments> invalidDataTypeProvider() {
    return Stream.of(
        Arguments.of("stringCol", 123, DataType.STRING),
        Arguments.of("intCol", "not_int", DataType.INTEGER),
        Arguments.of("doubleCol", "not_double", DataType.DOUBLE),
        Arguments.of("boolCol", "not_bool", DataType.BOOLEAN),
        Arguments.of("dateCol", "not_date", DataType.DATETIME),
        Arguments.of("stringCol", true, DataType.STRING),
        Arguments.of("intCol", 123.45, DataType.INTEGER),
        Arguments.of("boolCol", 123, DataType.BOOLEAN)
    );
  }

  @ParameterizedTest
  @MethodSource("validDataTypeProvider")
  public void isValidRecordReturnsTrueForValidDataTypes(
      String columnName,
      Object validValue,
      DataType expectedType
  ) {
    TableSchemaImpl testSchema = new TableSchemaImpl.Builder()
        .tableName("test")
        .addColumn("pk", DataType.INTEGER, false, false, true)
        .addColumn(columnName, expectedType, false, false, false)
        .build();

    Map<String, Object> validRecord = Map.of(
        "pk", 1,
        columnName, validValue
    );

    assertTrue(testSchema.isValidRecord(validRecord));
  }

  private static Stream<Arguments> validDataTypeProvider() {
    return Stream.of(
        Arguments.of("stringCol", "valid_string", DataType.STRING),
        Arguments.of("intCol", 123, DataType.INTEGER),
        Arguments.of("intCol", 456L, DataType.INTEGER),
        Arguments.of("doubleCol", 123.45, DataType.DOUBLE),
        Arguments.of("doubleCol", 456.78f, DataType.DOUBLE),
        Arguments.of("boolCol", true, DataType.BOOLEAN),
        Arguments.of("boolCol", false, DataType.BOOLEAN),
        Arguments.of("dateCol", LocalDateTime.now(), DataType.DATETIME),
        Arguments.of("dateCol", new Date(), DataType.DATETIME)
    );
  }

  @Test
  public void isValidRecordWithComplexBankingRecord() {
    Map<String, Object> bankingRecord = Map.of(
        "username", "john_doe",
        "password", "$2a$12$hashedpassword",
        "email", "john@example.com",
        "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0)
    );

    assertTrue(usersSchema.isValidRecord(bankingRecord));
  }

  @Test
  public void isValidRecordWithAccountRecord() {
    Map<String, Object> accountRecord = Map.of(
        "accountId", 12345,
        "ownerUsername", "john_doe",
        "balance", 1500.75,
        "isActive", true
    );

    assertTrue(accountsSchema.isValidRecord(accountRecord));
  }

  @Test
  public void isValidRecordFailsWithMixedRequiredAndNullable() {
    Map<String, Object> partialRecord = Map.of(
        "username", "testuser",
        "createdAt", LocalDateTime.now()
    );

    assertFalse(usersSchema.isValidRecord(partialRecord));
  }

  @Test
  public void columnMapIsPopulatedCorrectly() {
    ColumnDefinition usernameCol = usersSchema.getColumn("username");
    ColumnDefinition passwordCol = usersSchema.getColumn("password");
    ColumnDefinition emailCol = usersSchema.getColumn("email");

    assertNotNull(usernameCol);
    assertNotNull(passwordCol);
    assertNotNull(emailCol);

    assertTrue(usernameCol.primaryKey());
    assertTrue(usernameCol.unique());
    assertFalse(usernameCol.nullable());

    assertFalse(passwordCol.primaryKey());
    assertFalse(passwordCol.unique());
    assertFalse(passwordCol.nullable());

    assertFalse(emailCol.primaryKey());
    assertTrue(emailCol.unique());
    assertTrue(emailCol.nullable());
  }

  @Test
  public void primaryKeyColumnIsCorrectlyIdentified() {
    ColumnDefinition primaryKey = usersSchema.getColumn(usersSchema.getPrimaryKeyColumnName());

    assertNotNull(primaryKey);
    assertTrue(primaryKey.primaryKey());
    assertEquals("username", primaryKey.name());
  }

  @Test
  public void builderHandlesNullTableName() {
    TableSchemaImpl.Builder builder = new TableSchemaImpl.Builder()
        .addColumn("id", DataType.INTEGER, false, false, true);

    assertThrows(NullPointerException.class, builder::build);
  }

  @Test
  public void schemaSupportsAllDataTypes() {
    TableSchemaImpl allTypesSchema = new TableSchemaImpl.Builder()
        .tableName("all_types")
        .addColumn("id", DataType.INTEGER, false, false, true)
        .addColumn("name", DataType.STRING, false, false, false)
        .addColumn("balance", DataType.DOUBLE, false, false, false)
        .addColumn("isActive", DataType.BOOLEAN, false, false, false)
        .addColumn("createdAt", DataType.DATETIME, false, false, false)
        .build();

    Map<String, Object> allTypesRecord = Map.of(
        "id", 1,
        "name", "test",
        "balance", 100.0,
        "isActive", true,
        "createdAt", LocalDateTime.now()
    );

    assertTrue(allTypesSchema.isValidRecord(allTypesRecord));
  }

  @Test
  public void isValidRecordWithBoundaryValues() {
    Map<String, Object> boundaryRecord = Map.of(
        "id", Integer.MAX_VALUE,
        "name", ""
    );

    assertTrue(basicSchema.isValidRecord(boundaryRecord));
  }

  @Test
  public void isValidRecordFailsWithNullInNonNullableColumn() {
    Map<String, Object> recordWithNull = new HashMap<>();
    recordWithNull.put("id", null);
    recordWithNull.put("name", "Test");

    assertFalse(basicSchema.isValidRecord(recordWithNull));
  }

  @Test
  public void getColumnWithEmptyStringThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      basicSchema.getColumn("");
    });
  }

  @Test
  public void getColumnWithWhitespaceOnlyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      basicSchema.getColumn("   ");
    });
  }

  @Test
  public void primaryKeyColumnNameNeverNull() {
    assertNotNull(basicSchema.getPrimaryKeyColumnName());
    assertNotNull(usersSchema.getPrimaryKeyColumnName());
    assertNotNull(accountsSchema.getPrimaryKeyColumnName());
  }

  @Test
  public void getPrimaryKeyColumnNeverReturnsNull() {
    assertNotNull(basicSchema.getPrimaryKeyColumn());
    assertNotNull(usersSchema.getPrimaryKeyColumn());
    assertNotNull(accountsSchema.getPrimaryKeyColumn());
  }
}