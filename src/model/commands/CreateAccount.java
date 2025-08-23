package model.commands;

import model.Model;
import model.validation.InputValidator;

/**
 * Offers functionality to Create an account under a specified user, so long as there are no
 * other accounts under that user with the specified account properties. The CreateAccount class
 * relies upon the CreateAccountValidator.
 *
 * @see CreateAccountValidator for input validation logic.
 */
public class CreateAccount extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv    The respective input validator for this GenericCommand. It is reliant upon the
   *    *         user for the InputValidator and the respective Command to match as this will not
   *    *         be cross-checked.
   */
  protected CreateAccount(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public void execute() {

  }
}
