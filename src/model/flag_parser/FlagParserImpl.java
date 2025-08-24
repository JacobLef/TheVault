package model.flag_parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete implementation of {@link FlagParser} that maps flag tokens (starting with "--")
 * to their subsequent values, creating structured flag-value pairs from linear token sequences
 * while handling malformed input gracefully.
 */
public class FlagParserImpl implements FlagParser {
  @Override
  public Map<String, String> parseFlags(String[] tokens) throws IllegalArgumentException {
    if (tokens.length <= 1) {
      throw new IllegalArgumentException("Must specify at least one flag!");
    }
    Map<String, String> flags = new HashMap<>();
    String[] excludingKeyWord = Arrays.copyOfRange(tokens, 1, tokens.length);

    for (int i = 0; i < excludingKeyWord.length; i++) {
      String token = excludingKeyWord[i];

      if (token.startsWith("--")) {
        String flagName = token.substring(2);

        if (i + 1 < excludingKeyWord.length && !excludingKeyWord[i + 1].startsWith("--")) {
          flags.put(flagName, excludingKeyWord[i + 1]);
          i++;
        } else {
          throw new IllegalArgumentException("Missing value for one of the flags!");
        }
      }
    }

    return flags;
  }
}
