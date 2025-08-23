package model.commands;

import model.Model;
import model.validation.InputValidator;

/**
 * Represents an encapsulation of the data and functionality needed to create a new, unique User
 * within the Model. The created user must be unique amongst all users inside of the same Managed
 * model. The CreateUser class relies on the CreateUserValidator validation class.
 *
 * @see CreateUserValidator for the validation logic.
 */
public class CreateUser extends GenericCommand {
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
  protected CreateUser(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public void execute() {

  }
}
