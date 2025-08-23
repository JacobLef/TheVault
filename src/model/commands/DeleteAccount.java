package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.validation.validator.InputValidator;

/**
 * Represents functionality to delete an account under a specified user, so long as that account
 * actually exists under the user within the specified Model. DeleteAccount objects rely on the
 * DeleteAccountValidator validation object for all input validation.
 *
 * @see DeleteAccountValidator for validation logic of user input.
 */
public class DeleteAccount extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv    The respective input validator for this GenericCommand. It is reliant upon the
   *              user for the InputValidator and the respective Command to match as this will not
   *              be cross-checked.
   */
  protected DeleteAccount(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public CmdResult execute() {

  }
}
