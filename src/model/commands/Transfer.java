package model.commands;

import model.Model;
import model.validation.validator.InputValidator;

/**
 * Represents the ability to Transfer information from one account to another account, either
 * under the same user, same account, or different users. The Transfer objects rely entirely on
 * TransferValidator for all validation logic.
 *
 * @see TransferValidator for validation logic.
 */
public class Transfer extends GenericCommand {
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
  protected Transfer(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public void execute() {

  }
}
