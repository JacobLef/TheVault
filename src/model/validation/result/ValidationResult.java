package model.validation.result;

/**
 * Represents the result of parsing and validating an input by a user with respect to a given
 * command. How valid data is defined is reliant entirely on the implementing class of this
 * interface.
 */
public interface ValidationResult {
  /**
   * Is the result an erroneous error?
   * @return true -> it was and false -> otherwise.
   */
  boolean isError();

  /**
   * The error message present if there was an error.
   * @return the String version of the error message.
   */
  String message();
}
