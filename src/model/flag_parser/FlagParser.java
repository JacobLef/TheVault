package model.flag_parser;

import java.util.Map;

/**
 * Parses an array of tokens into flag-value pairs, interpreting tokens that begin with "--"
 * as flags and associating them with their subsequent values to create a structured
 * representation of command arguments.
 */
@FunctionalInterface
public interface FlagParser {
  /**
   * Parses the given tokenized inputs for all (flag, value) pairs, such that it can be easily
   * dissected into its parts.
   * @param tokens the tokens to be parsed for flags.
   * @return a Map of (flag, value) pairs.
   * @throws IllegalArgumentException if the given tokens array is of a length <= 1 or if there
   *         are an odd amount of flags to values (one or more flags are not
   *         accompanied by a value).
   */
  Map<String, String> parseFlags(String[] tokens) throws IllegalArgumentException;
}
