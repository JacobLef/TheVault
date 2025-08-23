package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.validation.validator.InputValidator;

/**
 * Represents the ability to update one of the properties (username, email, password) of a
 * specified user, so long as that user exists within the specified Model. UpdateUser object rely
 * entirely on the UpdateUserValidator for input validation.
 *
 * @see UpdateUserValidator for validation logic.
 */
public class UpdateUser extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv    The respective input validator for this GenericCommand. It is reliant upon the
   *              user for the InputValidator and the respective Command to match as this will not
   *              be cross-checked.
   */
  protected UpdateUser(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public CmdResult execute() {

  }
}
