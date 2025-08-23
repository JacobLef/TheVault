package model.validation;

/**
 * Offers functionality to validate the input that has been provided to it, with the presumption
 * that the given input is with respect to a valid command that this program offers. All inputs
 * are going to be in the form of a String, as the input passed to an InputValidator will always
 * be user input (in the form of a String).
 *
 */
public interface InputValidator {
  ValidationResult validate(String input[]);
}
