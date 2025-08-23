package model.commands;

import model.Model;
import model.validation.InputValidator;

/**
 * Represents the ability to delete a specified user from the Model with which this DeleteUser
 * object communicates with, so long as that Model contains the specified user properties.
 * DeleteUser objects rely entirely on the DeleteUserValidator class for all validation logic.
 *
 * @see DeleteUserValidator
 */
public class DeleteUser extends GenericCommand {
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
  protected DeleteUser(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public void execute() {

  }
}
