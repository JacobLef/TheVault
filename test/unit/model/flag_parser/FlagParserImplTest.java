package unit.model.flag_parser;

import model.flag_parser.FlagParser;
import model.flag_parser.FlagParserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for {@link FlagParserImpl} class. It should be noted that the inputs
 * this class uses are not representative of the inputs that should be given by the user. The
 * FlagParserImpl should do what it promises to do regardless of the structure and context of the
 * provided user input.
 */
public class FlagParserImplTest {

  private FlagParser flagParser;

  @BeforeEach
  void setUp() {
    flagParser = new FlagParserImpl();
  }

  @Test
  void parseSingleFlagWithValue() {
    String[] tokens = {"command", "--account", "123456"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(1, result.size());
    assertEquals("123456", result.get("account"));
  }

  @Test
  void parseMultipleFlagsWithValues() {
    String[] tokens = {"transfer", "--from", "123456", "--to", "789012", "--amount", "500.00"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(3, result.size());
    assertEquals("123456", result.get("from"));
    assertEquals("789012", result.get("to"));
    assertEquals("500.00", result.get("amount"));
  }

  @Test
  void handleFlagsWithComplexValues() {
    String[] tokens = {"create", "--name", "John Doe", "--email", "john.doe@email.com", "--type", "savings"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(3, result.size());
    assertEquals("John Doe", result.get("name"));
    assertEquals("john.doe@email.com", result.get("email"));
    assertEquals("savings", result.get("type"));
  }

  @Test
  void handleFlagsWithNumericValues() {
    String[] tokens = {"deposit", "--account", "123456", "--amount", "1000.50", "--fee", "2.99"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(3, result.size());
    assertEquals("123456", result.get("account"));
    assertEquals("1000.50", result.get("amount"));
    assertEquals("2.99", result.get("fee"));
  }

  @Test
  void throwExceptionWhenNoFlagsProvided() {
    String[] tokens = {"command"};

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> flagParser.parseFlags(tokens)
    );

    assertEquals("Must specify at least one flag!", exception.getMessage());
  }

  @Test
  void throwExceptionWhenEmptyArrayProvided() {
    String[] tokens = {};

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> flagParser.parseFlags(tokens)
    );

    assertEquals("Must specify at least one flag!", exception.getMessage());
  }

  @Test
  void throwExceptionWhenFlagHasNoValue() {
    String[] tokens = {"command", "--account"};

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> flagParser.parseFlags(tokens)
    );

    assertEquals("Missing value for one of the flags!", exception.getMessage());
  }

  @Test
  void throwExceptionWhenFlagIsFollowedByAnotherFlag() {
    String[] tokens = {"command", "--account", "--amount", "100"};

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> flagParser.parseFlags(tokens)
    );

    assertEquals("Missing value for one of the flags!", exception.getMessage());
  }

  @Test
  void throwExceptionWhenLastTokenIsFlagWithoutValue() {
    String[] tokens = {"command", "--account", "123456", "--amount"};

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> flagParser.parseFlags(tokens)
    );

    assertEquals("Missing value for one of the flags!", exception.getMessage());
  }

  @Test
  void handleFlagsWithEmptyStringValues() {
    String[] tokens = {"command", "--note", "", "--account", "123456"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(2, result.size());
    assertEquals("", result.get("note"));
    assertEquals("123456", result.get("account"));
  }

  @Test
  void ignoreNonFlagTokensBetweenValidFlags() {
    String[] tokens = {
        "command", "ignored", "--account", "123456", "also_ignored", "--amount", "100"
    };

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(2, result.size());
    assertEquals("123456", result.get("account"));
    assertEquals("100", result.get("amount"));
  }

  @Test
  void handleDuplicateFlagsWithLastOneWinning() {
    String[] tokens = {"command", "--account", "123456", "--account", "789012"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(1, result.size());
    assertEquals("789012", result.get("account"));
  }

  @Test
  void handleFlagsWithSpecialCharactersInNames() {
    String[] tokens = {"command", "--account-id", "123456", "--user_name", "john"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(2, result.size());
    assertEquals("123456", result.get("account-id"));
    assertEquals("john", result.get("user_name"));
  }

  @Test
  void handleSingleCharacterFlagNames() {
    String[] tokens = {"command", "--a", "123", "--b", "456"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(2, result.size());
    assertEquals("123", result.get("a"));
    assertEquals("456", result.get("b"));
  }

  @Test
  void returnEmptyMapWhenOnlyNonFlagTokensProvided() {
    String[] tokens = {"command", "token1", "token2", "token3"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertTrue(result.isEmpty());
  }

  @Test
  void handleNullArrayGracefully() {
    assertThrows(NullPointerException.class, () -> {
      flagParser.parseFlags(null);
    });
  }

  @Test
  void parseBankingAccountCreationFlags() {
    String[] tokens = {
        "create-account", "--type", "checking", "--owner", "Alice", "--balance", "1000.00"
    };

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(3, result.size());
    assertEquals("checking", result.get("type"));
    assertEquals("Alice", result.get("owner"));
    assertEquals("1000.00", result.get("balance"));
  }

  @Test
  void parseBankingTransferFlags() {
    String[] tokens = {
        "transfer", "--from", "ACC001", "--to", "ACC002", "--amount", "250.75", "--memo", "Rent"
    };

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(4, result.size());
    assertEquals("ACC001", result.get("from"));
    assertEquals("ACC002", result.get("to"));
    assertEquals("250.75", result.get("amount"));
    assertEquals("Rent", result.get("memo"));
  }

  @Test
  void parseBankingAccountFreezeFlags() {
    String[] tokens = {"freeze", "--account", "ACC123", "--reason", "suspicious_activity"};

    Map<String, String> result = flagParser.parseFlags(tokens);

    assertEquals(2, result.size());
    assertEquals("ACC123", result.get("account"));
    assertEquals("suspicious_activity", result.get("reason"));
  }
}