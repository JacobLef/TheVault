package model.commands;

import model.Model;
import model.validation.validator.InputValidator;

/**
 * Represents the ability to withdraw from a specified account under a specified user, so long as
 * they exist under the given user and model, respectively. Withdraw objects rely entirely on the
 * WithdrawValidator for all input validation.
 *
 * @see WithdrawValidator for validation logic.
 */
public class Withdraw extends GenericCommand {
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
  protected Withdraw(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public void execute() {

  }
}
