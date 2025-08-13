package unit;

import model.data_engine.table.ColumnDefinitionImpl;
import model.types.DataType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * This is a tester class for ColumnDefinitionImpl objects (record).
 */
class ColumnDefinitionImplTest {
  private ColumnDefinitionImpl standardColumn;

  @BeforeEach
  void setUp() {
    standardColumn = new ColumnDefinitionImpl(
        "username",
        DataType.STRING,
        false,
        true,
        false
    );
  }

  @Test
  public void gettersReturnCorrectValues() {
    assertEquals("username", standardColumn.name());
    assertEquals(DataType.STRING, standardColumn.type());
    assertFalse(standardColumn.nullable());
    assertTrue(standardColumn.unique());
    assertFalse(standardColumn.primaryKey());
  }

  @Test
  public void nullNameThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new ColumnDefinitionImpl(
          null,
          DataType.STRING,
          false,
          false,
          false
      );
    });
  }

  @Test
  public void nullDataTypeThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      new ColumnDefinitionImpl(
          "columnName",
          null,
          false,
          false,
          false
      );
    });
  }

  @Test
  public void nameIsTrimmedAutomatically() {
    ColumnDefinitionImpl columnWithSpaces = new ColumnDefinitionImpl(
        "  spacedName  ",
        DataType.INTEGER,
        true,
        false,
        false
    );

    assertEquals("spacedName", columnWithSpaces.name());
  }

  @Test
  public void nameTrimmingWorksWithVariousWhitespace() {
    ColumnDefinitionImpl tabColumn = new ColumnDefinitionImpl(
        "\t\ttabName\t\t",
        DataType.DOUBLE,
        false,
        false,
        true
    );

    ColumnDefinitionImpl newlineColumn = new ColumnDefinitionImpl(
        "\n\nnewlineName\n\n",
        DataType.BOOLEAN,
        true,
        true,
        false
    );

    assertEquals("tabName", tabColumn.name());
    assertEquals("newlineName", newlineColumn.name());
  }

  @ParameterizedTest
  @EnumSource(DataType.class)
  public void canCreateColumnWithAnyDataType(DataType dataType) {
    assertDoesNotThrow(() -> {
      new ColumnDefinitionImpl(
          "testColumn",
          dataType,
          true,
          false,
          false
      );
    });
  }

  @ParameterizedTest
  @MethodSource("booleanCombinationsProvider")
  public void canCreateColumnWithAnyBooleanCombination(
      boolean nullable,
      boolean unique,
      boolean primaryKey
  ) {
    assertDoesNotThrow(() -> {
      new ColumnDefinitionImpl(
          "testColumn",
          DataType.STRING,
          nullable,
          unique,
          primaryKey
      );
    });
  }

  private static Stream<Arguments> booleanCombinationsProvider() {
    return Stream.of(
        Arguments.of(true, true, true),
        Arguments.of(true, true, false),
        Arguments.of(true, false, true),
        Arguments.of(true, false, false),
        Arguments.of(false, true, true),
        Arguments.of(false, true, false),
        Arguments.of(false, false, true),
        Arguments.of(false, false, false)
    );
  }

  @Test
  public void primaryKeyColumnCanBeCreated() {
    ColumnDefinitionImpl primaryKeyColumn = new ColumnDefinitionImpl(
        "id",
        DataType.INTEGER,
        false,
        true,
        true
    );

    assertEquals("id", primaryKeyColumn.name());
    assertEquals(DataType.INTEGER, primaryKeyColumn.type());
    assertFalse(primaryKeyColumn.nullable());
    assertTrue(primaryKeyColumn.unique());
    assertTrue(primaryKeyColumn.primaryKey());
  }

  @Test
  public void nullableUniqueColumnCanBeCreated() {
    ColumnDefinitionImpl nullableUniqueColumn = new ColumnDefinitionImpl(
        "email",
        DataType.STRING,
        true,
        true,
        false
    );

    assertTrue(nullableUniqueColumn.nullable());
    assertTrue(nullableUniqueColumn.unique());
    assertFalse(nullableUniqueColumn.primaryKey());
  }

  @Test
  public void recordEqualityWorksCorrectly() {
    ColumnDefinitionImpl identical = new ColumnDefinitionImpl(
        "username",
        DataType.STRING,
        false,
        true,
        false
    );

    ColumnDefinitionImpl different = new ColumnDefinitionImpl(
        "password",
        DataType.STRING,
        false,
        true,
        false
    );

    assertEquals(standardColumn, identical);
    assertNotEquals(standardColumn, different);
    assertEquals(standardColumn.hashCode(), identical.hashCode());
  }

  @Test
  public void recordEqualityWithTrimmedNames() {
    ColumnDefinitionImpl trimmedName = new ColumnDefinitionImpl(
        "  username  ",
        DataType.STRING,
        false,
        true,
        false
    );

    assertEquals(standardColumn, trimmedName);
  }

  @Test
  public void canCreateColumnsWithAllDataTypes() {
    assertDoesNotThrow(() -> {
      new ColumnDefinitionImpl("intCol", DataType.INTEGER, false, false, false);
      new ColumnDefinitionImpl("doubleCol", DataType.DOUBLE, false, false, false);
      new ColumnDefinitionImpl("stringCol", DataType.STRING, false, false, false);
      new ColumnDefinitionImpl("boolCol", DataType.BOOLEAN, false, false, false);
      new ColumnDefinitionImpl("dateCol", DataType.DATETIME, false, false, false);
    });
  }

  @Test
  public void emptyStringNameAfterTrimThrowsNoException() {
    assertDoesNotThrow(() -> {
      new ColumnDefinitionImpl(
          "   ",
          DataType.STRING,
          false,
          false,
          false
      );
    });
  }

  @Test
  public void nameTrimmingPreservesNonWhitespaceCharacters() {
    ColumnDefinitionImpl column = new ColumnDefinitionImpl(
        "  column_name_123  ",
        DataType.INTEGER,
        true,
        false,
        true
    );

    assertEquals("column_name_123", column.name());
  }
}