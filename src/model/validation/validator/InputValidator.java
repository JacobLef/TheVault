package model.validation.validator;

import model.validation.result.ValidationResult;

/**
 * Offers functionality to validate the input that has been provided to it, with the presumption
 * that the given input corresponds to a valid command that this program offers. All
 * inputs are going to be in the form of a String, as the input passed to an InputValidator will
 * always be user input in the form of a String.
 */
public interface InputValidator {
  /**
   * Parses the given tokenized input and ensures that the user's input matches what this
   * InputValidator defines as a valid input.
   * @param input the tokenized input from the user.
   * @return the respective Validation result to indicate if there were any errors within the
   *         given tokenized data.
   */
  ValidationResult validate(String[] input);
}
